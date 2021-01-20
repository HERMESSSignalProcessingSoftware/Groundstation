package de.jlus.seriesdb.viewmodel

import de.jlus.seriesdb.model.*
import javafx.beans.binding.BooleanBinding
import javafx.scene.control.ButtonType
import tornadofx.*
import java.io.File
import java.io.IOException


/**
 * The main ViewModel representing any open project with all its open files
 */
class ProjectViewModel: ItemViewModel<Project>(Project()) {
    val name = bind(Project::nameProperty) // TODO Add validators for all fields
    val description = bind(Project::descriptionProperty)
    val file = bind(Project::fileProperty)
    val isOpened: BooleanBinding = file.isNotNull


    /**
     * Safes not only the project, but also all its open tabs
     * @return true, if save operation succeeded
     */
    fun saveProject (): Boolean {
        // First push all changes from this viewModel
        if (!commit())
            return false
        // save to file if it works
        try {
            item.saveToFile()
        }
        catch (e: IOException) {
            warning("Could not save file", e.message)
            return false
        }
        // TODO save all project related tabs
        return true
    }


    /**
     * saves a project in a new location
     * @param f the location of the project file (.herpro)
     * @return true, if save succeeded
     */
    fun saveProjectAs (f: File): Boolean {
        file.set(f)
        return saveProject()
    }


    /**
     * Opens a project from an existing file path
     * @param f the project file to open
     */
    fun openProject (f: File) {
        closeProject()
        try {
            item = Project(f)
        }
        catch (e: IOException) {
            item = Project()
            warning("Could not read file", e.message)
        }
    }


    /**
     * Reloads the entire project with all its tabs by closing it and then reopen it again
     */
    fun reloadProject () {
        val f = item.file
        if (f != null)
            openProject(f)
    }


    /**
     * Close the currently opened project and all its tabs.
     * If there are not saved changes, ask either to Save them all, discard changes or cancel closing operation
     * @return true, if close operation succeeded
     */
    fun closeProject (): Boolean {
        if (!isOpened.value) return true
        if (isDirty) {
            information(
                    "Project is not saved",
                    "Would you like to save all pending changes before closing?",
                    ButtonType.YES, ButtonType.NO, ButtonType.CANCEL
            ) {
                if (it == ButtonType.YES && !saveProject())
                    return false // an error message should have plopped up, if saveProject failed
                else if (it == ButtonType.CANCEL)
                    return false // just dont close the project
            }
        }
        // TODO close all other tabs
        item = Project()
        return true
    }
}