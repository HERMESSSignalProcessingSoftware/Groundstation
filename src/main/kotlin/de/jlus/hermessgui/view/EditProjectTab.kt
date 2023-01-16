package de.jlus.hermessgui.view

import de.jlus.hermessgui.viewmodel.ProjectViewModel
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.control.TextFormatter
import javafx.scene.web.HTMLEditor
import javafx.util.converter.IntegerStringConverter
import tornadofx.*


/**
 * displays the settings for the settings (currently only description)
 */
class EditProjectTab : MainTab("Project description") {
    private val projectVM by inject<ProjectViewModel>()
    private lateinit var htmlBox: HTMLEditor
    private val intermediateTmBaudrate = SimpleIntegerProperty(projectVM.tmBaudrate.value)


    override val root = vbox {
        spacing = 12.0
        hbox {
            label("TM Baudrate: ")
            spinner(1, 38400, intermediateTmBaudrate.value,
                1, true, intermediateTmBaudrate) {
                editor.textFormatter = TextFormatter(IntegerStringConverter(), intermediateTmBaudrate.value) {
                    if (it.isContentChange && it.controlNewText.toIntOrNull() == null) null else it
                }
            }
        }
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
        projectVM.tmBaudrate.value = intermediateTmBaudrate.value
        isDirty.value = false
        return true
    }


    init {
        isProjectTab.value = true
    }
}