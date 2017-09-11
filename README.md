[![Build Status](https://snap-ci.com/energizedwork/heroku-buildpack-runnable-jar-gradle-plugin/branch/master/build_image)](https://snap-ci.com/energizedwork/heroku-buildpack-runnable-jar-gradle-plugin/branch/master)
[![License](https://img.shields.io/badge/license-ASL2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

# heroku-buildpack-runnable-jar-gradle-plugin

This project contains a Gradle plugin for deploying applications using [runnable jar buildpack](https://github.com/energizedwork/heroku-buildpack-runnable-jar) to Heroku.

## Installation

For installation instructions please see [this plugin's page on Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.energizedwork.heroku-buildpack-runnable-jar).

## Usage

### Extension properties

This plugin exposes the following properties through the extension named `herokuBuildpackRunnableJar`:

| Name | Type | Required | Default value | Description |
| --- | --- | --- | --- | --- |
| `artifactUrl` | `String` | No | | The url of the artifact to be downloaded by heroku runnable jar buildpack upon deployment. Only one of `artifactUrl` and `artifactFile` should ever be specified. |
| `artifactFile` | `File` | No | | The file to be uploaded to heroku upon deployment. This property is useful if you want to perform a deployment of a file built locally without having to make it downloadable over HTTP. Has to be a file with either `.zip` or `.jar` extension and will be uploaded as `application.zip` or `application.jar` respectively regardless of the file name. Only one of `artifactUrl` and `artifactFile` should ever be specified. |
| `apiKey`| `String` | Yes | | A Heroku API key which is authorized to access the Heroku application for which the deployment is to be performed. |
| `applicationName` | `String` | No | Project name | The name of the Heroku application for which the deployment is to be performed. The name of the project to which this plugin is applied will be used if unspecified. Has no effect if `gitUrl` is also specified. |
| `gitUrl` | `String` | No | | The `https` url of git repository for the Heroku application for which the deployment is to be performed. By default obtained using Heroku API and based on `applicationName`. Does not need to be specified in most use cases. |
| `procfileContents` | `String` | No | `web: java -jar application.jar` | Allows to specify contents of the `Procfile` file that will be pushed as part of the deployment. The default value should be good enough in most use cases. |
| `javaVersion` | `String` | No | | JVM version to be used by the deployed application. Allowed values are as specified in [Heroku documentation](https://devcenter.heroku.com/articles/java-support#specifying-a-java-version). The latest JVM version available in Heroku will be used if unspecified. |

Example usage:

    herokuRunnableJarBuildpack {
        artifactUrl = "https://example.com/my-runnable-jar.jar"
        applicationName = "cryptic-ocean-8852"
        apiKey = "01234567-89ab-cdef-0123-456789abcdef"
    }

### Tasks

This plugin adds the following main task to the project:
 * `herokuDeploy` - deploys a runnable jar to heroku by creating a temporary git repository, committing all files written by `assembleHerokuRepositoryContents` task to it and force pushing that commit to `master` branch of a git repository for a Heroku application

Other tasks added by this plugin to the project:
 * `assembleHerokuRepositoryContents` - writes all files that need to be pushed to a Heroku git repository to perform a deployment of an application using the [runnable jar buildpack](https://github.com/energizedwork/heroku-buildpack-runnable-jar); 

## Building

### Credentials

To be able to run the tests locally the following project properties need to be configured:
 * `herokuApiKey` - a Heroku API key which is used by tests to create temporary heroku applications,
 * `awsAccessKey` - access key for an AWS account; the account has to be authorised to create S3 buckets as it is used by tests to create temporary S3 buckets,
 * `awsSecretKey` - secret key for the AWS account associated with `awsAccessKey`.

The best way to provide the above is by putting them into a `gradle.properties` file inside of the project directory after cloning the project.
That file is listed in `.gitignore` and therefore will not be committed.

### Importing into IDE

The project is setup to generate IntelliJ configuration files.
Simply run `./gradlew idea` and open the generated `*.ipr` file in IntelliJ.

### Tests

If you import the project into IntelliJ as described above then you can run integration tests even after changing the code without having to perform any manual steps.
They are configured to run in an environment matching the one used when running them using Gradle on the command line.

**NOTE:** Integration tests perform a number of time and network expensive activities:
 * building a simple [Ratpack](https://ratpack.io/) application
 * generating a temporary S3 bucket
 * uploading runnable jar of the application to S3
 * creating temporary applications using Heroku's web API
 * deploying the application to Heroku by pushing git commits

Patience and fast network is advised when running them.

### Checking the build

The project contains some code verification tasks aside from tests so if you wish to run a build matching the one on CI then execute `./gradlew check`. 
