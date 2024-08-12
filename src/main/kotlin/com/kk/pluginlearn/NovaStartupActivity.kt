package com.kk.pluginlearn

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.kk.pluginlearn.scriptActions.ScriptActionLoader

class NovaStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        val scriptActionLoader = ScriptActionLoader(project)
        scriptActionLoader.loadActions(project)
    }

}