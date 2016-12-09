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
import com.energizedwork.gradle.heroku.fixture.TemporaryS3Bucket
import com.energizedwork.gradle.heroku.fixture.TestConfig
import geb.waiting.Wait
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.ClassRule
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

abstract class BaseIntegrationSpec extends Specification {

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
    TemporaryFolder testProjectDir

    def setupSpec() {
        artifactUrl = s3Bucket.uploadPublicly(artifactFile)
    }

    abstract File getArtifactFile()

    protected GradleRunner runnerForDeployTask() {
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(HerokuRunnableJarBuildpackPlugin.DEPLOY_TASK_NAME)
                .withPluginClasspath()
    }

    protected BuildResult successfullyRunDeployTask() {
        runnerForDeployTask().build()
    }

    protected BuildResult runDeployTaskWithFailure() {
        runnerForDeployTask().buildAndFail()
    }

    protected void pluginConfigWithApiKey(String config) {
        pluginConfigWithoutApiKey("apiKey = '$testConfig.herokuApiKey'", config)
    }

    protected void pluginConfigWithoutApiKey(String... config) {
        testProjectDir.newFile('build.gradle') << """
            plugins {
                id 'com.energizedwork.heroku-buildpack-runnable-jar'
            }

            $HerokuRunnableJarBuildpackPlugin.NAME {
                ${config.join('\n')}
            }
        """
    }

    protected void setProjectName(String name) {
        testProjectDir.newFile('settings.gradle') << """
            rootProject.name = '$name'
        """
    }

    protected <T> T waitFor(Closure<T> block) {
        new Wait(30).waitFor(block)
    }
}
