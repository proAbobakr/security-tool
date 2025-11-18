package com.security.analyzer

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

abstract class AndroidSecurityAnalyzerTask extends DefaultTask {

    @InputFile
    @Optional
    abstract RegularFileProperty getApkFile()

    @Input
    @Optional
    abstract Property<String> getMinSeverity()

    @Input
    @Optional
    abstract Property<String> getReportFormat()

    @Input
    @Optional
    abstract Property<String> getReportOutputDir()

    @Input
    @Optional
    abstract Property<Boolean> getFailOnError()

    @Input
    @Optional
    abstract Property<String> getCustomRulesFile()

    @OutputDirectory
    File getOutputDir() {
        return project.file(reportOutputDir.get())
    }

    @TaskAction
    void analyze() {
        def apk = apkFile.getOrNull()?.asFile

        if (apk == null || !apk.exists()) {
            logger.warn("No APK file found. Skipping security analysis.")
            return
        }

        logger.lifecycle("Analyzing APK: ${apk.name}")

        // Ensure output directory exists
        def outputDir = getOutputDir()
        outputDir.mkdirs()

        // Build Python command
        def outputFile = new File(outputDir, "${apk.name}.${reportFormat.get()}")

        def command = [
            'python3', '-m', 'analyzer.cli',
            'analyze',
            apk.absolutePath,
            '--output', outputFile.absolutePath,
            '--format', reportFormat.get(),
            '--severity', minSeverity.get()
        ]

        logger.info("Executing: ${command.join(' ')}")

        def process = new ProcessBuilder(command)
            .directory(project.rootDir)
            .redirectErrorStream(true)
            .start()

        def output = new StringBuilder()
        process.inputStream.eachLine { line ->
            logger.lifecycle(line)
            output.append(line).append('\n')
        }

        def exitCode = process.waitFor()

        if (exitCode != 0) {
            logger.error("Security analysis failed with exit code: ${exitCode}")
            logger.error(output.toString())

            if (failOnError.get()) {
                throw new GradleException("Security analysis found critical issues. Check report: ${outputFile}")
            }
        } else {
            logger.lifecycle("Security analysis complete. Report: ${outputFile}")
        }
    }
}
