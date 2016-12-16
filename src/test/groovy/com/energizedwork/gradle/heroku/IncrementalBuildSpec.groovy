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

import com.energizedwork.gradle.heroku.fixture.TemporaryRunnableJarHerokuApp
import org.junit.Rule
import spock.lang.Shared

import static com.energizedwork.gradle.heroku.HerokuRunnableJarBuildpackPlugin.ASSEMBLE_REPOSITORY_CONTENTS_TASK_NAME
import static com.energizedwork.gradle.heroku.HerokuRunnableJarBuildpackPlugin.DEFAULT_PROCFILE_CONTENTS
import static com.energizedwork.gradle.heroku.HerokuRunnableJarBuildpackPlugin.DEPLOY_TASK_NAME
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class IncrementalBuildSpec extends BaseUploadedFileIntegrationSpec {

    @Rule
    TemporaryRunnableJarHerokuApp herokuApp = new TemporaryRunnableJarHerokuApp(testConfig.herokuApiKey)

    @Shared
    private File artifact

    def setupSpec() {
        artifact = ratpackProjectBuilder.buildRunnableJarRespondingWith('Deployed using runnable jar')
        upload(artifact)
    }

    def "assembleHerokuRepositoryContents is incremental and herokuDeploy is not"() {
        given:
        pluginConfigWithApiKey """
            artifactUrl = '$artifactUrl'
            applicationName = '$herokuApp.name'
        """

        when:
        def buildResult = successfullyRunDeployTask()

        then:
        buildResult.task(":$ASSEMBLE_REPOSITORY_CONTENTS_TASK_NAME").outcome == SUCCESS
        buildResult.task(":$DEPLOY_TASK_NAME").outcome == SUCCESS

        when:
        buildResult = successfullyRunDeployTask()

        then:
        buildResult.task(":$ASSEMBLE_REPOSITORY_CONTENTS_TASK_NAME").outcome == UP_TO_DATE
        buildResult.task(":$DEPLOY_TASK_NAME").outcome == SUCCESS
    }

    def "assembleHerokuRepositoryContents recreates files when it is not up to date"() {
        given:
        pluginConfigWithApiKey """
            artifactFile = new File('${artifact.absolutePath}')
            applicationName = '$herokuApp.name'
            javaVersion = '1.8.0_51'
        """
        def runner = runnerFor(ASSEMBLE_REPOSITORY_CONTENTS_TASK_NAME)

        when:
        runner.build()

        and:
        buildFile << """
            $HerokuRunnableJarBuildpackPlugin.NAME {
                artifactFile = null
                javaVersion = null
                artifactUrl = '$artifactUrl'
            }
        """
        runner.build()

        then:
        buildDirFile('heroku-repository-contents/Procfile').text == DEFAULT_PROCFILE_CONTENTS
        buildDirFile('heroku-repository-contents/manifest.sh').text == "ARTIFACT_URL=$artifactUrl"
        !buildDirFile('heroku-repository-contents/system.properties').exists()
        !buildDirFile('heroku-repository-contents/application.jar').exists()
    }

    private File buildDirFile(String path) {
        new File(testProjectDir.root, "build/$path")
    }

}
