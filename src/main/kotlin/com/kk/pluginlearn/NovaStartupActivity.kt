package com.kk.pluginlearn

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.kk.pluginlearn.dynamicActions.DynamicActionLoader

class NovaStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        println("NovaStartupActivity:execute")
        val dynamicActionLoader = DynamicActionLoader(project)
        dynamicActionLoader.loadActions(project)
    }

}