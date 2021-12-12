package com.notaworkshop.systemmonitorforpc

import androidx.collection.CircularArray
import org.json.JSONObject

interface HistoryViewer {
    fun updateView(data: CircularArray<JSONObject>)
}