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
package com.energizedwork.gradle.heroku.task

import groovy.json.JsonSlurper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.RefSpec
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

class HerokuDeploy extends DefaultTask {

    public static final String REMOTE_NAME = 'origin'
    public static final String DESCRIPTION = 'Deploys an application using runnable jar buildpack to Heroku'

    private final static String HEROKU_API_URL = 'https://api.heroku.com'

    @InputFiles
    FileCollection repoContents

    @Input
    String gitUrl

    @Input
    String apiKey

    HerokuDeploy() {
        group = 'Deployment'
        description = DESCRIPTION
    }

    private void commit(Git repo) {
        repo.commit().setMessage('Automated deployment').call()
    }

    private void copyRepoContents() {
        project.copy {
            from repoContents
            into temporaryDir
        }
    }

    private void addRemote(Git repo) {
        def remoteAdd = repo.remoteAdd()
        remoteAdd.name = REMOTE_NAME
        remoteAdd.uri = new URIish(getGitUrl())
        remoteAdd.call()
    }

    private void addAllFiles(Git repo) {
        repo.add().addFilepattern('.').call()
    }

    private void forcePushHeadToRemoteMaster(Git repo) {
        repo.push()
                .setRemote(REMOTE_NAME)
                .setRefSpecs(new RefSpec('HEAD:refs/heads/master'))
                .setForce(true)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider('', getApiKey()))
                .call()
    }

    @TaskAction
    void push() {
        copyRepoContents()
        def repo = Git.init().setDirectory(temporaryDir).call()
        addRemote(repo)
        addAllFiles(repo)
        commit(repo)
        forcePushHeadToRemoteMaster(repo)
    }

    String gitUrlFor(String appName) {
        def client = new OkHttpClient()

        def request = new Request.Builder()
                .url("$HEROKU_API_URL/apps/$appName")
                .header('Accept', 'application/vnd.heroku+json; version=3')
                .header('Authorization', "Bearer ${getApiKey()}")
                .build()

        def response = client.newCall(request).execute()

        if (!response.successful) {
            def message = "Unexpected response from Heroku API when obtaining git url for '$appName': ${response.code()}."
            message += 'Did you correctly configure Heroku application name and API key?'
            throw new RuntimeException(message)
        }

        new JsonSlurper().parseText(response.body().string()).git_url
    }
}
