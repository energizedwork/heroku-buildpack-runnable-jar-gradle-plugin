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

import com.energizedwork.gradle.heroku.task.AssembleHerokuRepositoryContents
import com.energizedwork.gradle.heroku.task.HerokuDeploy
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static com.energizedwork.gradle.heroku.HerokuRunnableJarDeploymentPlugin.ASSEMBLE_REPOSITORY_CONTENTS_TASK_NAME
import static com.energizedwork.gradle.heroku.HerokuRunnableJarDeploymentPlugin.DEPLOY_TASK_NAME

class DocumentationSpec extends Specification {

    @Rule
    TemporaryFolder testProjectDir

    def "plugin tasks are printed when obtaining project tasks"() {
        given:
        testProjectDir.newFile('build.gradle') << '''
            plugins {
                id 'com.energizedwork.heroku-buildpack-runnable-jar'
            }
        '''

        when:
        def output = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('tasks')
                .withPluginClasspath()
                .build().output

        then:
        output.contains("$ASSEMBLE_REPOSITORY_CONTENTS_TASK_NAME - $AssembleHerokuRepositoryContents.DESCRIPTION")
        output.contains("$DEPLOY_TASK_NAME - $HerokuDeploy.DESCRIPTION")
    }

}
