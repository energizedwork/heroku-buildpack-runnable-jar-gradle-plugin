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

import com.energizedwork.gradle.heroku.fixture.RatpackProjectBuilder
import com.energizedwork.gradle.heroku.fixture.TemporaryHerokuApp
import com.energizedwork.gradle.heroku.fixture.TemporaryS3Bucket
import com.energizedwork.gradle.heroku.fixture.TestConfig
import geb.waiting.Wait
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.ClassRule
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.genesis.Gen
import spock.lang.Shared
import spock.lang.Specification

import static com.energizedwork.gradle.heroku.HerokuRunnableJarDeploymentPlugin.ASSEMBLE_REPOSITORY_CONTENTS_TASK_NAME
import static com.energizedwork.gradle.heroku.HerokuRunnableJarDeploymentPlugin.DEPLOY_TASK_NAME
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class IntegrationSpec extends Specification {

    private static final String DEFAULT_RESPONSE = 'Deployed using runnable jar'

    private static final String RUNNABLE_JAR_BUILDPACK_URL = 'https://github.com/energizedwork/heroku-buildpack-runnable-jar'

    @Shared
    TestConfig testConfig = new TestConfig()

    @ClassRule
    @Shared
    RatpackProjectBuilder ratpackProjectBuilder = new RatpackProjectBuilder()

    @ClassRule
    @Shared
    TemporaryS3Bucket s3Bucket = new TemporaryS3Bucket(testConfig.awsAccessKey, testConfig.awsSecretKey)

    @Shared
    String artifactUrl

    @Rule
    TemporaryHerokuApp herokuApp = new TemporaryHerokuApp(RUNNABLE_JAR_BUILDPACK_URL, testConfig.herokuApiKey)

    @Rule
    TemporaryFolder testProjectDir

    def setupSpec() {
        artifactUrl = s3Bucket.uploadPublicly(ratpackProjectBuilder.buildAppRespondingWith(DEFAULT_RESPONSE))
    }

    def "deploying when application name is defined"() {
        given:
        pluginConfigWithApiKey """
            artifactUrl = '$artifactUrl'
            applicationName = '$herokuApp.name'
        """

        when:
        successfullyRunDeployTask()

        then:
        waitFor { herokuApp.httpClient.text == DEFAULT_RESPONSE }
    }

    def "deploying when application name defaults to project name"() {
        given:
        projectName = herokuApp.name
        pluginConfigWithApiKey """
            artifactUrl = '$artifactUrl'
        """

        when:
        successfullyRunDeployTask()

        then:
        waitFor { herokuApp.httpClient.text == DEFAULT_RESPONSE }
    }

    def "deploying when heroku git url is specified"() {
        given:
        pluginConfigWithApiKey """
            artifactUrl = '$artifactUrl'
            gitUrl = '$herokuApp.gitUrl'
        """

        when:
        successfullyRunDeployTask()

        then:
        waitFor { herokuApp.httpClient.text == DEFAULT_RESPONSE }
    }

    def "deploying with custom java version"() {
        given:
        pluginConfigWithApiKey """
            artifactUrl = '$artifactUrl'
            applicationName = '$herokuApp.name'
            javaVersion = '$javaVersion'
        """

        when:
        successfullyRunDeployTask()

        then:
        waitFor { herokuApp.httpClient.getText('java-version') == "$javaVersion-cedar14" }

        where:
        javaVersion << Gen.any('1.8.0_51', '1.8.0_72', '1.8.0_92').take(1)
    }

    def "deploying with custom procfile"() {
        given:
        pluginConfigWithApiKey """
            artifactUrl = '$artifactUrl'
            applicationName = '$herokuApp.name'
            procfileContents = 'web: java -jar application.jar "$overriddenResponse"'
        """

        when:
        successfullyRunDeployTask()

        then:
        waitFor { herokuApp.httpClient.text == overriddenResponse }

        where:
        overriddenResponse = 'Overridden using procfile'
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

    def "api key is mentioned in the error if it is not specified and is used to obtain git url of the app"() {
        given:
        pluginConfigWithoutApiKey """
            artifactUrl = '$artifactUrl'
            applicationName = '$herokuApp.name'
        """

        when:
        def buildResult = runDeployTaskWithFailure()

        then:
        buildResult.output.contains('Did you correctly configure Heroku application name and API key?')
    }

    private GradleRunner runnerForDeployTask() {
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(DEPLOY_TASK_NAME)
                .withPluginClasspath()
    }

    private BuildResult successfullyRunDeployTask() {
        runnerForDeployTask().build()
    }

    private BuildResult runDeployTaskWithFailure() {
        runnerForDeployTask().buildAndFail()
    }

    private void pluginConfigWithApiKey(String config) {
        pluginConfigWithoutApiKey("apiKey = '$testConfig.herokuApiKey'", config)
    }

    private void pluginConfigWithoutApiKey(String... config) {
        testProjectDir.newFile('build.gradle') << """
            plugins {
                id 'com.energizedwork.heroku-buildpack-runnable-jar'
            }

            $HerokuRunnableJarDeploymentPlugin.NAME {
                ${config.join('\n')}
            }
        """
    }

    private void setProjectName(String name) {
        testProjectDir.newFile('settings.gradle') << """
            rootProject.name = '$name'
        """
    }

    private <T> T waitFor(Closure<T> block) {
        new Wait(30).waitFor(block)
    }

}
