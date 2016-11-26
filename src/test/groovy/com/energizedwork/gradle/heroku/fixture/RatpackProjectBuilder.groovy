package com.energizedwork.gradle.heroku.fixture

import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class RatpackProjectBuilder implements TestRule {

    private final static String PROJECT_NAME = 'ratpack-app'

    TemporaryFolder testProjectDir = new TemporaryFolder()

    @Override
    Statement apply(Statement base, Description description) {
        testProjectDir.apply(base, description)
    }

    File buildAppRespondingWith(String responseText) {
        writeBuildFile()
        writeRatpackApplication(responseText)
        writeSettingsFile()

        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('shadowJar')
                .build()

        new File(testProjectDir.root, "build/libs/$PROJECT_NAME-all.jar")
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
        '''
    }
}
