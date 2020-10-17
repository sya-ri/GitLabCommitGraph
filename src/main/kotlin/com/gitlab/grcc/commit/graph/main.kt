package com.gitlab.grcc.commit.graph

import com.gitlab.grcc.commit.graph.gitlab.Commit.Companion.compress
import com.gitlab.grcc.commit.graph.gitlab.getAllCommits
import com.gitlab.grcc.commit.graph.gitlab.getAllProject
import com.gitlab.grcc.commit.graph.http.GitLabApiClient
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.time.TimeTableXYDataset
import java.awt.BorderLayout
import java.awt.Rectangle
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JOptionPane.PLAIN_MESSAGE
import javax.swing.JPanel
import javax.swing.JTextField
import kotlin.system.exitProcess

@ExperimentalStdlibApi
suspend fun main() {
    // グラフデータ
    val data = TimeTableXYDataset() // 時間を軸にしたデータ

    // グラフ表示
    val frame = JFrame().apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE // バツボタンの処理
        title = "GitLabCommitGraph" // ウィンドウタイトル
        bounds = Rectangle(900, 600) // ウィンドウサイズを指定
        setLocationRelativeTo(null) // ウィンドウを中心に配置
        layout = BorderLayout() // 東西南北・中央で要素を管理
        add(ChartPanel(ChartFactory.createTimeSeriesChart("", "Date", "Commits", data).apply {
            val plot = plot as XYPlot
            plot.renderer = XYLineAndShapeRenderer().apply {
                defaultShapesVisible = true // グラフに点を追加
            }
        }), BorderLayout.CENTER) // チャートパネルをウィンドウの中央に配置
        add(JButton("追加").apply {
            addActionListener {
                JDialog().apply {
                    bounds = Rectangle(450, 130) // ウィンドウサイズを指定
                    isResizable = false // サイズ変更を無効化
                    setLocationRelativeTo(null) // ウィンドウを中心に配置
                    add(JPanel().apply {
                        add(JLabel("名前"))
                        add(JTextField(32))
                        add(JLabel("URL"))
                        add(JTextField(32))
                        add(JButton("プロジェクト として追加"))
                        add(JButton("グループ として追加"))
                    })
                    isVisible = true // ウィンドウを表示
                }
            }
        }, BorderLayout.SOUTH) // ラベルをウィンドウの下に配置
        isVisible = true // ウィンドウを表示
    }

    // アクセストークンを入力
    var accessToken: String
    while (true) {
        accessToken = JOptionPane.showInputDialog(frame, "アクセストークンを入力", "API", PLAIN_MESSAGE) ?: exitProcess(0) // キャンセルで終了
        if (accessToken.isNotBlank()) break // 入力で無限ループを抜ける
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
    val compressDates = commits.compress()
    var sumCommit = 0
    compressDates.forEach { (day, commit) ->
        sumCommit += commit
        data.add(day, sumCommit.toDouble(), 1)
    }
}
