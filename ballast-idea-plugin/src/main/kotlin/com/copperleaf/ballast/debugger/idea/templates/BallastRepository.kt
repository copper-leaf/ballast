package com.copperleaf.ballast.debugger.idea.templates

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
            .addTemplate("Contract", RepositoryTemplate.Contract)
            .addTemplate("Repository", RepositoryTemplate.Repository)
            .addTemplate("InputHandler", RepositoryTemplate.InputHandler)
            .addTemplate("RepositoryImpl", RepositoryTemplate.StandardRepositoryImpl)
            .addTemplate("AndroidRepositoryImpl", RepositoryTemplate.AndroidRepositoryImpl)
            .addKind("Standard Repository (All components)", KotlinIcons.SCRIPT, "Internal_Repository_Standard")
            .addKind("Android Repository (All components)", KotlinIcons.SCRIPT, "Internal_Repository_Android")
    }

    override fun parseTemplateName(templateName: String): List<RepositoryTemplate> {
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
        override val icon: Icon,
    ) : BaseTemplateCreator.TemplateKind {
        Contract("RepositoryContract", "RepositoryContract", KotlinIcons.OBJECT),
        Repository("RepositoryInterface", "Repository", KotlinIcons.INTERFACE),
        InputHandler("RepositoryInputHandler", "RepositoryInputHandler", KotlinIcons.CLASS),
        StandardRepositoryImpl("RepositoryImpl", "RepositoryImpl", KotlinIcons.CLASS),
        AndroidRepositoryImpl("AndroidRepositoryImpl", "RepositoryImpl", KotlinIcons.CLASS),
    }
}
