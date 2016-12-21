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

class ErrorsSpec extends BaseIntegrationSpec {

    @AutoCleanup
    TemporaryRunnableJarHerokuApp herokuApp

    def "api key is mentioned in the error if it is not specified and is used to obtain git url of the app"() {
        given:
        pluginConfigWithoutApiKey """
            artifactUrl = 'https://example.com'
        """

        when:
        def buildResult = runDeployTaskWithFailure()

        then:
        buildResult.output.contains('Did you correctly configure Heroku application name and API key?')
    }

    def "setting both url artifact url and file results in an error"() {
        given:
        pluginConfigWithoutApiKey """
            artifactUrl = 'https://example.com'
            artifactFile = new File('${testProjectDir.newFile('application.jar')}')
        """

        when:
        def buildResult = runDeployTaskWithFailure()

        then:
        buildResult.output.contains('Only one of artifactFile and artifactUrl properties should be set but both were.')
    }

    def "using a file that is neither a zip or a jar as the artifact results in an error"() {
        given:
        pluginConfigWithoutApiKey """
            artifactFile = new File('${testProjectDir.newFile('application.txt')}')
        """

        when:
        def buildResult = runDeployTaskWithFailure()

        then:
        buildResult.output.contains('Only .jar and .zip extensions are allowed for the artifact file.')
    }

    def "build fails when push to heroku is not successful"() {
        given:
        herokuApp = new TemporaryRunnableJarHerokuApp(testConfig.herokuApiKey, NO_PRE_DEPLOY: null)
        pluginConfigWithApiKey """
            artifactUrl = 'not-a-url'
            applicationName = '$herokuApp.name'
        """

        when:
        def result = runDeployTaskWithFailure()

        then:
        result.output.contains 'Unexpected git remote update status'
    }

}
