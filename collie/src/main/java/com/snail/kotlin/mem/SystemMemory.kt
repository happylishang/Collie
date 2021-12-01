package com.snail.kotlin.mem

class SystemMemory {
    @JvmField
    var availMem: Long = 0
    @JvmField
    var lowMemory = false
    @JvmField
    var threshold: Long = 0
    @JvmField
    var totalMem: Long = 0
}