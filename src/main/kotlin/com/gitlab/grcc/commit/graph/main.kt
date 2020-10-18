package com.gitlab.grcc.commit.graph

import com.gitlab.grcc.commit.graph.api.graph.GraphData
import com.gitlab.grcc.commit.graph.api.http.GitLabApiClient
import org.jfree.chart.ChartPanel
import org.jfree.chart.axis.DateAxis
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import java.awt.BorderLayout
import java.awt.Rectangle
import java.text.SimpleDateFormat
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel

@ExperimentalStdlibApi
fun main() {
    // グラフデータ
    val data = GraphData()

    // グラフ
    val chart = GraphData.createTimeSeriesChart("", "Date", "Commits", data).apply {
        val plot = plot as XYPlot
        plot.renderer = XYLineAndShapeRenderer().apply {
            defaultShapesVisible = true // グラフに点を追加
        }
        val dateAxis = plot.domainAxis as DateAxis
        dateAxis.dateFormatOverride = SimpleDateFormat("yyyy/MM/dd")
    }
    val chartPanel = ChartPanel(chart)

    // ApiClient を定義
    val client = GitLabApiClient()

    // グラフ表示
    val frame = JFrame().apply {
        val frame = this
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE // バツボタンの処理
        title = "GitLabCommitGraph" // ウィンドウタイトル
        bounds = Rectangle(900, 600) // ウィンドウサイズを指定
        setLocationRelativeTo(null) // ウィンドウを中心に配置
        layout = BorderLayout() // 東西南北・中央で要素を管理
        add(chartPanel, BorderLayout.CENTER) // チャートパネルをウィンドウの中央に配置
        add(JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(JButton("グラフを追加").apply {
                addActionListener {
                    showDialogAddGraph(frame, data, client)
                }
            })
            add(JButton("グラフを削除").apply {
                addActionListener {
                    showDialogRemoveGraph(frame, data)
                }
            })
            add(JButton("グラフを出力").apply {
                addActionListener {
                    showDialogSaveGraph(frame, chart, chartPanel)
                }
            })
        }, BorderLayout.SOUTH) // ボタンをウィンドウの下に配置
        isVisible = true // ウィンドウを表示
    }

    client.setAccessToken(frame, "アクセストークンを入力")
    client.setOnFailure(frame)
}
