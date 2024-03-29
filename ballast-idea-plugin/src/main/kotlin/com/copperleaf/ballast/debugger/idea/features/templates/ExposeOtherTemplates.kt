package com.copperleaf.ballast.debugger.idea.features.templates

import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory
import org.jetbrains.kotlin.idea.KotlinIcons

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
        return FileTemplateGroupDescriptor("Ballast", KotlinIcons.MPP).apply {
            addTemplate(FileTemplateDescriptor(BallastUi.UiTemplate.Contract.templateName, BallastUi.UiTemplate.Contract.icon))
            addTemplate(FileTemplateDescriptor(BallastUi.UiTemplate.InputHandler.templateName, BallastUi.UiTemplate.InputHandler.icon))
            addTemplate(FileTemplateDescriptor(BallastUi.UiTemplate.EventHandler.templateName, BallastUi.UiTemplate.EventHandler.icon))
            addTemplate(FileTemplateDescriptor(BallastUi.UiTemplate.SavedStateAdapter.templateName, BallastUi.UiTemplate.SavedStateAdapter.icon))

            BallastRepository.RepositoryTemplate.values().forEach {
                addTemplate(FileTemplateDescriptor(it.templateName, it.icon))
            }

            BallastViewModel.ViewModelTemplate.values().forEach {
                addTemplate(FileTemplateDescriptor(it.templateName, it.icon))
            }
        }
    }
}
