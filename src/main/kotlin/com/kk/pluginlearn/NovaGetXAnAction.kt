package com.kk.pluginlearn

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages

class NovaGetXAnAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        showCurrentSelectedInfoDialog(event)
    }

    /**
     *  使用Messages.showMesssageDialog 输出当前PSI信息
     *  如 ： event.presentation.text 为 Nova 对应的是action注册信息中的text
     *  selectedElement 则为选中文件的导航信息既可以理解为 ： 文件的全路径
     *  event.presentation.description ： 则为插件action中的desc
     *
     *  @notice : selectedElement = event.getData(CommonDataKeys.NAVIGATABLE)
     *  我的核心点在这个路径下生成文件
     *
     */
    private fun showCurrentSelectedInfoDialog(event: AnActionEvent) {
        val project = event.project
        val sbMessage = StringBuilder(event.presentation.text + " Selected !")
        // 文件路径
        val selectedElement = event.getData(CommonDataKeys.NAVIGATABLE)
        if (selectedElement != null) {
            sbMessage.append("\n Selected Element:").append(selectedElement)
        }
        val title = event.presentation.description
        Messages.showMessageDialog(
            project,
            sbMessage.toString(),
            title,
            Messages.getInformationIcon()
        )
    }
}