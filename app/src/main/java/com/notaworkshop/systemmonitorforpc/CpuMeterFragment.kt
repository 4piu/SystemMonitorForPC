package com.notaworkshop.systemmonitorforpc

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.collection.CircularArray
import org.json.JSONObject
import java.util.*

class CpuMeterFragment : Fragment(), HistoryViewer {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cpu_meter, container, false)
    }

    override fun updateView(data: LinkedList<JSONObject?>) {
        val percent = data.last?.getJSONObject("cpu")?.getString("percent_sum")
        activity?.findViewById<TextView>(R.id.cpu_meter_percent_integer)?.text = if (percent==null) "---" else percent.split(".")[0]
        activity?.findViewById<TextView>(R.id.cpu_meter_percent_fragment)?.text = if (percent==null) "-" else percent.split(".")[1]
    }
}