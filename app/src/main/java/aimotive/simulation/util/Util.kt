package aimotive.simulation.util

fun Any.tag(target: Any = this): String =
    target.javaClass.simpleName
