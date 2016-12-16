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
import spock.genesis.Gen
import spock.lang.Shared

class IntegrationSpec extends BaseUploadedFileIntegrationSpec {

    private static final String DEFAULT_RESPONSE = 'Deployed using runnable jar'

    @Rule
    TemporaryRunnableJarHerokuApp herokuApp = new TemporaryRunnableJarHerokuApp(testConfig.herokuApiKey)

    @Shared
    private File artifact

    def setupSpec() {
        artifact = ratpackProjectBuilder.buildRunnableJarRespondingWith(DEFAULT_RESPONSE)
        upload(artifact)
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

    def "deploying by pushing a file"() {
        given:
        pluginConfigWithApiKey """
            artifactFile = new File('${artifact.absolutePath}')
            applicationName = '$herokuApp.name'
        """

        when:
        successfullyRunDeployTask()

        then:
        waitFor { herokuApp.httpClient.text == DEFAULT_RESPONSE }
    }
}
