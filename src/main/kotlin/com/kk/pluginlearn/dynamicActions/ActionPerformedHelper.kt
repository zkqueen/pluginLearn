package com.kk.pluginlearn.dynamicActions

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets


class ActionPerformedHelper(val project: Project) {

    fun actionPerformed(event: AnActionEvent, config: ActionConfig) {
        if (config.shPath == null || config.shPath?.isEmpty() == true) {
            showMessage(event, "请检查脚本路径配置是否正常", "错误")
            return
        }
        runTerminalScript(event, config)
    }

    private fun shRunScript(event: AnActionEvent, path: String) {
        val processBuilder = ProcessBuilder()
        processBuilder.command("sh", path)
        // 切换到项目目录下
        processBuilder.directory(File(project.basePath!!))
        try {
            val process = processBuilder.start()
            val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))

            var line: String?
            while ((bufferedReader.readLine().also { line = it }) != null) {
                println(line)
            }
            // 等待进程结束
            val exitCode = process.waitFor()
            runResult(exitCode, event, path)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun runResult(
        exitCode: Int,
        event: AnActionEvent,
        path: String
    ) {
        println("Exited with code : $exitCode")
        if (exitCode == 0) {
            showMessage(event, "${path}执行成功", "Success")
        } else {
            showMessage(event, "${path}执行失败，processBuilder错误码为：${exitCode}", "Failed")
        }
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
            val shPathList = config.shPath?.split(File.separator)

            val newPath = shPathList?.let {
                val joinToString = it.take(n = it.size - 1).joinToString(separator = File.separator)
                joinToString
            }

            var workingDir = basePath
            if (config.needWorkDir) {
                workingDir += File.separator + newPath
            }
            processBuilder.directory(File(workingDir))
            consoleView.print(
                "当前执行路径：$workingDir \n",
                ConsoleViewContentType.LOG_INFO_OUTPUT
            )

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
        }
    }

    private fun dartRunScript(event: AnActionEvent, path: String) {
        val project = event.project ?: return
        val basePath = project.basePath ?: return

        val terminal =
            ToolWindowManager.getInstance(project).getToolWindow("Terminal") ?: ToolWindowManager.getInstance(project)
                .registerToolWindow("Terminal", true, ToolWindowAnchor.BOTTOM)

        terminal.activate {
            val consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
            val contentManager = terminal.contentManager
            val content = contentManager.factory.createContent(consoleView.component, "Run Dart Script", false)
            contentManager.addContent(content)
            contentManager.setSelectedContent(content)

            // Build and execute the command
            val processBuilder = ProcessBuilder("fvm", "dart", "run", "$basePath/$path")
            processBuilder.directory(File(basePath))
            val process = processBuilder.start()
            val processHandler = OSProcessHandler(process, "fvm dart run $path", StandardCharsets.UTF_8)

            consoleView.attachToProcess(processHandler)
            processHandler.addProcessListener(object : ProcessAdapter() {
                override fun processTerminated(event: ProcessEvent) {
                    // 执行结束
                    consoleView.print("脚本执行完成", ConsoleViewContentType.LOG_INFO_OUTPUT)
                }
            })
            processHandler.startNotify()
        }
    }

    private fun showMessage(event: AnActionEvent, content: String, title: String) {
        val project = event.project
        Messages.showMessageDialog(
            project, content, title, Messages.getInformationIcon()
        )
    }

}