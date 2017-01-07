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
import spock.lang.AutoCleanup
import spock.lang.Shared

class UnzipIntegrationSpec extends BaseUploadedFileIntegrationSpec {

    private static final String DEFAULT_RESPONSE = 'Deployed using zip'

    @AutoCleanup
    TemporaryRunnableJarHerokuApp herokuApp

    @Shared
    private File artifact

    def setupSpec() {
        artifact = ratpackProjectBuilder.buildDistributionZipRespondingWith(DEFAULT_RESPONSE)
        upload(artifact)
    }

    void withoutPreDeploy() {
        herokuApp = new TemporaryRunnableJarHerokuApp(testConfig.herokuApiKey, UNZIP_ARTIFACT: true.toString())
    }

    void withPreDeploy(Map<String, String> configVars = [:]) {
        herokuApp = new TemporaryRunnableJarHerokuApp([UNZIP_ARTIFACT: true.toString(), NO_PRE_DEPLOY: null] + configVars, testConfig.herokuApiKey)
    }

    def "deploying a distribution zip"() {
        given:
        withoutPreDeploy()
        pluginConfigWithApiKey """
            artifactUrl = '$artifactUrl'
            applicationName = '$herokuApp.name'
            procfileContents = 'web: bin/${ratpackProjectBuilder.PROJECT_NAME}'
        """

        when:
        successfullyRunDeployTask()

        then:
        waitFor { herokuApp.httpClient.text == DEFAULT_RESPONSE }
    }

    def "deploying by pushing a file"() {
        given:
        withoutPreDeploy()
        pluginConfigWithApiKey """
            artifactFile = new File('${artifact.absolutePath}')
            applicationName = '$herokuApp.name'
            procfileContents = 'web: bin/${ratpackProjectBuilder.PROJECT_NAME}'
        """

        when:
        successfullyRunDeployTask()

        then:
        waitFor { herokuApp.httpClient.text == DEFAULT_RESPONSE }
    }

    def "application can declare the pre deploy command to use"() {
        given:
        withPreDeploy(PRE_DEPLOY_COMMAND: "bin/$ratpackProjectBuilder.PROJECT_NAME pre-deploy")
        pluginConfigWithApiKey """
            artifactUrl = '$artifactUrl'
            applicationName = '$herokuApp.name'
        """
        buildFile << '''
            herokuDeploy {
                pushMessageLogLevel = LogLevel.LIFECYCLE
            }
        '''

        when:
        def result = successfullyRunDeployTask()

        then:
        result.output.contains 'Running pre-deploy actions'
    }

}
