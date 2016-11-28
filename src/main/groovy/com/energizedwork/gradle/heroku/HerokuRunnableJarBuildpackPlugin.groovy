/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.energizedwork.gradle.heroku

import com.energizedwork.gradle.heroku.task.AssembleHerokuRepositoryContents
import com.energizedwork.gradle.heroku.task.HerokuDeploy
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class HerokuRunnableJarBuildpackPlugin implements Plugin<Project> {

    public static final String NAME = 'herokuRunnableJarBuildpack'
    public static final String ASSEMBLE_REPOSITORY_CONTENTS_TASK_NAME = 'assembleHerokuRepositoryContents'
    public static final String DEPLOY_TASK_NAME = 'herokuDeploy'
    public static final String DEFAULT_PROCFILE_CONTENTS = 'web: java -jar application.jar'

    protected HerokuRunnableJarBuildpackPluginExtension pluginExtension

    void apply(Project project) {
        pluginExtension = project.extensions.create(NAME, HerokuRunnableJarBuildpackPluginExtension)

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
