package com.copperleaf.ballast.debugger.idea.templates

import com.copperleaf.ballast.debugger.idea.base.BaseTemplateCreator
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
 *
 * See https://plugins.jetbrains.com/docs/intellij/templates.html
 *     https://plugins.jetbrains.com/docs/intellij/using-file-templates.html#custom-create-file-from-template-actions
 */
class BallastUi : BaseTemplateCreator<BallastUi.UiTemplate>(
    "Ballast UI component",
    "Creates new components for Ballast UIs",
    KotlinIcons.MPP,
), DumbAware {
    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String {
        return "Create Ballast UI component: $newName"
    }

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder
            .setTitle("New Ballast UI Component")
            .addTemplate("Contract", UiTemplate.Contract)
            .addTemplate("InputHandler", UiTemplate.InputHandler)
            .addTemplate("EventHandler", UiTemplate.EventHandler)
            .addTemplate("SavedStateAdapter", UiTemplate.SavedStateAdapter)
            .addKind("Contract, InputHandler, EventHandler", KotlinIcons.SCRIPT, "Internal_Ui_CIE")
            .addKind("All Components", KotlinIcons.SCRIPT, "Internal_Ui_All")
    }

    override fun parseTemplateName(templateName: String): List<UiTemplate> {
        return UiTemplate
            .values()
            .firstOrNull { templateName == it.templateName }
            ?.let { listOf(it) }
            ?: when (templateName) {
                "Internal_Ui_CIE" -> listOf(
                    UiTemplate.Contract,
                    UiTemplate.InputHandler,
                    UiTemplate.EventHandler,
                )
                "Internal_Ui_All" -> listOf(
                    UiTemplate.Contract,
                    UiTemplate.InputHandler,
                    UiTemplate.EventHandler,
                    UiTemplate.SavedStateAdapter,
                )
                else -> error("Unknown template type")
            }
    }

    enum class UiTemplate(
        override val templateName: String,
        override val fileNameSuffix: String,
        override val icon: Icon,
    ) : BaseTemplateCreator.TemplateKind {
        Contract("UiContract", "Contract", KotlinIcons.OBJECT),
        InputHandler("UiInputHandler", "InputHandler", KotlinIcons.CLASS),
        EventHandler("UiEventHandler", "EventHandler", KotlinIcons.CLASS),
        SavedStateAdapter("UiSavedStateAdapter", "SavedStateAdapter", KotlinIcons.CLASS),
    }
}
