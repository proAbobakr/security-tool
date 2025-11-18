package com.security.apkanalyzer.analyzer

import com.security.apkanalyzer.domain.DexClass
import com.security.apkanalyzer.domain.DexMethod
import com.security.apkanalyzer.domain.DexField
import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.Opcodes
import org.jf.dexlib2.iface.DexFile
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.Field
import java.io.File
import java.util.zip.ZipFile

class DexAnalyzer {

    fun analyzeDexFiles(apkFile: File): List<DexClass> {
        val classes = mutableListOf<DexClass>()

        try {
            // Extract DEX files from APK
            val dexFiles = extractDexFiles(apkFile)

            dexFiles.forEach { dexFile ->
                val dex = DexFileFactory.loadDexFile(dexFile, Opcodes.getDefault())
                dex.classes.forEach { classDef ->
                    classes.add(parseDexClass(classDef))
                }
            }

        } catch (e: Exception) {
            println("Error analyzing DEX files: ${e.message}")
        }

        return classes
    }

    private fun extractDexFiles(apkFile: File): List<File> {
        val dexFiles = mutableListOf<File>()
        val tempDir = createTempDir("dex_analysis")

        try {
            ZipFile(apkFile).use { zip ->
                zip.entries().asSequence()
                    .filter { it.name.endsWith(".dex") }
                    .forEach { entry ->
                        val dexFile = File(tempDir, entry.name)
                        zip.getInputStream(entry).use { input ->
                            dexFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        dexFiles.add(dexFile)
                    }
            }
        } catch (e: Exception) {
            println("Error extracting DEX files: ${e.message}")
        }

        return dexFiles
    }

    private fun parseDexClass(classDef: ClassDef): DexClass {
        val methods = classDef.methods.map { parseDexMethod(it) }
        val fields = classDef.fields.map { parseDexField(it) }

        return DexClass(
            className = classDef.type,
            methods = methods,
            fields = fields,
            superClass = classDef.superclass,
            interfaces = classDef.interfaces.toList()
        )
    }

    private fun parseDexMethod(method: Method): DexMethod {
        val implementation = method.implementation
        val code = if (implementation != null) {
            try {
                implementation.instructions.joinToString("\n") { it.toString() }
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }

        return DexMethod(
            name = method.name,
            descriptor = method.returnType,
            accessFlags = method.accessFlags,
            code = code
        )
    }

    private fun parseDexField(field: Field): DexField {
        return DexField(
            name = field.name,
            type = field.type,
            accessFlags = field.accessFlags
        )
    }

    fun findMethodCalls(classes: List<DexClass>, targetMethod: String): List<String> {
        val callers = mutableListOf<String>()

        classes.forEach { dexClass ->
            dexClass.methods.forEach { method ->
                if (method.code?.contains(targetMethod) == true) {
                    callers.add("${dexClass.className}.${method.name}")
                }
            }
        }

        return callers
    }

    fun findStringUsages(classes: List<DexClass>, searchString: String): List<String> {
        val usages = mutableListOf<String>()

        classes.forEach { dexClass ->
            dexClass.methods.forEach { method ->
                if (method.code?.contains(searchString) == true) {
                    usages.add("${dexClass.className}.${method.name}")
                }
            }
        }

        return usages
    }
}
