package de.jlus.hermessgui.model

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import java.io.File
import java.io.IOException
import javax.json.Json
import javax.json.JsonObject


class Calibrations: JsonModel {
    val dataframes = observableListOf<Dataframe>()

    // general settings
    val calName = SimpleStringProperty("UNDEFINED")
    val targetSize = SimpleIntegerProperty(120)
    val actualSize = SimpleIntegerProperty(0)


    /**
     * saves in a json file
     * @param file the file to store to. The containing directory must exist. It is not required for
     * the file to exist, it should be writable however.
     * @throws IOException if cannot write to file
     */
    fun saveToFile (file: File) {
        if (!file.canWrite() && file.exists())
            throw IOException("Can not write to file: ${file.absolutePath}")
        file.writeText(JsonBuilder().add("Calibrations", this).build().toPrettyString())
    }


    /**
     * overrides the properties of this object with the ones specified by the file provided
     * @param file the file to read from. Must exist.
     * @throws IOException if could not read file
     * @throws NullPointerException if could not find all fields required
     */
    fun readFromFile (file: File) {
        if (!file.canRead())
            throw IOException("Can not read file: ${file.absolutePath}")
        updateModel(loadJsonObject(file.toPath()).getJsonObject("Calibrations"))
    }


    /**
     * Loads the model from a json object
     */
    override fun updateModel (json: JsonObject) {
        try {
            calName.value = json.string("calName")!!
            targetSize.value = json.int("targetSize")!!
            actualSize.value = json.int("actualSize")!!
            for (dfValues in json.getJsonArray("dataframes") ?: listOf()) {
                val df = Dataframe()
                df.updateModel(dfValues.asJsonObject())
                dataframes.add(df)
            }
        }
        catch (_: NullPointerException) {
            warning("The configuration file does not contain all required fields.")
        }
        catch (_: IllegalArgumentException) {
            warning("The configuration file does not contain all required fields.")
        }
    }


    /**
     * export to a json object
     */
    override fun toJSON (json: JsonBuilder) {
        with(json) {
            add("calName", calName.value)
            add("targetSize", targetSize.value)
            add("actualSize", actualSize.value)
            val builder = Json.createArrayBuilder()
            for (i in dataframes.indices)
                builder.add(dataframes[i].toJSON())
            add("dataframes", builder)
        }
    }
}