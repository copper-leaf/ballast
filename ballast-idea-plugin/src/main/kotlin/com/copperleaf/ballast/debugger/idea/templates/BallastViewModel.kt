package com.copperleaf.ballast.debugger.idea.templates

import com.copperleaf.ballast.debugger.idea.base.BaseTemplateCreator
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import org.jetbrains.kotlin.idea.KotlinIcons
import javax.swing.Icon

class BallastViewModel : BaseTemplateCreator<BallastViewModel.ViewModelTemplate>(
    "Ballast ViewModel",
    "Creates new Ballast ViewModel containers",
    KotlinIcons.MPP,
), DumbAware {
    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String {
        return "Create Ballast ViewModel: $newName"
    }

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder
            .setTitle("New Ballast UI Component")
            .addTemplate("BasicViewModel", ViewModelTemplate.Basic)
            .addTemplate("AndroidViewModel", ViewModelTemplate.Android)
            .addTemplate("IosViewModel", ViewModelTemplate.Ios)
    }

    override fun parseTemplateName(templateName: String): List<ViewModelTemplate> {
        return ViewModelTemplate
            .values()
            .firstOrNull { templateName == it.templateName }
            ?.let { listOf(it) }
            ?: error("Unknown template type")
    }

    enum class ViewModelTemplate(
        override val templateName: String,
        override val fileNameSuffix: String,
        override val icon: Icon,
    ) : BaseTemplateCreator.TemplateKind {
        Basic("BasicViewModel", "ViewModel", KotlinIcons.CLASS),
        Android("AndroidViewModel", "ViewModel", KotlinIcons.CLASS),
        Ios("IosViewModel", "ViewModel", KotlinIcons.CLASS),
    }
}
