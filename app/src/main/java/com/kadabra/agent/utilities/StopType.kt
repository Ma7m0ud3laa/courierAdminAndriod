package com.kadabra.agent.utilities

enum class StopType(var status: String) {
    PICKUP("pick Up"),
    DROP_OFF("Drop Off"),
    STOP("Stop");

        override fun toString(): String {
            return status
        }
}