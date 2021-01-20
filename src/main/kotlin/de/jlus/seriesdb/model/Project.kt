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
 * @param initFile When provided, the model will load the file
 */
class Project(initFile: File? = null): JsonModel {
    val nameProperty = SimpleStringProperty("NO NAME SET")
    var name: String by nameProperty
    val descriptionProperty = SimpleStringProperty("NO DESCRIPTION SET")
    var description: String by descriptionProperty
    val fileProperty = SimpleObjectProperty<File>(initFile)
    var file: File? by fileProperty


    init {
        if (initFile != null)
            readFromFile()
    }


    /**
     * Saves only this project file to
     * @throws IOException in case anything does not work
     */
    fun saveToFile () {
        val f = file
        if (!isValidFilePath(f, false))
            throw IOException("Filepath is not valid for a hermess project file: $f")
        f!! // was assured in isValidFilePath
        // make new directory, if not exists
        f.parentFile?.mkdirs()
        if (!f.canWrite() && f.exists())
            throw IOException("Can not write to file: $f")
        // generate the JSON string
        f.writeText(JsonBuilder().add("project", this).build().toPrettyString())
    }


    /**
     * Reads only this project file. Make sure it exists!
     * @throws IOException in case anything does not work
     */
    fun readFromFile () {
        val f = file
        if (!isValidFilePath(f, true))
            throw IOException("Filepath is not valid for a hermess project file: $f")
        f!! // was assured in isValidFilePath
        if (!f.canRead())
            throw IOException("Can not read file: $f")
        updateModel(loadJsonObject(f.toPath()).getJsonObject("project"))
    }


    /**
     * Checks, if the file path is consistent with the rules for projects
     */
    private fun isValidFilePath (f: File?, mustExist: Boolean): Boolean {
        if (f == null)
            return false
        val parent = f.parentFile ?: File("")
        if (f.extension != "herpro" || f.nameWithoutExtension != parent.name
                || !parent.name.matches(regexProjectName))
            return false
        if (mustExist && (!f.isFile || !parent.isDirectory))
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