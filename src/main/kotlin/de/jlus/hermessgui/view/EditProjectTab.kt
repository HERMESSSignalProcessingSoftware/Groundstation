package de.jlus.hermessgui.view

import de.jlus.hermessgui.viewmodel.ProjectViewModel
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.web.HTMLEditor
import tornadofx.*


/**
 *
 */
class EditProjectTab : MainTab("Project description") {
    override var isProjectTab = true
    override val isDirty = SimpleBooleanProperty(false)

    private val projectVM by inject<ProjectViewModel>()
    private lateinit var htmlBox: HTMLEditor


    override val root = vbox {
        spacing = 12.0
        htmlBox = htmleditor {
            htmlText = projectVM.description.value
            setOnKeyReleased {
                // mark this tab dirty, if the editor lost focus and has changed
                if (!isDirty.value && htmlText != projectVM.description.value)
                    isDirty.value = true
            }
        }
        buttonbar {
            button("Save").action(::saveResource)
            button("Reset").action {
                htmlBox.htmlText = projectVM.description.value
            }
        }
    }


    override fun saveResource(): Boolean {
        projectVM.description.value = htmlBox.htmlText
        isDirty.value = false
        return projectVM.commit()
    }
    override fun onProjectSave(): Boolean = true // will be saved anyway by project
}