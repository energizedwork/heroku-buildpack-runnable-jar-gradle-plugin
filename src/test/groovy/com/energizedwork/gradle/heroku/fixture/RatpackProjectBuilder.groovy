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

import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class RatpackProjectBuilder implements TestRule {

    public final static String PROJECT_NAME = 'ratpack-app'

    TemporaryFolder testProjectDir = new TemporaryFolder()

    @Override
    Statement apply(Statement base, Description description) {
        testProjectDir.apply(base, description)
    }

    private void runBuild(String task, String responseText) {
        writeBuildFile()
        writeRatpackApplication(responseText)
        writeSettingsFile()

        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(task)
                .build()
    }

    File buildRunnableJarRespondingWith(String responseText) {
        runBuild('shadowJar', responseText)
        new File(testProjectDir.root, "build/libs/$PROJECT_NAME-all.jar")
    }

    File buildDistributionZipRespondingWith(String responseText) {
        runBuild('packageApp', responseText)
        new File(testProjectDir.root, 'build/distributions/main.zip')
    }

    private File writeSettingsFile() {
        testProjectDir.newFile('settings.gradle') << """
            rootProject.name = '$PROJECT_NAME'
        """
    }

    private File writeRatpackApplication(String defaultResponseText) {
        new File(testProjectDir.newFolder('src', 'main', 'java'), 'App.java') << """
            import ratpack.server.RatpackServer;

            public class App {
                public static void main(String[] args) throws Exception {
                    final String response = args.length > 0 ? args[0] : "$defaultResponseText";
                    RatpackServer.start(s ->
                        s.handlers(chain ->
                            chain
                                .get("java-version", ctx -> ctx.render(System.getProperty("java.version")))
                                .all(ctx -> ctx.render(response))
                        )
                    );
                }
            }
        """
    }

    private File writeBuildFile() {
        testProjectDir.newFile('build.gradle') << '''
            plugins {
                id 'io.ratpack.ratpack-java' version '1.4.4'
                id 'com.github.johnrengelman.shadow' version '1.2.4'
            }

            repositories {
                mavenCentral()
            }

            mainClassName = "App"

            task packageApp(type: Zip) {
                archiveName = "main.zip"
                with distributions.main.contents
            }
        '''
    }
}
