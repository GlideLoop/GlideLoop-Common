package skywolf46.glideloop.common.utils

class Flag(val flag: Int) {
    fun positive(type: Int): Flag {
        return Flag(flag or (1 shl type))
    }

    fun negative(type: Int): Flag {
        return Flag(flag and (1 shl type).inv())
    }

    fun isPositive(type: Int): Boolean {
        return (flag and (1 shl type)) != 0
    }
}

fun Int.asFlag(): Flag {
    return Flag(this)
}