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

class UnzipIntegrationSpec extends BaseIntegrationSpec {

    private static final String DEFAULT_RESPONSE = 'Deployed using zip'

    @Rule
    TemporaryRunnableJarHerokuApp herokuApp = new TemporaryRunnableJarHerokuApp(testConfig.herokuApiKey, UNZIP_ARTIFACT: 'true')

    @Override
    File getArtifactFile() {
        ratpackProjectBuilder.buildDistributionZipRespondingWith(DEFAULT_RESPONSE)
    }

    def "deploying a distribution zip"() {
        given:
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
}
