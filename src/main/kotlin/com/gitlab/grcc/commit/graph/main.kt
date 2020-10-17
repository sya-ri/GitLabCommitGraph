package com.gitlab.grcc.commit.graph

import com.gitlab.grcc.commit.graph.gitlab.Commit.Companion.compress
import com.gitlab.grcc.commit.graph.gitlab.Project
import com.gitlab.grcc.commit.graph.gitlab.getAllCommits
import com.gitlab.grcc.commit.graph.gitlab.getAllProject
import com.gitlab.grcc.commit.graph.http.GitLabApiClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection
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
fun main() {
    // グラフデータ
    val data = TimeSeriesCollection() // 時間を軸にしたデータ

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
        add(ChartPanel(ChartFactory.createTimeSeriesChart("", "Date", "Commits", data).apply {
            val plot = plot as XYPlot
            plot.renderer = XYLineAndShapeRenderer().apply {
                defaultShapesVisible = true // グラフに点を追加
            }
        }), BorderLayout.CENTER) // チャートパネルをウィンドウの中央に配置
        add(JButton("グラフを追加").apply {
            addActionListener {
                JDialog(frame).apply {
                    bounds = Rectangle(450, 130) // ウィンドウサイズを指定
                    isResizable = false // サイズ変更を無効化
                    setLocationRelativeTo(null) // ウィンドウを中心に配置
                    add(JPanel().apply {
                        val nameTextField: JTextField
                        val urlTextField: JTextField
                        add(JLabel("名前"))
                        add(JTextField(32).apply {
                            nameTextField = this
                        })
                        add(JLabel("URL"))
                        add(JTextField(32).apply {
                            urlTextField = this
                        })
                        add(JButton("プロジェクト として追加").apply {
                            addActionListener addProject@ {
                                val nameText = nameTextField.text ?: return@addProject
                                val urlText = urlTextField.text ?: return@addProject
                                val groupId = urlText.removePrefix("https://gitlab.com/").removeSuffix("/")
                                client.addGraphFromProject(data, nameText, Project(nameText, groupId))
                            }
                        })
                        add(JButton("グループ として追加").apply {
                            addActionListener addProject@ {
                                val nameText = nameTextField.text ?: return@addProject
                                val urlText = urlTextField.text ?: return@addProject
                                val groupId = urlText.removePrefix("https://gitlab.com/").removeSuffix("/")
                                client.addGraphFromGroup(data, nameText, groupId)
                            }
                        })
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

    // ApiClientのアクセストークンを初期化
    client.accessToken = accessToken
}

@ExperimentalStdlibApi
fun GitLabApiClient.addGraphFromGroup(data: TimeSeriesCollection, name: String, groupId: String) {
    GlobalScope.launch {
        // プロジェクトの取得
        val projects = getAllProject(groupId)

        // グラフに反映
        addGraphFromProject(data, name, projects)
    }
}

@ExperimentalStdlibApi
fun GitLabApiClient.addGraphFromProject(data: TimeSeriesCollection, name: String, project: Project) {
    addGraphFromProject(data, name, setOf(project))
}

@ExperimentalStdlibApi
fun GitLabApiClient.addGraphFromProject(data: TimeSeriesCollection, name: String, projects: Set<Project>) {
    GlobalScope.launch {
        data.addSeries(TimeSeries(name).apply {
            // コミットの取得
            val commits = getAllCommits(projects)

            // コミットをグラフに反映
            val compressDates = commits.compress()
            var sumCommit = 0
            compressDates.forEach { (day, commit) ->
                sumCommit += commit
                add(day, sumCommit.toDouble())
            }
        })
    }
}