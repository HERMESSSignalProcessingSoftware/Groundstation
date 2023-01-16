package de.jlus.hermessgui.viewmodel

import de.jlus.hermessgui.model.*
import de.jlus.hermessgui.view.MainTab
import javafx.beans.binding.Binding
import javafx.beans.binding.BooleanBinding
import javafx.scene.control.ButtonType
import tornadofx.*
import java.io.File
import java.io.IOException


/**
 * The main ViewModel representing any open project with all its open files
 */
class ProjectViewModel: ItemViewModel<Project>(Project()) {
    val description = bind(Project::descriptionProperty)
    val tmBaudrate = bind(Project::tmBaudrateProperty)
    val file = bind(Project::fileProperty)

    val name: Binding<String> = file.stringBinding { it?.nameWithoutExtension ?: "UNDEFINED" }
    val directory: Binding<File?> = file.objectBinding { it?.parentFile }
    val isOpened: BooleanBinding = file.isNotNull
    val spuConfFiles = mutableListOf<ProjectFileEntry>().asObservable()
    val calFiles = mutableListOf<ProjectFileEntry>().asObservable()
    val measurementFiles = mutableListOf<ProjectFileEntry>().asObservable()


    /**
     * Safes not only the project, but also all its open tabs
     * @return true, if save operation succeeded
     */
    fun saveProject (): Boolean {
        // save all tabs first, in case they modify this model
        for (tab in MainTab.allOpenTabs) {
            if (!tab.onProjectSave())
                return false
        }
        // push all changes from this viewModel
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
        return true
    }


    /**
     * saves a project in a new location
     * @param f the location of the project file (.herpro)
     * @return true, if save succeeded
     */
    fun saveProjectAs (f: File): Boolean {
        val oldName = name.value
        val oldDirectory = directory.value
        file.set(f)
        val newDirectory = directory.value

        // copy all project files and delete the old project file
        if (oldDirectory != null && newDirectory != null) {
            oldDirectory.copyRecursively(newDirectory, true)
            if (oldName != name.value)
                File(newDirectory, "$oldName.herpro").delete()
        }

        // save the herpro file
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
            refreshFileLists()
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
        // ask if it should save the dirty model
        if (isDirty || MainTab.allOpenTabs.any { it.isProjectTab.value && it.isDirty.value }) {
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

        // check all tabs, if they can be closed
        for (tab in MainTab.allOpenTabs.toList()) {
            if (!tab.onProjectClose())
                return false
        }

        // actually closing the project
        item = Project()
        spuConfFiles.clear()
        measurementFiles.clear()
        return true
    }


    /**
     * refreshes the lists of project associated files
     */
    fun refreshFileLists () {
        val dir = directory.value
        if (dir == null || !dir.isDirectory)
            return
        val files = dir.listFiles()?.toList() ?: return

        // add all SPUConfig files
        spuConfFiles.clear()
        spuConfFiles.addAll(files.filter { it.extension == "herconf" }
            .map { ProjectFileEntry(it.nameWithoutExtension, it) }.sortedBy { it.name } )
        // add all SPUConfig files
        calFiles.clear()
        calFiles.addAll(files.filter { it.extension == "hercal" }
            .map { ProjectFileEntry(it.nameWithoutExtension, it) }.sortedBy { it.name } )
        // add all measurement files
        measurementFiles.clear()
        measurementFiles.addAll(files.filter { it.extension == "hermeas" }
            .map { ProjectFileEntry(it.nameWithoutExtension, it) }.sortedBy { it.name } )
    }
}


class ProjectFileEntry (val name: String, val file: File)