package de.jlus.seriesdb.controller

import de.jlus.seriesdb.model.Project
import de.jlus.seriesdb.viewmodel.*
import javafx.beans.property.*
import tornadofx.Controller
import tornadofx.warning
import java.io.File
import java.io.IOException


/**
 * Manages the top level resources
 */
class MainController: Controller() {
    val projectViewModel = ProjectViewModel(Project())
    val dapiConnected = SimpleBooleanProperty(false)
    val tmConnected = SimpleBooleanProperty(false)


    /**
     * create a new project and open it. Both arguments will not be checked again!
     * @param newName sanitized name for the directory and the filename
     * @param parentDir only existing directory allowed, where the new project folder will be made
     */
    fun createNewProject (newName: String, parentDir: File) {
        // create directory
        val projectPath = File(parentDir, newName)
        // create the new Project object and save to file
        val newProject = Project(false).apply {
            name = newName
            description = "New project"
            file = File(projectPath, "${newName}.herpro")
        }
        try {
            newProject.saveToFile()
        } catch (e: IOException) {
            warning("Could not save file", e.message)
        }
        openProject(newProject)
    }


    fun closeProject () {
        // check if dirty

        // close related tabs
    }


    fun openProject (project: Project) {
        closeProject()
        // open new project
    }
}