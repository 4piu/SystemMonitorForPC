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


class NetworkActivityFragment : Fragment(), HistoryViewer {
    companion object {
        private val TAG = NetworkActivityFragment::class.qualifiedName
    }

    private var chart: LineChart? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_network_activity, container, false)
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
        chart = activity?.findViewById(R.id.net_activity_chart)
        chart?.axisRight?.textColor = textColorPrimary
        chart?.legend?.textColor = textColorPrimary
        chart?.description?.isEnabled = false
        chart?.xAxis?.isEnabled = false
        chart?.axisLeft?.isEnabled = false
        chart?.setTouchEnabled(false)
    }

    override fun updateView(data: LinkedList<JSONObject?>, historySize: Int) {
        val netRxEntryList = ArrayList<Entry>()
        val netTxEntryList = ArrayList<Entry>()

        for (i in 0 until historySize) {
            val record = data[i]
            if (record == null) {  // blank record
                netRxEntryList.add(Entry(i.toFloat(), 0F))
                netTxEntryList.add(Entry(i.toFloat(), 0F))
            } else { // valid record
                val jsonArray = record.getJSONObject("network").getJSONArray("speed")
                val netSpeed = Array(jsonArray.length()) { jsonArray.getDouble(it).toFloat() }
                netRxEntryList.add(Entry(i.toFloat(), netSpeed[0]))
                netTxEntryList.add(Entry(i.toFloat(), netSpeed[1]))
            }
        }
        val lineDatasetList = ArrayList<ILineDataSet>()
        val datasetRx = LineDataSet(netRxEntryList, "Rx").apply {
            this.setDrawCircles(false)
            this.color = ContextCompat.getColor(requireContext(), R.color.m_deep_orange)
        }
        val datasetTx = LineDataSet(netTxEntryList, "Tx").apply {
            this.setDrawCircles(false)
            this.color = ContextCompat.getColor(requireContext(), R.color.m_green)
        }
        lineDatasetList.add(datasetRx)
        lineDatasetList.add(datasetTx)
        chart?.data = LineData(lineDatasetList)
        chart?.invalidate()
    }
}