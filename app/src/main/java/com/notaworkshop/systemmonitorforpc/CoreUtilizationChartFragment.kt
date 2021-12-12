package com.notaworkshop.systemmonitorforpc

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class CoreUtilizationChartFragment : Fragment(), HistoryViewer {
    companion object {
        private val TAG = MonitorFragment::class.qualifiedName
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_core_utilization_chart, container, false)
    }

    override fun updateView(data: LinkedList<JSONObject?>, historySize: Int) {
        val chart = activity?.findViewById<LineChart>(R.id.core_utilization_chart)
        val coreUtilList = ArrayList<ArrayList<Entry>>()

        if (coreUtilList.size == 0) {   // init array to length of history
            data.forEach { record ->
                val coreNum = record?.getJSONObject("cpu")?.getInt("core_num")
                coreNum?.let {
                    while (coreUtilList.size < it) {coreUtilList.add(ArrayList())}
                }
            }
        }

        for (i in 0 until historySize) {
            val record = data[i]
            if (record == null) {  // blank record
                coreUtilList.forEach { it.add(Entry(i.toFloat(), 0F)) }
            } else { // valid record
                val jsonArray = record.getJSONObject("cpu").getJSONArray("percent")
                val coreUtil = Array(jsonArray.length()) { jsonArray.getDouble(it) }
                coreUtil.forEachIndexed { index, percent ->
                    coreUtilList[index].add(Entry(i.toFloat(), percent.toFloat()))
                }
            }
        }
        val lineDatasetList = ArrayList<ILineDataSet>()
        coreUtilList.forEachIndexed { index, entryList ->
            lineDatasetList.add(LineDataSet(entryList, "core $index"))
        }
        chart?.data = LineData(lineDatasetList)
        chart?.invalidate()
    }
}