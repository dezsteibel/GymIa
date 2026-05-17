package com.gymia.domain

import javax.inject.Inject

class SessionTimer @Inject constructor() {
    private var startTimestamp: Long = 0L
    private var stopTimestamp: Long = 0L
    private var running: Boolean = false

    fun start() {
        startTimestamp = System.currentTimeMillis()
        running = true
    }

    fun elapsed(): Int {
        if (!running) return ((stopTimestamp - startTimestamp) / 1000).toInt().coerceAtLeast(0)
        return ((System.currentTimeMillis() - startTimestamp) / 1000).toInt().coerceAtLeast(0)
    }

    fun stop(): Int {
        stopTimestamp = System.currentTimeMillis()
        running = false
        return elapsed()
    }

    fun reset() {
        startTimestamp = 0L
        stopTimestamp = 0L
        running = false
    }
}
