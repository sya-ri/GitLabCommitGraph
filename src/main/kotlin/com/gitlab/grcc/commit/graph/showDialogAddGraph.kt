package com.gitlab.grcc.commit.graph

import com.gitlab.grcc.commit.graph.api.gitlab.Commit.Companion.compress
import com.gitlab.grcc.commit.graph.api.gitlab.Project
import com.gitlab.grcc.commit.graph.api.gitlab.getAllCommits
import com.gitlab.grcc.commit.graph.api.gitlab.getAllProject
import com.gitlab.grcc.commit.graph.api.http.GitLabApiClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
            add(JLabel("名前"))
            val nameTextField = JTextField(32).apply { add(this) }
            add(JLabel("URL"))
            val urlTextField = JTextField(32).apply { add(this) }
            val addProjectButton = JButton("プロジェクト として追加").apply { add(this) }
            val addGroupButton = JButton("グループ として追加").apply { add(this) }

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

@ExperimentalStdlibApi
private fun GitLabApiClient.addGraphFromGroup(data: TimeSeriesCollection, name: String, groupId: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
    GlobalScope.launch {
        // プロジェクトの取得
        val projects = getAllProject(groupId) ?: return@launch onFailure.invoke()

        // グラフに反映
        addGraphFromProject(data, name, projects, onSuccess, onFailure)
    }
}

@ExperimentalStdlibApi
private fun GitLabApiClient.addGraphFromProject(data: TimeSeriesCollection, name: String, project: Project, onSuccess: () -> Unit, onFailure: () -> Unit) {
    addGraphFromProject(data, name, setOf(project), onSuccess, onFailure)
}

@ExperimentalStdlibApi
private fun GitLabApiClient.addGraphFromProject(data: TimeSeriesCollection, name: String, projects: Set<Project>, onSuccess: () -> Unit, onFailure: () -> Unit) {
    GlobalScope.launch {
        data.addSeries(TimeSeries(name).apply {
            // コミットの取得
            val commits = getAllCommits(projects) ?: return@launch onFailure.invoke()

            // コミットをグラフに反映
            val compressDates = commits.compress()
            var sumCommit = 0
            compressDates.forEach { (day, commit) ->
                sumCommit += commit
                add(day, sumCommit.toDouble())
            }
        })
        onSuccess.invoke()
    }
}