package com.copperleaf.ballast.debugger.idea.templates

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.util.PlatformIcons.CLASS_ICON
import com.intellij.util.PlatformIcons.SYMLINK_ICON

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
class NewBallastComponent : CreateFileFromTemplateAction(
    "Ballast Component",
    "Creates a new Ballast component",
    SYMLINK_ICON,
) {
    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder
            .setTitle("New My File")
            .addKind("Android Fragment", CLASS_ICON, "Ballast Android Fragment")
            .addKind("Android Activity", CLASS_ICON, "Ballast Android Fragment")
            .addKind("Repository", CLASS_ICON, "Ballast Android Fragment")
    }

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String {
        return "Create Ballast Component: $newName"
    }

    override fun createFile(name: String, templateName: String, dir: PsiDirectory): PsiFile? {
        when (templateName) {
            "Ballast Android Fragment" -> {
                createFileFromTemplate(
                    null,
                    FileTemplateManager
                        .getInstance(dir.project)
                        .getInternalTemplate("\${NAME}Fragment.kt"),
                    dir,
                )
                createFileFromTemplate(
                    null,
                    FileTemplateManager
                        .getInstance(dir.project)
                        .getInternalTemplate("\${NAME}ViewModel.kt"),
                    dir,
                )
            }
        }

        return null
    }
}
