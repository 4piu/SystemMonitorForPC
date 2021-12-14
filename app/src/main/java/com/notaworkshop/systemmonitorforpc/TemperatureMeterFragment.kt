package com.notaworkshop.systemmonitorforpc

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import org.json.JSONObject
import java.util.*

class TemperatureMeterFragment : Fragment(), HistoryViewer {
    companion object {
        private val TAG = TemperatureMeterFragment::class.qualifiedName
    }
    private var chart: PieChart? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_temperature, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chart = activity?.findViewById(R.id.temperature_percent_chart)
        chart?.setDrawEntryLabels(false)
        chart?.isDrawHoleEnabled = true
        chart?.holeRadius = 85f
        chart?.setHoleColor(Color.TRANSPARENT)
        chart?.maxAngle = 270f
        chart?.rotationAngle = 135f
        chart?.legend?.isEnabled = false
        chart?.description?.isEnabled = false
        chart?.setTouchEnabled(false)
    }

    override fun updateView(data: LinkedList<JSONObject?>, historySize: Int) {
        val sensorObject = data.last?.getJSONObject("sensor")
        val sensorKeys = sensorObject?.keys()
        val sensorValArray = if (sensorKeys != null && sensorKeys.hasNext()) sensorObject.getJSONArray(sensorKeys.next()) else null
        val temperature = if (sensorValArray != null) Array(sensorValArray.getJSONArray(0).length()) { sensorValArray.getJSONArray(0).getString(it) }[1] else null
        // textview
        activity?.findViewById<TextView>(R.id.temperature_meter_percent_integer)?.text = if (temperature==null) "--" else temperature.split(".")[0]
        activity?.findViewById<TextView>(R.id.temperature_meter_percent_fragment)?.text = if (temperature==null) "-" else temperature.split(".")[1][0].toString()
        // chart
        val fTemperature = temperature?.toFloat()?:0F
        val dataEntryList = arrayListOf(PieEntry(fTemperature), PieEntry(if (fTemperature < 100F ) 100F - fTemperature else 0F))
        val dataset = PieDataSet(dataEntryList, "").apply {
            this.setColors(
                ContextCompat.getColor(requireContext(), R.color.teal_200),
                ContextCompat.getColor(requireContext(),R.color.transparent_dim))
            this.setDrawValues(false)
        }
        chart?.data = PieData(dataset)
        chart?.invalidate()
    }
}