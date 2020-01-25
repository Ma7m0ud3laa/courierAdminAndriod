package com.twoam.agent.utilities

enum class TaskPriority(var status: String) {
    LOW("Low"),
    NORMAL("Normal"),
    HIGHT("High"),
    CRITICAL("Critical");

    override fun toString(): String {
        return status
    }
}
