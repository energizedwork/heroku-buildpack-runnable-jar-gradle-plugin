package com.energizedwork.gradle.heroku

import com.energizedwork.gradle.heroku.task.AssembleHerokuRepositoryContents
import com.energizedwork.gradle.heroku.task.HerokuDeploy
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class HerokuRunnableJarDeploymentPlugin implements Plugin<Project> {

    public static final String NAME = 'herokuRunnableJarDeployment'
    public static final String ASSEMBLE_REPOSITORY_CONTENTS_TASK_NAME = 'assembleHerokuRepositoryContents'
    public static final String DEPLOY_TASK_NAME = 'herokuDeploy'
    public static final String DEFAULT_PROCFILE_CONTENTS = 'web: java -jar application.jar'

    protected HerokuRunnableJarDeploymentPluginExtension pluginExtension

    void apply(Project project) {
        pluginExtension = project.extensions.create(NAME, HerokuRunnableJarDeploymentPluginExtension)

        Task assembleHerokuRepositoryContents = addAssembleHerokuRepositoryContentsTask(project)
        addHerokuDeployTask(project, assembleHerokuRepositoryContents)
    }

    Task addAssembleHerokuRepositoryContentsTask(Project project) {
        project.task(ASSEMBLE_REPOSITORY_CONTENTS_TASK_NAME, type: AssembleHerokuRepositoryContents) {
            conventionMapping.procfileContents = {
                pluginExtension.procfileContents ?: DEFAULT_PROCFILE_CONTENTS
            }
            conventionMapping.artifactUrl = { pluginExtension.artifactUrl }
            conventionMapping.javaVersion = { pluginExtension.javaVersion }
        }
    }

    void addHerokuDeployTask(Project project, Task assembleHerokuRepositoryContents) {
        project.task(DEPLOY_TASK_NAME, type: HerokuDeploy) { HerokuDeploy task ->
            repoContents = assembleHerokuRepositoryContents.outputs.files
            conventionMapping.apiKey = { pluginExtension.apiKey }
            conventionMapping.gitUrl = {
                String url = pluginExtension.gitUrl
                url ?: task.gitUrlFor(pluginExtension.applicationName ?: project.name)
            }
        }
    }
}
