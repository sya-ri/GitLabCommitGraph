package com.gitlab.grcc.commit.graph

import com.gitlab.grcc.commit.graph.GraphManager.addGraphFromGroup
import com.gitlab.grcc.commit.graph.GraphManager.addGraphFromProject
import com.gitlab.grcc.commit.graph.gitlab.Project
import com.gitlab.grcc.commit.graph.http.GitLabApiClient
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
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
                        val addProjectButton: JButton
                        val addGroupButton: JButton

                        add(JLabel("名前"))
                        add(JTextField(32).apply {
                            nameTextField = this
                        })
                        add(JLabel("URL"))
                        add(JTextField(32).apply {
                            urlTextField = this
                        })
                        add(JButton("プロジェクト として追加").apply {
                            addProjectButton = this
                        })
                        add(JButton("グループ として追加").apply {
                            addGroupButton = this
                        })

                        fun addGraphAction(action: (nameText: String, groupId: String, onSuccess: () -> Unit) -> Unit) {
                            val nameText = nameTextField.text ?: return
                            val urlText = urlTextField.text ?: return
                            val groupId = urlText.removePrefix("https://gitlab.com/").removeSuffix("/")
                            addProjectButton.isEnabled = false
                            addGroupButton.isEnabled = false
                            action.invoke(nameText, groupId) {
                                addProjectButton.isEnabled = true
                                addGroupButton.isEnabled = true
                            }
                        }

                        addProjectButton.addActionListener {
                            addGraphAction { nameText, groupId, onSuccess ->
                                client.addGraphFromProject(data, nameText, Project(nameText, groupId), onSuccess)
                            }
                        }

                        addGroupButton.addActionListener {
                            addGraphAction { nameText, groupId, onSuccess ->
                                client.addGraphFromGroup(data, nameText, groupId, onSuccess)
                            }
                        }
                    })
                    isVisible = true // ウィンドウを表示
                }
            }
        }, BorderLayout.SOUTH) // ラベルをウィンドウの下に配置
        isVisible = true // ウィンドウを表示
    }

    client.accessToken = enterAccessToken(frame, "アクセストークンを入力")
}

fun enterAccessToken(frame: JFrame, message: String): String {
    // アクセストークンを入力
    var accessToken: String
    while (true) {
        accessToken = JOptionPane.showInputDialog(frame, message, "API", PLAIN_MESSAGE) ?: exitProcess(0) // キャンセルで終了
        if (accessToken.isNotBlank()) break // 入力で無限ループを抜ける
    }
    return accessToken
}