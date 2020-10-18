package com.gitlab.grcc.commit.graph

import com.gitlab.grcc.commit.graph.GraphManager.addGraphFromGroup
import com.gitlab.grcc.commit.graph.GraphManager.addGraphFromProject
import com.gitlab.grcc.commit.graph.gitlab.Project
import com.gitlab.grcc.commit.graph.http.GitLabApiClient
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection
import java.awt.Rectangle
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

@ExperimentalStdlibApi
fun showDialogAddGraph(frame: JFrame, data: TimeSeriesCollection, client: GitLabApiClient) {
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
                val series = data.series.filterIsInstance(TimeSeries::class.java)
                series.firstOrNull { it.key == nameText }?.let {
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