package com.copperleaf.ballast.debugger.idea.base

import com.intellij.ide.actions.CreateFileAction
import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsActions
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import java.util.Properties
import javax.swing.Icon

/**
 * Provide a set of File Templates for creating new Ballast components for:
 *   - Android Activity (XML Views)
 *   - Android Activity (Compose)
 *   - Android Fragment (XML Views)
 *   - Android Fragment (Compose)
 *   - Compose Desktop Window
 *   - IntelliJ Plugin Tool Window
 *   - Repository
 *
 * See https://plugins.jetbrains.com/docs/intellij/templates.html
 *     https://plugins.jetbrains.com/docs/intellij/using-file-templates.html#custom-create-file-from-template-actions
 */
abstract class BaseTemplateCreator<T : BaseTemplateCreator.TemplateKind>(
    @NlsActions.ActionText text: String,
    @NlsActions.ActionDescription description: String,
    icon: Icon
) : CreateFileFromTemplateAction(text, description, icon), DumbAware {

    interface TemplateKind {
        val templateName: String
        val fileNameSuffix: String
        val icon: Icon

        fun getTemplate(project: Project): FileTemplate {
            return FileTemplateManager.getInstance(project).getInternalTemplate(templateName)
        }

        fun getActualFileName(featureName: String): String {
            return "${featureName}${fileNameSuffix}"
        }
    }

    abstract fun parseTemplateName(templateName: String): List<T>

    final override fun createFileFromTemplate(name: String?, template: FileTemplate, dir: PsiDirectory): PsiFile? {
        val (actualName, actualDir) = if (name != null) {
            val mkdirs = CreateFileAction.MkDirs(name, dir)
            mkdirs.newName to mkdirs.directory
        } else {
            name to dir
        }

        val featureName = FileUtilRt.getNameWithoutExtension(actualName!!)

        val templateKinds = parseTemplateName(template.name)

        templateKinds.forEach { templateKind ->
            createFileFromTemplate(
                featureName = featureName,
                templateKind = templateKind,
                actualDir = actualDir,
            )
        }

        return null
    }

    private fun createFileFromTemplate(
        featureName: String,
        templateKind: T,
        actualDir: PsiDirectory,
    ): PsiFile? {
        val project = actualDir.project

        val psiFile = FileTemplateUtil
            .createFromTemplate(
                templateKind.getTemplate(project),
                templateKind.getActualFileName(featureName),
                Properties(FileTemplateManager.getInstance(project).defaultProperties).apply {
                    this["featureName"] = featureName
                },
                actualDir
            )
            .containingFile

        val pointer = SmartPointerManager.getInstance(project).createSmartPsiElementPointer(psiFile)
        val virtualFile = psiFile.virtualFile
        if (virtualFile != null) {
            FileEditorManager.getInstance(project).openFile(virtualFile, true)
            return pointer.element
        }

        return null
    }

    protected fun CreateFileFromTemplateDialog.Builder.addTemplate(
        name: String,
        templateKind: T,
    ): CreateFileFromTemplateDialog.Builder {
        return this.addKind(name, templateKind.icon, templateKind.templateName)
    }
}
