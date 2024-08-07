package com.kk.pluginlearn.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Toolkit
import javax.swing.*
import javax.swing.text.AttributeSet
import javax.swing.text.PlainDocument

interface OnEnsureClickListener {
    fun onEnsure(className: String, widgetType: WidgetType)
}

enum class WidgetType {
    page,
    widget,
}

class GetXDialog(project: Project, private val onEnsureClickListener: OnEnsureClickListener) : DialogWrapper(project, true) {

    private val inputField = JTextField()
    private val isPageRadio = JRadioButton("isPage（页面）", true)
    private val isWidgetRadio = JRadioButton("isWidget（控件）")

    init {
        title = "GetX模版代码生成器"
        isModal = true
        init()
    }

    override fun createCenterPanel(): JComponent {
        val dialogPanel = JPanel(BorderLayout())

        // 单选按钮
        val radioPanel = buildWidgetTypeRadioGroup()
        val inputField = buildClassNameField()

        dialogPanel.add(radioPanel, BorderLayout.SOUTH)
        dialogPanel.add(JLabel("请输入类名："), BorderLayout.NORTH)
        dialogPanel.add(inputField, BorderLayout.CENTER)

        return dialogPanel
    }

    private fun buildClassNameField(): JTextField {
        inputField.preferredSize = Dimension(200, 30)
        inputField.document = object : PlainDocument() {
            override fun insertString(offs: Int, str: String?, a: AttributeSet?) {
                if (str != null && str.matches(Regex("[a-zA-Z0-9_]*"))) {
                    super.insertString(offs, str, a)
                }
            }
        }
        return inputField
    }

    private fun buildWidgetTypeRadioGroup(): JPanel {
        val radioPanel = JPanel()
        val buttonGroup = ButtonGroup()
        buttonGroup.add(isPageRadio)
        buttonGroup.add(isWidgetRadio)

        radioPanel.add(JLabel("Widget类型："))
        radioPanel.add(isPageRadio)
        radioPanel.add(isWidgetRadio)
        return radioPanel
    }

    override fun createSouthPanel(): JComponent {
        val buttonPanel = JPanel()
        val confirmButton = JButton("确认")

        confirmButton.addActionListener {
            val input = inputField.text
            if (input.isNotEmpty()) {
                val widgetType = if (isPageRadio.isSelected) WidgetType.page else WidgetType.widget
                onEnsureClickListener.onEnsure(input, widgetType)
                close(OK_EXIT_CODE) // 关闭弹框
            } else {
                JOptionPane.showMessageDialog(null, "输入不能为空")
            }
        }

        buttonPanel.add(confirmButton)
        return buttonPanel
    }


    override fun getPreferredSize(): Dimension {
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        return Dimension(screenSize.width / 5, screenSize.height / 5)
    }


}