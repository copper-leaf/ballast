package com.copperleaf.ballast.debugger.idea.templates

import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory

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
class ExposeOtherTemplates : FileTemplateGroupDescriptorFactory {

    override fun getFileTemplatesDescriptor(): FileTemplateGroupDescriptor {
        return FileTemplateGroupDescriptor("Ballast Android Fragment", null).apply {
            addTemplate(FileTemplateDescriptor("BallastAndroidFragment.kt"))
            addTemplate(FileTemplateDescriptor("BallastAndroidViewModel.kt"))
            addTemplate(FileTemplateDescriptor("BallastUiContract.kt"))
            addTemplate(FileTemplateDescriptor("BallastUiEventHandler.kt"))
            addTemplate(FileTemplateDescriptor("BallastUiInputHandler.kt"))
        }
    }
}
