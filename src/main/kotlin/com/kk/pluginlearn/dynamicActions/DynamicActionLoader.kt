package com.kk.pluginlearn.dynamicActions

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import java.io.File
import java.io.FileReader

class DynamicActionLoader(project: Project) : DumbAware {

    val actionHelper: ActionPerformedHelper = ActionPerformedHelper(project)

    fun loadActions(project: Project) {
        try {

            val shJsonPath = project.basePath + "/nova_plugin_sh_list.json"

            val gson = Gson()
            val configs: List<ActionConfig> = gson.fromJson(
                FileReader(shJsonPath),
                object : TypeToken<List<ActionConfig?>?>() {}.type
            )
            val actionGroup: DefaultActionGroup =
                ActionManager.getInstance().getAction("NovaMenu") as DefaultActionGroup
            actionGroup.removeAll()

            for (config in configs) {
                val configAction = object : AnAction(config.displayName, config.description, null) {
                    override fun actionPerformed(event: AnActionEvent) {
                        actionHelper.actionPerformed(event, config)
                    }
                }
                ActionManager.getInstance().registerAction(config.actionName ?: "", configAction)
                actionGroup.add(configAction)
            }

        } catch (e: Exception) {
            e.printStackTrace();
        }
    }
}

class ActionConfig {
    var actionName: String? = null
    var shPath: String? = null
    var displayName: String? = null
    var description: String? = null
    var type: String? = null
    var needWorkDir: Boolean = false

    fun title(): String {
        return "${type}-${displayName}"
    }

    fun processCommands(): List<String> {
        val commands = arrayListOf<String>()
        type?.let {
            when (it) {
                "sh" -> {
                    commands.add(it)
                }

                "dart" -> {
                    commands.add(it)
                    commands.add("run")
                }

                "fvm dart" -> {
                    commands.add("fvm")
                    commands.add("dart")
                    commands.add("run")
                }

                else -> {
                    return commands
                }
            }
        }
        shPath?.let {
            if (needWorkDir) {
                val shPathList = it.split(File.separator).last()
                commands.add(shPathList)
            } else {
                commands.add(it)
            }
        }
        return commands
    }
}