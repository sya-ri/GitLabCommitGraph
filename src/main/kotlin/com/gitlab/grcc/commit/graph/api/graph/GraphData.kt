package com.gitlab.grcc.commit.graph.api.graph

import org.jfree.chart.ChartFactory
import org.jfree.chart.JFreeChart
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection
import javax.swing.table.DefaultTableModel

class GraphData {
    private val timeSeriesCollection = TimeSeriesCollection() // 時間を軸にしたデータ
    private val seriesList = mutableMapOf<String, TimeSeries>()

    private fun addSeries(name: String, series: TimeSeries) {
        seriesList[name] = series
        timeSeriesCollection.addSeries(series)
    }

    suspend fun addSeries(name: String, action: suspend TimeSeries.() -> Unit) {
        addSeries(name, TimeSeries(name).apply {
            action.invoke(this)
        })
    }

    fun removeSeries(name: String) {
        seriesList.remove(name)?.let {
            timeSeriesCollection.removeSeries(it)
        }
    }

    fun containsSeries(name: String): Boolean {
        return seriesList.contains(name)
    }

    class TableModel(private val data: GraphData): DefaultTableModel() {
        init {
            val seriesNameList = data.seriesList.keys.map { arrayOf(it) }.toTypedArray()
            setDataVector(seriesNameList, arrayOf("グラフ名"))
        }

        override fun removeRow(row: Int) {
            data.removeSeries(getValueAt(row, 0) as String)
            super.removeRow(row)
        }
    }

    companion object {
        fun createTimeSeriesChart(
            title: String,
            timeAxisLabel: String,
            valueAxisLabel: String,
            data: GraphData,
        ): JFreeChart {
            return ChartFactory.createTimeSeriesChart(
                title,
                timeAxisLabel,
                valueAxisLabel,
                data.timeSeriesCollection
            )
        }
    }
}