package com.kk.pluginlearn.scriptActions

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.EnvironmentUtil
import com.jetbrains.rd.util.getThrowableText
import com.jetbrains.rd.util.string.printToString
import java.io.File
import java.nio.charset.StandardCharsets


class ScriptActionPerformedHelper(val project: Project) {

    fun actionPerformed(event: AnActionEvent, config: ActionConfig) {
        if (config.shPath == null || config.shPath?.isEmpty() == true) {
            showMessage(event, "请检查脚本路径配置是否正常", "错误")
            return
        }
        runTerminalScript(event, config)
    }


    private fun runTerminalScript(event: AnActionEvent, config: ActionConfig) {
        val project = event.project ?: return
        val basePath = project.basePath ?: return


        val terminal =
            ToolWindowManager.getInstance(project).getToolWindow("Terminal") ?: ToolWindowManager.getInstance(project)
                .registerToolWindow("Terminal", true, ToolWindowAnchor.BOTTOM)

        terminal.activate {
            val consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
            val contentManager = terminal.contentManager
            val content = contentManager.factory.createContent(consoleView.component, config.title(), false)
            contentManager.addContent(content)
            contentManager.setSelectedContent(content)


            // command 构造
            val processCommands = config.processCommands()
            val processBuilder = ProcessBuilder(processCommands)

            // 将系统环境变量
            val env = processBuilder.environment()
            env.putAll(EnvironmentUtil.getEnvironmentMap())
            val fvmFlutterSdkPath = getFlutterSdkPath(project)
            println("fvmFlutterSdkPath:$fvmFlutterSdkPath")
            var envPath = env["PATH"] ?: ""
            if (fvmFlutterSdkPath.isNullOrEmpty()) {
                envPath = updateFlutterSdkPath(envPath, fvmFlutterSdkPath ?: "")
            }
            env["PATH"] = envPath
            var workingDir = basePath
            if (config.needWorkDir) {
                val shPathList = config.shPath?.split(File.separator)

                val newPath = shPathList?.let {
                    val joinToString = it.take(n = it.size - 1).joinToString(separator = File.separator)
                    joinToString
                }
                workingDir += File.separator + newPath
            }
            processBuilder.directory(File(workingDir))
            processBuilder.redirectErrorStream(true)
            consoleView.print(
                "当前执行路径：$workingDir \n",
                ConsoleViewContentType.LOG_INFO_OUTPUT
            )

            try {
                val process = processBuilder.start()
                val processHandler =
                    OSProcessHandler(process, processCommands.joinToString(separator = " "), StandardCharsets.UTF_8)

                consoleView.attachToProcess(processHandler)
                processHandler.addProcessListener(object : ProcessAdapter() {
                    override fun processTerminated(event: ProcessEvent) {
                        // 执行结束
                        consoleView.print("脚本执行完成", ConsoleViewContentType.LOG_INFO_OUTPUT)
                    }
                })
                processHandler.startNotify()
            } catch (e: Exception) {
                consoleView.print("catch：${e.getThrowableText()}", ConsoleViewContentType.ERROR_OUTPUT)
            }
        }
    }

    private fun showMessage(event: AnActionEvent, content: String, title: String) {
        val project = event.project
        Messages.showMessageDialog(
            project, content, title, Messages.getInformationIcon()
        )
    }

    private fun getFlutterSdkPath(project: Project): String? {
//        val fvmLink = File(project.basePath, ".fvm/flutter_sdk")
//        println("fvmLink.exists:"+fvmLink.exists() + "file path: ${fvmLink.absolutePath}")
//        return if (fvmLink.exists()) fvmLink.canonicalPath else null
        val basePath = project.basePath ?: return null
        val vfs = LocalFileSystem.getInstance().findFileByPath(basePath) ?: return null
        val fvmLink = vfs.findChild(".fvm")?.findChild("flutter_sdk")
        println("fvmLink.exists:" + fvmLink?.exists() + "file canonicalPath: ${fvmLink?.canonicalPath} filePath: ${fvmLink?.path}")
        return if (fvmLink != null && fvmLink.exists()) fvmLink.path else null
    }

    private fun updateFlutterSdkPath(existingPath: String, newFlutterSdkPath: String): String {
        val paths = existingPath.split(":").toMutableList() // 使用":"分割PATH
        val flutterRegex = Regex(".*flutter.*bin") // 定义一个正则表达式来匹配包含flutter的路径

        paths.forEachIndexed { index, path ->
            if (path.matches(flutterRegex)) {
                paths[index] = newFlutterSdkPath
            }
        }

        return paths.joinToString(separator = ":")
    }


}