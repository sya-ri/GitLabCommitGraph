package com.gitlab.grcc.commit.graph

import com.gitlab.grcc.commit.graph.GraphManager.addGraphFromGroup
import com.gitlab.grcc.commit.graph.GraphManager.addGraphFromProject
import com.gitlab.grcc.commit.graph.gitlab.Project
import com.gitlab.grcc.commit.graph.http.GitLabApiClient
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection
import java.awt.BorderLayout
import java.awt.Rectangle
import javax.swing.*
import javax.swing.JOptionPane.ERROR_MESSAGE
import javax.swing.JOptionPane.PLAIN_MESSAGE

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

                        fun addGraphAction(action: (nameText: String, groupId: String, onSuccess: () -> Unit, onFailure: () -> Unit) -> Unit) {
                            val nameText = nameTextField.text
                            if (nameText.isNullOrBlank()) return
                            data.series.filterIsInstance(TimeSeries::class.java).firstOrNull { (it.key as? String) == nameText }?.let {
                                data.removeSeries(it)
                            }
                            val urlText = urlTextField.text ?: return
                            val groupId = urlText.removePrefix("https://gitlab.com/").removeSuffix("/")
                            addProjectButton.isEnabled = false
                            addGroupButton.isEnabled = false
                            action.invoke(nameText, groupId, {
                                addProjectButton.isEnabled = true
                                addGroupButton.isEnabled = true
                            }, {
                                addProjectButton.isEnabled = true
                                addGroupButton.isEnabled = true
                            })
                        }

                        addProjectButton.addActionListener {
                            addGraphAction { nameText, groupId, onSuccess, onFailure ->
                                client.addGraphFromProject(data, nameText, Project(nameText, groupId), onSuccess, onFailure)
                            }
                        }

                        addGroupButton.addActionListener {
                            addGraphAction { nameText, groupId, onSuccess, onFailure ->
                                client.addGraphFromGroup(data, nameText, groupId, onSuccess, onFailure)
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
    client.onFailure = {
        fun showErrorMessage(message: String) {
            JOptionPane.showMessageDialog(frame, message, "エラー", ERROR_MESSAGE)
        }

        when (it) {
            GitLabApiClient.RequestResult.Failure.NotContent -> {
                showErrorMessage("Jsonの取得に失敗")
            }
            GitLabApiClient.RequestResult.Failure.BadRequest -> {
                showErrorMessage("APIリクエストに必要な値が足りません")
            }
            GitLabApiClient.RequestResult.Failure.Unauthorized -> {
                client.accessToken = enterAccessToken(frame, "アクセストークンを再入力")
            }
            GitLabApiClient.RequestResult.Failure.Forbidden -> {
                showErrorMessage("アクセスすることが許可されていません")
            }
            GitLabApiClient.RequestResult.Failure.NotFound -> {
                showErrorMessage("プロジェクトもしくはグループが見つかりませんでした")
            }
            GitLabApiClient.RequestResult.Failure.MethodNotAllowed -> {
                showErrorMessage("そのリクエストはサポートされていません")
            }
            GitLabApiClient.RequestResult.Failure.Conflict -> {
                showErrorMessage("競合が発生しました")
            }
            GitLabApiClient.RequestResult.Failure.RequestDenied -> {
                showErrorMessage("リクエストが拒否されました")
            }
            GitLabApiClient.RequestResult.Failure.Unprocessable -> {
                showErrorMessage("処理に失敗しました")
            }
            GitLabApiClient.RequestResult.Failure.ServerError -> {
                showErrorMessage("サーバーでエラーが発生しました")
            }
            GitLabApiClient.RequestResult.Failure.UnHandle -> {
                showErrorMessage("ハンドリングされていないレスポンスコードです")
            }
        }
    }
}

fun enterAccessToken(frame: JFrame, message: String): String {
    while (true) {
        val accessToken = JOptionPane.showInputDialog(frame, message, "API", PLAIN_MESSAGE)
        if (accessToken.isNotBlank()) return accessToken
    }
}