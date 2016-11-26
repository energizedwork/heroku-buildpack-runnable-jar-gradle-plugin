import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class WriteTestConfig extends DefaultTask {

    @OutputDirectory
    File generatedTestResourcesDir

    @Input
    Properties testProperties = new Properties()

    File getTestConfigPropertiesFile() {
        new File(generatedTestResourcesDir, 'test-config.properties')
    }

    void testConfig(Map<String, String> config) {
        testProperties.putAll(config)
    }

    @TaskAction
    def generate() {
        testConfigPropertiesFile.withOutputStream { testProperties.store(it, null) }
    }
}