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
package com.energizedwork.gradle.heroku.task

import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.*

class AssembleHerokuRepositoryContents extends DefaultTask {

    public static final String DESCRIPTION = 'Writes files needed to perform a deployment of an application using the runnable jar buildpack to Heroku'

    @Input
    String procfileContents

    @Input
    @Optional
    String artifactUrl

    @InputFile
    @Optional
    File artifactFile

    @Input
    @Optional
    String javaVersion

    @OutputDirectory
    File repositoryContentsDir

    AssembleHerokuRepositoryContents() {
        repositoryContentsDir = new File(project.buildDir, 'heroku-repository-contents')
        group = 'Deployment'
        description = DESCRIPTION
    }

    @TaskAction
    void assemble() {
        if (getArtifactFile() && getArtifactUrl()) {
            throw new InvalidUserDataException('Only one of artifactFile and artifactUrl properties should be set but both were.')
        }
        new File(repositoryContentsDir, 'Procfile') << getProcfileContents()
        new File(repositoryContentsDir, 'manifest.sh') << (getArtifactUrl() ? """ARTIFACT_URL=${getArtifactUrl()}""" : '')
        if (getJavaVersion()) {
            new File(repositoryContentsDir, 'system.properties') << "java.runtime.version=${getJavaVersion()}"
        }
        if (getArtifactFile()) {
            new File(repositoryContentsDir, "application${artifactExtension()}") << getArtifactFile().newInputStream()
        }
    }

    private String artifactExtension() {
        def name = getArtifactFile().name
        def extension = name[name.lastIndexOf('.')..-1]
        if (!['.zip', '.jar'].contains(extension)) {
            throw new InvalidUserDataException('Only .jar and .zip extensions are allowed for the artifact file.')
        }
        extension

    }
}
