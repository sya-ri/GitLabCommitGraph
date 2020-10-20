package com.gitlab.grcc.commit.graph

import com.gitlab.grcc.commit.graph.api.gitlab.Commit.Companion.compress
import com.gitlab.grcc.commit.graph.api.gitlab.Project
import com.gitlab.grcc.commit.graph.api.gitlab.getAllCommits
import com.gitlab.grcc.commit.graph.api.gitlab.getAllProject
import com.gitlab.grcc.commit.graph.api.graph.GraphData
import com.gitlab.grcc.commit.graph.api.http.GitLabApiClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.awt.Rectangle
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.WindowConstants
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

private var workingJob: Job? = null

@ExperimentalStdlibApi
fun showDialogAddGraph(frame: JFrame, data: GraphData, client: GitLabApiClient) {
    JDialog(frame).apply {
        bounds = Rectangle(450, 130) // ウィンドウサイズを指定
        isResizable = false // サイズ変更を無効化
        setLocationRelativeTo(null) // ウィンドウを中心に配置
        add(JPanel().apply {
            add(JLabel("名前"))
            val nameTextField = add(JTextField(32)) as JTextField
            add(JLabel("URL"))
            val urlTextField = add(JTextField(32)) as JTextField
            val addProjectButton = add(JButton("プロジェクト として追加")) as JButton
            val addGroupButton = add(JButton("グループ として追加")) as JButton

            fun addGraphAction(action: (nameText: String, groupId: String) -> Unit) {
                val nameText = nameTextField.text
                if (nameText.isNullOrBlank()) return
                val urlText = urlTextField.text ?: return
                val groupId = urlText.removePrefix("https://gitlab.com/").removeSuffix("/")
                addProjectButton.isEnabled = false
                addGroupButton.isEnabled = false
                action.invoke(nameText, groupId)
            }

            nameTextField.document.addDocumentListener(object: DocumentListener {
                override fun changedUpdate(e: DocumentEvent) {
                    val text = e.document.getText(0, e.document.length) ?: return
                    val isEnabled = data.containsSeries(text).not()
                    addProjectButton.isEnabled = isEnabled
                    addGroupButton.isEnabled = isEnabled
                }

                override fun insertUpdate(e: DocumentEvent) {
                    changedUpdate(e)
                }

                override fun removeUpdate(e: DocumentEvent) {
                    changedUpdate(e)
                }
            })

            addProjectButton.addActionListener {
                addGraphAction { nameText, groupId ->
                    client.addGraphFromProject(data, nameText, Project(nameText, groupId))
                }
            }

            addGroupButton.addActionListener {
                addGraphAction { nameText, groupId ->
                    client.addGraphFromGroup(data, nameText, groupId)
                }
            }
        })
        addWindowListener(object: WindowAdapter() {
            override fun windowClosed(e: WindowEvent) {
                workingJob?.let {
                    it.cancel()
                    JOptionPane.showMessageDialog(frame, "キャンセルされました", "", JOptionPane.PLAIN_MESSAGE)
                }
            }
        })
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE // 終了時に破棄する
        isModal = true // メインウィンドウを操作できなくする
        isVisible = true // ウィンドウを表示
    }
}

@ExperimentalStdlibApi
private fun GitLabApiClient.addGraphFromGroup(data: GraphData, name: String, groupId: String) {
    workingJob = GlobalScope.launch {
        // プロジェクトの取得
        val projects = getAllProject(groupId)
        workingJob = null
        if(projects == null) return@launch

        // グラフに反映
        addGraphFromProject(data, name, projects)
    }
}

@ExperimentalStdlibApi
private fun GitLabApiClient.addGraphFromProject(data: GraphData, name: String, project: Project) {
    addGraphFromProject(data, name, setOf(project))
}

@ExperimentalStdlibApi
private fun GitLabApiClient.addGraphFromProject(data: GraphData, name: String, projects: Set<Project>) {
    workingJob = GlobalScope.launch {
        // コミットの取得
        val commits = getAllCommits(projects)
        workingJob = null
        if(commits == null) return@launch

        data.addSeries(name) {
            // コミットをグラフに反映
            val compressDates = commits.compress()
            var sumCommit = 0
            compressDates.forEach { (day, commit) ->
                sumCommit += commit
                add(day, sumCommit.toDouble())
            }
        }
    }
}