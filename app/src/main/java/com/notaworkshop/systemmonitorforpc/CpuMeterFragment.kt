package com.notaworkshop.systemmonitorforpc

import android.graphics.Color
import android.os.Bundle
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

class CpuMeterFragment : Fragment(), HistoryViewer {
    companion object {
        private val TAG = MonitorFragment::class.qualifiedName
    }
    private var chart: PieChart? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cpu_meter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chart = activity?.findViewById(R.id.cpu_sum_chart)
        chart?.setUsePercentValues(true)
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
        val percent = data.last?.getJSONObject("cpu")?.getString("percent_sum")
        // textview
        activity?.findViewById<TextView>(R.id.cpu_meter_percent_integer)?.text = if (percent==null) "--" else percent.split(".")[0]
        activity?.findViewById<TextView>(R.id.cpu_meter_percent_fragment)?.text = if (percent==null) "-" else percent.split(".")[1]
        // chart
        val fPercent = percent?.toFloat()?:0F
        val dataEntryList = arrayListOf(PieEntry(fPercent), PieEntry(100F - fPercent))
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