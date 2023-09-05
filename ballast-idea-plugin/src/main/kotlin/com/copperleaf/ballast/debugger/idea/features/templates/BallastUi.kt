package com.copperleaf.ballast.debugger.idea.features.templates

import com.copperleaf.ballast.debugger.idea.BallastIdeaPlugin
import com.copperleaf.ballast.debugger.idea.base.BaseTemplateCreator
import com.copperleaf.ballast.debugger.idea.settings.IntellijPluginSettingsSnapshot
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import org.jetbrains.kotlin.idea.KotlinIcons
import javax.swing.Icon

/**
 * Provide a set of File Templates for creating new Ballast components for the following types:
 *   - Contract
 *   - InputHandler
 *   - EventHandler
 *   - SavedStateAdapter
 *   - ViewModel
 *
 * See https://plugins.jetbrains.com/docs/intellij/templates.html
 *     https://plugins.jetbrains.com/docs/intellij/using-file-templates.html#custom-create-file-from-template-actions
 */
class BallastUi : BaseTemplateCreator<BallastUi.UiTemplate>(
    "Ballast UI component",
    "Creates new components for Ballast UIs",
    KotlinIcons.MPP,
), DumbAware {
    private val defaultViewModelItem = UiTemplate.ViewModel("BasicViewModel")

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String {
        return "Create Ballast UI component: $newName"
    }

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder
            .setTitle("New Ballast UI Component")
            .addTemplate(UiTemplate.Contract)
            .addTemplate(UiTemplate.InputHandler)
            .addTemplate(UiTemplate.EventHandler)
            .addTemplate(UiTemplate.SavedStateAdapter)
            .addTemplate(defaultViewModelItem)
            .addKind("All components", KotlinIcons.SCRIPT, "Internal_Ui_All")
    }

    override fun parseTemplateName(project: Project, templateName: String): List<UiTemplate> {
        val settingsSnapshot = BallastIdeaPlugin.getSettings(project)

        return getTemplatesFromName(settingsSnapshot, templateName)
            ?: error("Unknown template type: $templateName")
    }



    private fun getTemplatesFromName(settings: IntellijPluginSettingsSnapshot, templateName: String): List<UiTemplate>? {
        println("templateName: $templateName (${defaultViewModelItem.templateName})")
        return when(templateName) {
            UiTemplate.Contract.templateName -> listOf(UiTemplate.Contract)
            UiTemplate.InputHandler.templateName -> listOf(UiTemplate.InputHandler)
            UiTemplate.EventHandler.templateName -> listOf(UiTemplate.EventHandler)
            UiTemplate.SavedStateAdapter.templateName -> listOf(UiTemplate.SavedStateAdapter)
            defaultViewModelItem.templateName -> listOf(UiTemplate.ViewModel(settings.baseViewModelType.templateName))
            "Internal_Ui_All" -> {
                buildList {
                    this += UiTemplate.Contract
                    this += UiTemplate.InputHandler
                    this += UiTemplate.EventHandler
                    if(settings.allComponentsIncludesViewModel) {
                        this += UiTemplate.ViewModel(settings.baseViewModelType.templateName)
                    }
                    if (settings.allComponentsIncludesSavedStateAdapter) {
                        this += UiTemplate.SavedStateAdapter
                    }
                }
            }
            else -> null
        }
    }

    sealed class UiTemplate(
        override val templateName: String,
        override val fileNameSuffix: String,

        override val displayName: String,
        override val icon: Icon,
    ) : BaseTemplateCreator.TemplateKind {
        data object Contract : UiTemplate("UiContract", "Contract", "Contract", KotlinIcons.OBJECT)
        data object InputHandler : UiTemplate("UiInputHandler", "InputHandler", "InputHandler", KotlinIcons.CLASS)
        data object EventHandler : UiTemplate("UiEventHandler", "EventHandler", "EventHandler", KotlinIcons.CLASS)
        data object SavedStateAdapter : UiTemplate("UiSavedStateAdapter", "SavedStateAdapter", "SavedStateAdapter", KotlinIcons.CLASS)
        class ViewModel(templateName: String) : UiTemplate(templateName, "ViewModel", "ViewModel", KotlinIcons.CLASS)
    }
}
