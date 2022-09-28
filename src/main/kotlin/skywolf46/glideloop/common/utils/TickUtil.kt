package skywolf46.glideloop.common.utils

import kotlin.math.roundToLong

object TickUtil {
    fun createTicker(timeBetweenTicks: Long): Ticker {
        return Ticker(timeBetweenTicks)
    }

    fun createStrictTicker(timeBetweenTicks: Long, strictToMinus: Boolean = false): Ticker {
        return StrictTicker(timeBetweenTicks, strictToMinus)
    }

    fun createScalingTicker(timeBetweenTicks: Long): ScalingTicker {
        return ScalingTicker(timeBetweenTicks)
    }

    fun createAutoScalingTicker(
        timeBetweenTicks: Long,
        decreasePerTick: Double = 1.0,
        keepTick: Long = 4L
    ): ScalingTicker {
        return AutoScalingTicker(timeBetweenTicks, decreasePerTick, keepTick)
    }

    // 일정 시간마다 틱을 호출하는 티커.
    // 따로 보정이 존재하지 않으며, 한 틱이 끝날때마다 파라미터로 주어진 시간만큼 정지한다.
    open class Ticker(private val timeBetweenTicks: Long) {
        private val tasks = mutableListOf<() -> Unit>()

        open fun getTick(): Long {
            return timeBetweenTicks
        }

        open fun tick() {
            executeTasks()
        }

        protected fun executeTasks() {
            val executingTask = tasks.toList()
            tasks.clear()
            for (x in executingTask) {
                try {
                    x.invoke()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }

    // 어떠한 상황에서도 틱 사이의 간격을 주어진 파라미터로 맞추려고 시도하는 티커.
    // strictToMinus가 true일 경우, 넘친 틱에 대해서도 맞추기를 시도한다.
    open class StrictTicker(timeBetweenTicks: Long, val strictToMinus: Boolean) : Ticker(timeBetweenTicks) {
        private var elapsedTick = 0L

        override fun getTick(): Long {
            return if (elapsedTick > super.getTick()) 0 else super.getTick() - elapsedTick
        }

        override fun tick() {
            elapsedTick = (elapsedTick - super.getTick()).coerceAtLeast(0)
            val time = System.currentTimeMillis()
            executeTasks()
            val elapsed = System.currentTimeMillis() - time
            elapsedTick += if (strictToMinus) elapsed else elapsed.coerceAtMost(super.getTick())
        }
    }


    // 주어진 퍼센티지에 따라 틱 간격을 조정하는 티커.
    // 0%는 정상 틱이며, 계수가 높아질수록 틱이 느려진다.
    // 예를 들어, 23.5%는 현재 틱이 123.5%의 속도로 돌아감을 의미한다.
    open class ScalingTicker(timeBetweenTicks: Long) : Ticker(timeBetweenTicks) {
        private var tickingPercentage = 0.0
        private var currentTick = super.getTick()

        override fun getTick(): Long {
            return currentTick
        }

        fun getOriginalTick(): Long {
            return super.getTick()
        }

        fun updateTimeDelay(delayPercentage: Double) {
            this.tickingPercentage = delayPercentage
            this.currentTick = (getOriginalTick() * (1 + delayPercentage / 100.0)).roundToLong()
        }

        fun getTimeDelay(): Double {
            return tickingPercentage
        }

        override fun tick() {
            executeTasks()
        }
    }

    /**
     * 현재 티커의 활성화 시간에 따라 자동으로 틱 간격을 조정하는 티커.
     * AutoScalingTicker은 최대한 균일한 서버 틱을 유지하려고 시도하며,틱의 감소도 주어진 파라미터에 따라 서서히 감소된다.
     *
     *
     * increasePerTick - 현재 틱이 틱을 벗어났을 경우 추가될 틱의 퍼센티지.
     * decreasePerTick - 현재 틱이 안정화되었을경우, 감소시킬 틱의 퍼센티지.
     * keepTick - 틱이 변경되었을 경우 유지 시간
     */
    open class AutoScalingTicker(
        timeBetweenTicks: Long,
        private val decreasePerTick: Double = 1.0,
        private val keepTick: Long
    ) : ScalingTicker(timeBetweenTicks) {
        private var lastTick = System.currentTimeMillis()
        private var keepTickLeft = 0L

        override fun tick() {
            lastTick = System.currentTimeMillis()
            executeTasks()
            if (--keepTickLeft >= 0L) {
                return
            }
            val elapsed = System.currentTimeMillis() - lastTick
            if (elapsed > getTick()) {
                keepTickLeft = keepTick
                updateTimeDelay((elapsed - getOriginalTick()) / getOriginalTick().toDouble())
            } else if (getTimeDelay() > 0) {
                keepTickLeft = keepTick
                updateTimeDelay((getTimeDelay() - decreasePerTick).coerceAtLeast(0.0))
            }
        }
    }
}