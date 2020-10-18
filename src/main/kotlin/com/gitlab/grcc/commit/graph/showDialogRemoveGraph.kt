package com.gitlab.grcc.commit.graph

import com.gitlab.grcc.commit.graph.api.graph.GraphData
import java.awt.Dimension
import java.awt.Rectangle
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable

@ExperimentalStdlibApi
fun showDialogRemoveGraph(frame: JFrame, data: GraphData) {
    JDialog(frame).apply {
        bounds = Rectangle(450, 420) // ウィンドウサイズを指定
        isResizable = false // サイズ変更を無効化
        setLocationRelativeTo(null) // ウィンドウを中心に配置
        add(JPanel().apply {
            val tableModel = GraphData.TableModel(data)
            val table = JTable(tableModel)
            add(JScrollPane(table).apply {
                preferredSize = Dimension(350, 350)
            })
            add(JButton("選択したプロジェクトを削除").apply {
                addActionListener {
                    while (table.selectedRow != -1) {
                        tableModel.removeRow(table.selectedRow)
                    }
                }
            })
            add(JButton("全削除").apply {
                addActionListener {
                    tableModel.clearRows()
                }
            })
        })
        isVisible = true // ウィンドウを表示
    }
}