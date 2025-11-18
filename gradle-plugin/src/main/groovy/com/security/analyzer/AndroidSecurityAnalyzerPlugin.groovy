package com.security.analyzer

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

class AndroidSecurityAnalyzerPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        // Create extension for configuration
        def extension = project.extensions.create('securityAnalyzer', SecurityAnalyzerExtension)

        // Register the analysis task
        TaskProvider<AndroidSecurityAnalyzerTask> analyzerTask = project.tasks.register(
            'analyzeSecurityFindings',
            AndroidSecurityAnalyzerTask
        ) {
            group = 'verification'
            description = 'Analyze APK for security vulnerabilities using OWASP guidelines'

            // Configure task with extension values
            minSeverity.set(extension.minSeverity)
            reportFormat.set(extension.reportFormat)
            reportOutputDir.set(extension.reportOutputDir)
            failOnError.set(extension.failOnError)
            customRulesFile.set(extension.customRulesFile)
        }

        // Hook into Android build process
        project.afterEvaluate {
            if (project.plugins.hasPlugin('com.android.application')) {
                project.android.applicationVariants.all { variant ->
                    def assembleTask = project.tasks.findByName("assemble${variant.name.capitalize()}")
                    if (assembleTask) {
                        analyzerTask.configure {
                            dependsOn assembleTask
                            apkFile.set(variant.outputs.first().outputFile)
                        }
                    }
                }
            }
        }
    }
}

class SecurityAnalyzerExtension {
    String minSeverity = 'MEDIUM'
    String reportFormat = 'html'
    String reportOutputDir = 'build/reports/security'
    boolean failOnError = false
    String customRulesFile = null

    // Specific checks to enable/disable
    boolean checkPermissions = true
    boolean checkDebuggable = true
    boolean checkBackup = true
    boolean checkNetworkSecurity = true
    boolean checkExportedComponents = true
    boolean checkCryptography = true
    boolean checkHardcodedSecrets = true
    boolean checkObfuscation = true
}
