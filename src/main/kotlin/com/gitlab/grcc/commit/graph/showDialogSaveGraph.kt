package com.gitlab.grcc.commit.graph

import org.jfree.chart.ChartPanel
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import java.awt.Desktop
import java.awt.Rectangle
import java.awt.Toolkit
import java.io.File
import java.io.IOException
import javax.swing.InputVerifier
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.UIManager
import javax.swing.WindowConstants.DISPOSE_ON_CLOSE
import javax.swing.filechooser.FileNameExtensionFilter

fun showDialogSaveGraph(frame: JFrame, chart: JFreeChart, chartPanel: ChartPanel) {
    JDialog(frame).apply {
        bounds = Rectangle(450, 130) // ウィンドウサイズを指定
        isResizable = false // サイズ変更を無効化
        setLocationRelativeTo(null) // ウィンドウを中心に配置
        add(JPanel().apply {
            add(JLabel("横幅"))
            val widthTextField = add(JTextField(12).apply {
                text = chartPanel.width.toString()
                inputVerifier = IntegerInputVerifier()
            }) as JTextField
            add(JLabel("高さ"))
            val heightTextField = add(JTextField(12).apply {
                text = chartPanel.height.toString()
                inputVerifier = IntegerInputVerifier()
            }) as JTextField

            fun saveImageAction(extension: String, saveAction: (File, Int, Int) -> Unit) {
                val fileChooser = JFileChooser().apply {
                    fileFilter = FileNameExtensionFilter("*.$extension", extension)
                }
                val result = fileChooser.showSaveDialog(frame)
                if (result != JFileChooser.APPROVE_OPTION) return
                val file = fileChooser.selectedFile
                try {
                    val filePath = if (file.path.endsWith(".$extension", true)) {
                        file.path
                    } else {
                        file.path + ".$extension"
                    }
                    val width = widthTextField.text?.toIntOrNull() ?: return
                    val height = heightTextField.text?.toIntOrNull() ?: return
                    val savedFile = File(filePath)
                    saveAction.invoke(savedFile, width, height)
                    Desktop.getDesktop().open(savedFile)
                    dispose()
                } catch (ex: IOException) {
                    return
                }
            }

            add(JButton("PNG で保存").apply {
                addActionListener {
                    saveImageAction("png") { file: File, width: Int, height: Int ->
                        ChartUtils.saveChartAsPNG(file, chart, width, height)
                    }
                }
            })
            add(JButton("JPEG で保存").apply {
                addActionListener {
                    saveImageAction("jpg") { file: File, width: Int, height: Int ->
                        ChartUtils.saveChartAsJPEG(file, chart, width, height)
                    }
                }
            })
        })
        defaultCloseOperation = DISPOSE_ON_CLOSE // 終了時に破棄する
        isModal = true // メインウィンドウを操作できなくする
        isVisible = true // ウィンドウを表示
    }
}

internal class IntegerInputVerifier: InputVerifier() {
    override fun verify(input: JComponent): Boolean {
        return if ((input as JTextField).text.toIntOrNull() != null) {
            true
        } else {
            UIManager.getLookAndFeel().provideErrorFeedback(input)
            Toolkit.getDefaultToolkit().beep()
            false
        }
    }
}