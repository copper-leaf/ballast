package com.copperleaf.ballast.debugger.idea.features.templates

import com.copperleaf.ballast.debugger.idea.base.BaseTemplateCreator
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import org.jetbrains.kotlin.idea.KotlinIcons
import javax.swing.Icon

class BallastRepository : BaseTemplateCreator<BallastRepository.RepositoryTemplate>(
    "Ballast Repository component",
    "Creates new components for Ballast Repositories",
    KotlinIcons.MPP,
), DumbAware {
    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String {
        return "Create Ballast Repository component: $newName"
    }

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder
            .setTitle("New Ballast Repository")
            .addTemplate(RepositoryTemplate.Contract)
            .addTemplate(RepositoryTemplate.Repository)
            .addTemplate(RepositoryTemplate.InputHandler)
            .addTemplate(RepositoryTemplate.StandardRepositoryImpl)
            .addTemplate(RepositoryTemplate.AndroidRepositoryImpl)
            .addKind("Standard Repository (All components)", KotlinIcons.SCRIPT, "Internal_Repository_Standard")
            .addKind("Android Repository (All components)", KotlinIcons.SCRIPT, "Internal_Repository_Android")
    }

    override fun parseTemplateName(project: Project, templateName: String): List<RepositoryTemplate> {
        return RepositoryTemplate
            .values()
            .firstOrNull { templateName == it.templateName }
            ?.let { listOf(it) }
            ?: when (templateName) {
                "Internal_Repository_Standard" -> listOf(
                    RepositoryTemplate.Contract,
                    RepositoryTemplate.InputHandler,
                    RepositoryTemplate.Repository,
                    RepositoryTemplate.StandardRepositoryImpl,
                )
                "Internal_Repository_Android" -> listOf(
                    RepositoryTemplate.Contract,
                    RepositoryTemplate.InputHandler,
                    RepositoryTemplate.Repository,
                    RepositoryTemplate.AndroidRepositoryImpl,
                )
                else -> error("Unknown template type")
            }
    }

    enum class RepositoryTemplate(
        override val templateName: String,
        override val fileNameSuffix: String,
        override val displayName: String,
        override val icon: Icon,
    ) : BaseTemplateCreator.TemplateKind {
        Contract("RepositoryContract", "RepositoryContract", "Contract", KotlinIcons.OBJECT),
        Repository("RepositoryInterface", "Repository", "Repository", KotlinIcons.INTERFACE),
        InputHandler("RepositoryInputHandler", "RepositoryInputHandler", "InputHandler", KotlinIcons.CLASS),
        StandardRepositoryImpl("RepositoryImpl", "RepositoryImpl", "RepositoryImpl", KotlinIcons.CLASS),
        AndroidRepositoryImpl("AndroidRepositoryImpl", "RepositoryImpl", "AndroidRepositoryImpl", KotlinIcons.CLASS),
    }
}
