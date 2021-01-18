package de.jlus.seriesdb.viewmodel

import de.jlus.seriesdb.model.*
import tornadofx.*
import java.io.IOException

class ProjectViewModel(dummyProject: Project): ItemViewModel<Project>(dummyProject) {
    val isDummy = bind(Project::isDummy)
    val name = bind(Project::nameProperty) // TODO Add validators for all three fields
    val description = bind(Project::descriptionProperty)
    val file = bind(Project::fileProperty)

    override fun onCommit() {
        try {
            item.saveToFile()
        } catch (e: IOException) {
            warning("Could not save file", e.message)
        }
    }
}