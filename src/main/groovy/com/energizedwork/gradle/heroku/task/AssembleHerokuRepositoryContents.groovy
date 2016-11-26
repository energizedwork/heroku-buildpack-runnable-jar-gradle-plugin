package com.energizedwork.gradle.heroku.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class AssembleHerokuRepositoryContents extends DefaultTask {

    @Input
    String procfileContents

    @Input
    String artifactUrl

    @Input
    @Optional
    String javaVersion

    @OutputDirectory
    File repositoryContentsDir

    AssembleHerokuRepositoryContents() {
        repositoryContentsDir = new File(project.buildDir, 'heroku-repository-contents')
    }

    @TaskAction
    void assemble() {
        new File(repositoryContentsDir, 'Procfile') << getProcfileContents()
        new File(repositoryContentsDir, 'manifest.sh') << """ARTIFACT_URL=${getArtifactUrl()}"""
        if (getJavaVersion()) {
            new File(repositoryContentsDir, 'system.properties') << "java.runtime.version=${getJavaVersion()}"
        }
    }
}
