package com.notaworkshop.systemmonitorforpc

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.collection.CircularArray
import org.json.JSONObject

class CpuMeterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cpu_meter, container, false)
    }

    fun updateView(data: CircularArray<JSONObject>) {
        val text = activity?.findViewById<TextView>(R.id.cpu_meter_percent)
        val latestData = data.last
        val percent = latestData?.getJSONObject("cpu")?.getDouble("percent_sum")
        text?.text = if (percent==null) "--%" else "${percent}%"
    }
}