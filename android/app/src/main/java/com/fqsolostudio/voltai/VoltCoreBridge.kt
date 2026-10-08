package com.fqsolostudio.voltai

class VoltCoreBridge {
    init {
        System.loadLibrary("volt_core")
    }

    external fun initVoltEngine(dbPath: String): Boolean
    external fun queryVolt(prompt: String): String
}
