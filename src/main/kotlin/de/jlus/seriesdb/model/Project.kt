package de.jlus.seriesdb.model

import de.jlus.seriesdb.app.regexProjectName
import javafx.beans.property.*
import tornadofx.JsonModel
import tornadofx.*
import java.io.File
import java.io.IOException
import javax.json.JsonObject


/**
 * A Project file and thus this model contains only basic text information
 * @param isDummy can only be set upon initialization. If set to true, no actions on this model are permitted and
 * the view should regard this project as "no project loaded"
 * @param initFile When provided, the model will load the file. Make sure to only pass a sanitized file, otherwise
 * undefined behavior may occur.
 */
class Project(val isDummy: Boolean = true, initFile: File = File("")): JsonModel {
    val nameProperty = SimpleStringProperty("")
    var name: String by nameProperty
    val descriptionProperty = SimpleStringProperty("")
    var description: String by descriptionProperty
    val fileProperty = SimpleObjectProperty(initFile)
    var file: File by fileProperty


    init {
        if (!isDummy && initFile.isFile && initFile.exists())
            readFromFile()
    }


    /**
     * Saves only this project file to
     * @throws IOException in case anything does not work
     */
    fun saveToFile () {
        if (!isValidFilePath(false))
            throw IOException("Filepath is not valid for a hermess project file: $file")
        // make new directory, if not exists
        file.parentFile?.mkdirs()
        if (!file.canWrite() && file.exists())
            throw IOException("Can not write to file: $file")
        // generate the JSON string
        file.writeText(JsonBuilder().add("project", this).build().toPrettyString())
    }


    /**
     * Reads only this project file. Make sure it exists!
     * @throws IOException in case anything does not work
     */
    fun readFromFile () {
        if (!isValidFilePath(true))
            throw IOException("Filepath is not valid for a hermess project file: $file")
        if (!file.canRead())
            throw IOException("Can not read file: $file")
        updateModel(loadJsonObject(file.toPath()).getJsonObject("project"))
    }


    /**
     * Checks, if the file path is consistent with the rules for projects
     */
    private fun isValidFilePath (mustExist: Boolean): Boolean {
        val parent = file.parentFile ?: File("")
        if (file.extension != "herpro" || file.nameWithoutExtension != parent.name
                || !parent.name.matches(regexProjectName))
            return false
        if (mustExist && (!file.isFile || !parent.isDirectory))
            return false
        return true
    }


    override fun updateModel(json: JsonObject) {
        with(json) {
            if (!json.keys.containsAll(listOf("name", "description")))
                warning("Corrupted file loaded: Not all required keys existing.")
            name = string("name") ?: "UNDEFINED"
            description = string("description") ?: "UNDEFINED"
        }
    }


    override fun toJSON(json: JsonBuilder) {
        with(json) {
            add("name", name)
            add("description", description)
        }
    }
}