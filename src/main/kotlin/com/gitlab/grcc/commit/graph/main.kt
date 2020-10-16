package com.gitlab.grcc.commit.graph

import com.gitlab.grcc.commit.graph.gitlab.Commit.Companion.compressDate
import com.gitlab.grcc.commit.graph.gitlab.getAllCommits
import com.gitlab.grcc.commit.graph.gitlab.getAllProject
import com.gitlab.grcc.commit.graph.http.GitLabApiClient
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.time.TimeTableXYDataset
import java.awt.Rectangle
import javax.swing.JFrame
import javax.swing.JOptionPane
import kotlin.system.exitProcess

@ExperimentalStdlibApi
suspend fun main() {
    // グラフデータ
    val data = TimeTableXYDataset()

    // 日付とコミットのグラフ作成
    val chart = ChartFactory.createTimeSeriesChart("", "Date", "Commits", data).apply {
        val plot = plot as XYPlot
        plot.renderer = XYLineAndShapeRenderer().apply {
            defaultShapesVisible = true
        }
    }

    // グラフ表示
    val frame = JFrame().apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        title = "GitLabCommitGraph"
        bounds = Rectangle(900, 600)
        setLocationRelativeTo(null)
        add(ChartPanel(chart))
        isVisible = true
    }

    // アクセストークンを入力
    var accessToken: String
    while (true) {
        accessToken = JOptionPane.showInputDialog(frame, "アクセストークンを入力") ?: exitProcess(0)
        if (accessToken.isNotBlank()) break
    }

    // ApiClient を定義
    val client = GitLabApiClient(accessToken)

    // 取得するトップグループを入力
    print("GroupId: ")
    val groupId = readLine()
    if (groupId.isNullOrBlank()) return

    // プロジェクトの取得
    println()
    print("Getting All Projects ... ")
    val projects = client.getAllProject(groupId)
    println(projects.size)

    // コミットの取得
    println()
    print("Getting All Commits ... ")
    val commits = client.getAllCommits(projects)
    println(commits.size)

    // コミットをグラフに反映
    val compressDates = commits.compressDate()
    var sumCommit = 0
    compressDates.forEach { (day, commit) ->
        sumCommit += commit
        data.add(day, sumCommit.toDouble(), 1)
    }
}
