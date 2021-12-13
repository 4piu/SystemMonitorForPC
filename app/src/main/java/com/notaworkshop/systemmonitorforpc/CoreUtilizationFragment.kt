package com.notaworkshop.systemmonitorforpc

import android.content.Context
import android.os.Bundle
import android.util.Log
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

class CoreUtilizationFragment : Fragment(), HistoryViewer {
    companion object {
        private val TAG = MonitorFragment::class.qualifiedName
    }
    private var chart: LineChart? = null
    private val colorPalette = arrayOf<Int>(
        R.color.m_red,
        R.color.m_purple,
        R.color.m_indigo,
        R.color.m_cyan,
        R.color.m_green,
        R.color.m_lime,
        R.color.m_amber,
        R.color.m_deep_orange,
        R.color.m_grey,
        R.color.m_blue_grey,
        R.color.m_brown,
        R.color.m_orange,
        R.color.m_yellow,
        R.color.m_light_green,
        R.color.m_teal,
        R.color.m_blue,
        R.color.m_deep_purple,
        R.color.m_pink
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_core_utilization, container, false)
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
        chart = activity?.findViewById(R.id.core_utilization_chart)
        chart?.axisRight?.textColor = textColorPrimary
        chart?.legend?.textColor = textColorPrimary
        chart?.description?.isEnabled = false
        chart?.xAxis?.isEnabled = false
        chart?.axisLeft?.isEnabled = false
        chart?.setTouchEnabled(false)
    }

    override fun updateView(data: LinkedList<JSONObject?>, historySize: Int) {
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
            val dataset = LineDataSet(entryList, "core $index")
            dataset.setDrawCircles(false)
            dataset.color = ContextCompat.getColor(requireContext(), colorPalette[index % colorPalette.size])
            lineDatasetList.add(dataset)
        }
        chart?.data = LineData(lineDatasetList)
        chart?.invalidate()
    }
}