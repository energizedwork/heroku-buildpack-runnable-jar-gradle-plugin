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
import org.junit.ClassRule
import spock.lang.Shared

class BaseUploadedFileIntegrationSpec extends BaseIntegrationSpec {

    @ClassRule
    @Shared
    RatpackProjectBuilder ratpackProjectBuilder = new RatpackProjectBuilder()

    @ClassRule
    @Shared
    TemporaryS3Bucket s3Bucket = new TemporaryS3Bucket(testConfig.awsAccessKey, testConfig.awsSecretKey)

    @Shared
    String artifactUrl

    void upload(File file) {
        artifactUrl = s3Bucket.uploadPublicly(file)
    }

}
