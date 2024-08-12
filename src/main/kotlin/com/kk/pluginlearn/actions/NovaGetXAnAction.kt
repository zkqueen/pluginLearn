package com.kk.pluginlearn.actions

import FileTemplateGenerator
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import com.kk.pluginlearn.ui.GetXDialog
import com.kk.pluginlearn.ui.OnEnsureClickListener
import com.kk.pluginlearn.ui.WidgetType

class NovaGetXAnAction : AnAction(), OnEnsureClickListener {

    private var psiDirectory: PsiDirectory? = null
    override fun actionPerformed(event: AnActionEvent) {
        showGetXDialog(event)
    }

    /**
     *  展示弹框
     */
    private fun showGetXDialog(event: AnActionEvent) {

        event.project?.let { p ->
            val dataContext = event.dataContext
            CommonDataKeys.VIRTUAL_FILE.getData(dataContext)?.let { vf ->
                psiDirectory = PsiManager.getInstance(p).findDirectory(vf)
                GetXDialog(event.project!!, this@NovaGetXAnAction).showAndGet()
            }
        }
    }


    override fun onEnsure(className: String, widgetType: WidgetType) {
        // 这里需要在对应的文档结构中生成文件
        psiDirectory?.let {
            FileTemplateGenerator(className, widgetType, it).generateFiles()
        }
    }
}

///**
// *  使用Messages.showMesssageDialog 输出当前PSI信息
// *  如 ： event.presentation.text 为 Nova 对应的是action注册信息中的text
// *  selectedElement 则为选中文件的导航信息既可以理解为 ： 文件的全路径
// *  event.presentation.description ： 则为插件action中的desc
// *
// *  @notice : selectedElement = event.getData(CommonDataKeys.NAVIGATABLE)
// *  我的核心点在这个路径下生成文件
// *
// */
//private fun showCurrentSelectedInfoDialog(event: AnActionEvent) {
//    val project = event.project
//    val sbMessage = StringBuilder(event.presentation.text + " Selected !")
//    // 文件路径
//    val selectedElement = event.getData(CommonDataKeys.NAVIGATABLE)
//    if (selectedElement != null) {
//        sbMessage.append("\n Selected Element:").append(selectedElement)
//    }
//    val title = event.presentation.description
//    Messages.showMessageDialog(
//        project,
//        sbMessage.toString(),
//        title,
//        Messages.getInformationIcon()
//    )
//}