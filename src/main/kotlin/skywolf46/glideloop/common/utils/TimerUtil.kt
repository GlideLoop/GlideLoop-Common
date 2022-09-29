package skywolf46.glideloop.common.utils

import skywolf46.glideloop.core.abstraction.Trigger
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

object TimerUtil {
    interface TaskLooper {
        fun addTask(task: (TaskLooper) -> Unit)

        fun shutdown(): List<(TaskLooper) -> Unit>

        fun shutdownGracefully()
    }

    class TaskExtractor internal constructor(val looper: InternalTaskLooper) : Iterator<(TaskLooper) -> Unit> {
        override fun hasNext(): Boolean {
            return looper.taskList.isNotEmpty()
        }

        override fun next(): (TaskLooper) -> Unit {
            return looper.taskList.removeAt(0)
        }
    }

    class InternalTaskLooper(internal val taskList: MutableList<(TaskLooper) -> Unit>) : TaskLooper {
        private val isTaskLooping = AtomicBoolean(false)
        override fun addTask(task: (TaskLooper) -> Unit) {
            taskList += taskList
        }

        override fun shutdown(): List<(TaskLooper) -> Unit> {
            throw IllegalStateException("Task shutdown not supported for InternalTaskLooper")
        }

        override fun shutdownGracefully() {
            throw IllegalStateException("Task shutdown not supported for InternalTaskLooper")
        }


        fun executeCurrentTasks() {
            if (isTaskLooping.get())
                throw IllegalStateException()
            isTaskLooping.set(true)
            for (x in taskList) {
                x.invoke(this)
            }
            isTaskLooping.set(false)
        }

        fun hasMoreTasks(): Boolean {
            return taskList.isNotEmpty()
        }
    }

    class ThreadedTaskTimer(val tick: AtomicInteger) : TaskLooper {
        private val thread: Thread = object : Thread() {
            override fun run() {
                while (!shutdown.get()) {
                    val tasks = mutableListOf<(TaskLooper) -> Unit>()
                    dataLock.write {
                        cachedTasks.addAll(cachedTasks)
                        cachedTasks.clear()
                    }
                    if (gracefulShutdown.get() && cachedTasks.isEmpty()) {
                        return
                    }
                    InternalTaskLooper(tasks).apply {
                        while (hasMoreTasks()) {
                            executeCurrentTasks()
                        }
                    }
                    try {
                        sleep(tick.get().toLong())
                    } catch (e: Throwable) {
                        // ignore
                    }
                }
            }
        }

        private val shutdown = AtomicBoolean(false)

        private val gracefulShutdown = AtomicBoolean(false)

        private val cachedTasks = mutableListOf<(TaskLooper) -> Unit>()

        private val dataLock = ReentrantReadWriteLock()

        init {
            thread.start()
        }

        override fun addTask(task: (TaskLooper) -> Unit) {
            dataLock.read {
                cachedTasks += task
            }
        }

        override fun shutdown(): List<(TaskLooper) -> Unit> {
            shutdown.set(true)
            thread.join()
            return cachedTasks
        }

        override fun shutdownGracefully() {
            gracefulShutdown.set(true)
            thread.join()
        }
    }
}