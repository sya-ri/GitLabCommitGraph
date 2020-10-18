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

    fun addSeries(name: String, action: TimeSeries.() -> Unit) {
        addSeries(name, TimeSeries(name).apply(action))
    }

    fun removeSeries(name: String) {
        seriesList.remove(name)?.let {
            timeSeriesCollection.removeSeries(it)
        }
    }

    fun clearSeries() {
        seriesList.clear()
        timeSeriesCollection.removeAllSeries()
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

        fun clearRows() {
            data.clearSeries()
            rowCount = 0
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