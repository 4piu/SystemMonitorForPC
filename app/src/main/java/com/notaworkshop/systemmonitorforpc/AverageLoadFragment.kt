package com.notaworkshop.systemmonitorforpc

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class AverageLoadFragment : Fragment(), HistoryViewer {
    companion object {
        private val TAG = MonitorFragment::class.qualifiedName
    }

    private var chart: LineChart? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_average_load, container, false)
    }

    @ColorInt
    fun Context.getColorThemeRes(@AttrRes id: Int): Int {
        val resolvedAttr = TypedValue()
        this.theme.resolveAttribute(id, resolvedAttr, true)
        return this.getColor(resolvedAttr.resourceId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val textColorPrimary = requireContext().getColorThemeRes(android.R.attr.textColorPrimary)
        chart = activity?.findViewById(R.id.average_load_chart)
        chart?.axisRight?.textColor = textColorPrimary
        chart?.legend?.textColor = textColorPrimary
        chart?.description?.isEnabled = false
        chart?.xAxis?.isEnabled = false
        chart?.axisLeft?.isEnabled = false
        chart?.setTouchEnabled(false)
    }

    override fun updateView(data: LinkedList<JSONObject?>, historySize: Int) {
        val avgLoad1EntryList = ArrayList<Entry>()
        val avgLoad5EntryList = ArrayList<Entry>()
        val avgLoad15EntryList = ArrayList<Entry>()

        for (i in 0 until historySize) {
            val record = data[i]
            if (record == null) {  // blank record
                avgLoad1EntryList.add(Entry(i.toFloat(), 0F))
                avgLoad5EntryList.add(Entry(i.toFloat(), 0F))
                avgLoad15EntryList.add(Entry(i.toFloat(), 0F))
            } else { // valid record
                val jsonArray = record.getJSONObject("cpu").getJSONArray("load_avg")
                val avgLoad = Array(jsonArray.length()) { jsonArray.getDouble(it).toFloat() }
                avgLoad1EntryList.add(Entry(i.toFloat(), avgLoad[0]))
                avgLoad5EntryList.add(Entry(i.toFloat(), avgLoad[1]))
                avgLoad15EntryList.add(Entry(i.toFloat(), avgLoad[2]))
            }
        }
        val lineDatasetList = ArrayList<ILineDataSet>()
        val dataset1Min = LineDataSet(avgLoad1EntryList, "1min").apply {
            this.setDrawCircles(false)
            this.setColor(ContextCompat.getColor(requireContext(), R.color.m_cyan), 200)
        }
        val dataset5Min = LineDataSet(avgLoad5EntryList, "5min").apply {
            this.setDrawCircles(false)
            this.setColor(ContextCompat.getColor(requireContext(), R.color.m_pink), 200)
        }
        val dataset15Min = LineDataSet(avgLoad15EntryList, "15min").apply {
            this.setDrawCircles(false)
            this.setColor(ContextCompat.getColor(requireContext(), R.color.m_lime), 200)
        }
        lineDatasetList.add(dataset1Min)
        lineDatasetList.add(dataset5Min)
        lineDatasetList.add(dataset15Min)
        chart?.data = LineData(lineDatasetList)
        chart?.invalidate()
    }
}