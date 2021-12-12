package com.notaworkshop.systemmonitorforpc

import org.json.JSONObject
import java.util.*

interface HistoryViewer {
    fun updateView(data: LinkedList<JSONObject?>)
}