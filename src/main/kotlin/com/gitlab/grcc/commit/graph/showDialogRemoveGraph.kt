package com.gitlab.grcc.commit.graph

import com.gitlab.grcc.commit.graph.api.graph.GraphData
import java.awt.Rectangle
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.table.DefaultTableModel

@ExperimentalStdlibApi
fun showDialogRemoveGraph(frame: JFrame, data: GraphData) {
    JDialog(frame).apply {
        bounds = Rectangle(450, 130) // ウィンドウサイズを指定
        isResizable = false // サイズ変更を無効化
        setLocationRelativeTo(null) // ウィンドウを中心に配置
        add(JPanel().apply {
            val seriesNameList = data.seriesNameList.map { arrayOf(it) }.toTypedArray()
            val tableModel = DefaultTableModel(seriesNameList, arrayOf("グラフ名"))
            val table = add(JTable(tableModel)) as JTable
            add(JButton("選択したプロジェクトを削除").apply {
                addActionListener {
                    val selectedRow = table.selectedRow
                    if (selectedRow != -1) {
                        data.removeSeries(tableModel.getValueAt(selectedRow, 0) as String)
                        tableModel.removeRow(selectedRow)
                    }
                }
            })
        })
        isVisible = true // ウィンドウを表示
    }
}