package com.energizedwork.gradle.heroku.fixture

import groovy.json.JsonSlurper
import org.junit.rules.ExternalResource
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

class TemporaryHerokuApp extends ExternalResource implements ApplicationUnderTest {

    private final static String HEROKU_API_URL = 'https://api.heroku.com/'

    private final TestHttpClient client = testHttpClient { new URI(HEROKU_API_URL) }

    @Delegate
    private HerokuAppDetails appDetails

    private final String buildpackUrl
    private final String herokuApiKey

    TemporaryHerokuApp(String buildpackUrl, String herokuApiKey) {
        this.buildpackUrl = buildpackUrl
        this.herokuApiKey = herokuApiKey
    }

    protected void before() throws Throwable {
        appDetails = createApp()
        setupBuildpack()
        setupConfigVars()
    }

    protected void after() {
        deleteApp()
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

    private HerokuAppDetails createApp() {
        defaultRequestSpec()
        def response = client.post('apps')
        assert response.statusCode == CREATED.code()
        new HerokuAppDetails(new JsonSlurper().parseText(response.body.text))
    }

    private void jsonRequest(Map<String, Object> payload) {
        requestSpec {
            it.headers {
                it.add(CONTENT_TYPE, APPLICATION_JSON)
            }
            .body {
                it.text(toJson(payload))
            }
        }
    }

    private void setupBuildpack() {
        jsonRequest([
                updates: [
                        [
                                buildpack: buildpackUrl
                        ]
                ]
        ])
        assert client.put("apps/$name/buildpack-installations").statusCode == OK.code()
    }

    private void setupConfigVars() {
        jsonRequest([
                NO_PRE_DEPLOY: 'true'
        ])
        assert client.patch("apps/$name/config-vars").statusCode == OK.code()
    }

    private void deleteApp() {
        if (appDetails) {
            defaultRequestSpec()
            assert client.delete("apps/$name").statusCode == OK.code()
        }
    }

    private static class HerokuAppDetails {

        private final Map<String, Object> appCreationResponse

        HerokuAppDetails(Map<String, Object> appCreationResponse) {
            this.appCreationResponse = appCreationResponse
        }

        String getName() {
            appCreationResponse.name
        }

        String getGitUrl() {
            appCreationResponse.git_url
        }

        URI getAddress() {
            new URI(appCreationResponse.web_url)
        }
    }
}
