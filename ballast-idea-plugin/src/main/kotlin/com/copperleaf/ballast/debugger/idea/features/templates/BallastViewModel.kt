package com.copperleaf.ballast.debugger.idea.features.templates

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
            .setTitle("New Ballast UI ViewModel")
            .addTemplate(ViewModelTemplate.Basic)
            .addTemplate(ViewModelTemplate.Android)
            .addTemplate(ViewModelTemplate.Ios)
            .addTemplate(ViewModelTemplate.Typealias)
    }

    override fun parseTemplateName(project: Project, templateName: String): List<ViewModelTemplate> {
        return ViewModelTemplate
            .values()
            .firstOrNull { templateName == it.templateName }
            ?.let { listOf(it) }
            ?: error("Unknown template type")
    }

    enum class ViewModelTemplate(
        override val templateName: String,
        override val fileNameSuffix: String,
        override val displayName: String,
        override val icon: Icon,
    ) : BaseTemplateCreator.TemplateKind {
        Basic("BasicViewModel", "ViewModel", "BasicViewModel", KotlinIcons.CLASS),
        Android("AndroidViewModel", "ViewModel", "AndroidViewModel", KotlinIcons.CLASS),
        Ios("IosViewModel", "ViewModel", "IosViewModel", KotlinIcons.CLASS),
        Typealias("TypealiasViewModel", "ViewModel", "Typealias", KotlinIcons.TYPE_ALIAS),
    }

    enum class DefaultVisibility(
        val displayName: String,
        val classVisibility: String,
        val propertyVisibility: String,
    ) {
        Public("Public", "public ", "public "),
        Internal("Internal", "internal ", ""),
        Default("Default (no modifier)", "", ""),
    }
}
