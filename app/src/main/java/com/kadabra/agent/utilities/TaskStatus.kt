package com.kadabra.agent.utilities

enum class TaskStatus(var status: String) {
    NEW("New"),
    IN_PROGRESS("In progress"),
    COMPLETED("Completed"),
    WAITING_FEEDBASK("Waiting feedback"),
    POST_PONDED("Postponed"),
    CANCELLED("Cancelled");

        override fun toString(): String {
            return status
        }
}