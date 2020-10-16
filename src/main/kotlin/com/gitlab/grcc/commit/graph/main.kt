package com.gitlab.grcc.commit.graph

import com.gitlab.grcc.commit.graph.gitlab.Commit.Companion.compressDate
import com.gitlab.grcc.commit.graph.gitlab.getAllCommits
import com.gitlab.grcc.commit.graph.gitlab.getAllProject
import com.gitlab.grcc.commit.graph.http.GitLabApiClient
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.data.time.TimeTableXYDataset
import javax.swing.JFrame


@ExperimentalStdlibApi
suspend fun main() {
    // アクセストークンを入力
    print("AccessToken: ")
    val accessToken = readLine()
    if (accessToken.isNullOrBlank()) return

    // 取得するトップグループを入力
    print("GroupId: ")
    val groupId = readLine()
    if (groupId.isNullOrBlank()) return

    // ApiClient を定義
    val client = GitLabApiClient(accessToken)

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

    // 日付とコミットのグラフ作成
    val data = TimeTableXYDataset()
    val compressDates = commits.compressDate()
    var sumCommit = 0
    compressDates.forEach { (day, commit) ->
        sumCommit += commit
        data.add(day, sumCommit.toDouble(), 1)
    }
    val chart = ChartFactory.createTimeSeriesChart("Test", "Date", "Commits", data)

    // グラフ表示
    JFrame().apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        title = "GitLabCommitGraph"
        extendedState = JFrame.MAXIMIZED_BOTH
        isVisible = true
        add(ChartPanel(chart))
    }
}
