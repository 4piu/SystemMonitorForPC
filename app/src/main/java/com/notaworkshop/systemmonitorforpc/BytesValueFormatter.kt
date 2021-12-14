package com.notaworkshop.systemmonitorforpc

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow


class BytesValueFormatter : ValueFormatter() {

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        if (value <= 0) return "0"
        val units = arrayOf("B", "kB", "MB", "GB", "TB")
        val digitGroups = (log10(value.toDouble()) / log10(1000.0)).toInt()
        return DecimalFormat("#,##0.#").format(value / 1000.0.pow(digitGroups.toDouble()))
            .toString() + " " + units[digitGroups]
    }
}