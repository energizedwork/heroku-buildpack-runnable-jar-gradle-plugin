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
package com.energizedwork.gradle.heroku.fixture

import groovy.json.JsonSlurper
import ratpack.func.Action
import ratpack.http.client.RequestSpec
import ratpack.test.ApplicationUnderTest
import ratpack.test.http.TestHttpClient

import static groovy.json.JsonOutput.toJson
import static io.netty.handler.codec.http.HttpHeaderNames.*
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON
import static io.netty.handler.codec.http.HttpResponseStatus.CREATED
import static io.netty.handler.codec.http.HttpResponseStatus.OK
import static ratpack.test.http.TestHttpClient.testHttpClient

class TemporaryRunnableJarHerokuApp implements ApplicationUnderTest, Closeable {

    private static final String RUNNABLE_JAR_BUILDPACK_URL = 'https://github.com/energizedwork/heroku-buildpack-runnable-jar'

    private final static String HEROKU_API_URL = 'https://api.heroku.com/'

    private final TestHttpClient client = testHttpClient { new URI(HEROKU_API_URL) }

    private final Map<String, String> configVars

    private Map<String, Object> appCreationResponse

    private final String herokuApiKey

    TemporaryRunnableJarHerokuApp(Map<String, String> configVars = [:], String herokuApiKey) {
        this.herokuApiKey = herokuApiKey
        this.configVars = [NO_PRE_DEPLOY: 'true'] + configVars
    }

    void close() {
        deleteApp()
    }

    String getName() {
        ensureStarted()
        appCreationResponse.name
    }

    String getGitUrl() {
        ensureStarted()
        appCreationResponse.git_url
    }

    URI getAddress() {
        ensureStarted()
        new URI(appCreationResponse.web_url)
    }

    private void ensureStarted() {
        if (!appCreationResponse) {
            appCreationResponse = createApp()
            setupBuildpack()
            setupConfigVars()
        }
    }

    private void defaultRequestSpec() {
        requestSpec {}
    }

    private void requestSpec(Action<? super RequestSpec> requestAction) {
        client.requestSpec(Action.join({
            it.headers {
                it.add(ACCEPT, 'application/vnd.heroku+json; version=3')
                        .add(AUTHORIZATION, "Bearer $herokuApiKey")
            }
        } as Action<RequestSpec>, requestAction))
    }

    private Map<String, Object> createApp() {
        defaultRequestSpec()
        def response = client.post('apps')
        assert response.statusCode == CREATED.code()
        new JsonSlurper().parseText(response.body.text)
    }

    private void jsonRequest(Map<String, Object> payload) {
        requestSpec {
            it.headers {
                it.add(CONTENT_TYPE, APPLICATION_JSON)
            }
            .body {
                it.text(toJson(payload.findAll { it.value }))
            }
        }
    }

    private void setupBuildpack() {
        jsonRequest([
                updates: [
                        [
                                buildpack: RUNNABLE_JAR_BUILDPACK_URL
                        ]
                ]
        ])
        assert client.put("apps/$name/buildpack-installations").statusCode == OK.code()
    }

    private void setupConfigVars() {
        jsonRequest(configVars)
        assert client.patch("apps/$name/config-vars").statusCode == OK.code()
    }

    private void deleteApp() {
        if (appCreationResponse) {
            defaultRequestSpec()
            assert client.delete("apps/$name").statusCode == OK.code()
        }
    }
}
