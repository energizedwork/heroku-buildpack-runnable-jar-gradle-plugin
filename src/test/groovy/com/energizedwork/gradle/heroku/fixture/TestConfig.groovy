package com.energizedwork.gradle.heroku.fixture

class TestConfig {

    Properties testConfigProperties

    TestConfig() {
        def testConfigResourceStream = getClass().getResourceAsStream('/test-config.properties')
        testConfigProperties = new Properties()
        testConfigProperties.load(testConfigResourceStream)
    }

    String getHerokuApiKey() {
        testConfigProperties['herokuApiKey']
    }

    String getAwsAccessKey() {
        testConfigProperties['awsAccessKey']
    }

    String getAwsSecretKey() {
        testConfigProperties['awsSecretKey']
    }

}
