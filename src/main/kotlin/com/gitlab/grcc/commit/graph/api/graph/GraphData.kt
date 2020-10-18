package com.gitlab.grcc.commit.graph.api.graph

import org.jfree.chart.ChartFactory
import org.jfree.chart.JFreeChart
import org.jfree.data.time.TimeSeriesCollection

class GraphData {
    private val timeSeriesCollection = TimeSeriesCollection() // 時間を軸にしたデータ

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