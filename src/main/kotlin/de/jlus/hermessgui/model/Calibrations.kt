package de.jlus.hermessgui.model

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import java.io.File
import java.io.IOException
import javax.json.JsonObject


class Calibrations: JsonModel {
    // general settings
    val calName = SimpleStringProperty("UNDEFINED")
    val stamps = Array(3) {Stamp()}


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
            for (i in stamps.indices)
                stamps[i].updateModel(json.jsonObject("stamp$i")!!)
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
            for (i in stamps.indices)
                add("stamp$i", stamps[i])
        }
    }



    /**
     * Contains for 2 strain gauges and 1 PT-100
     * - 24 bit complementary offset calibration data
     * - 24 bit complementary full scale calibration data
     */
    class Stamp: JsonModel {
        val dms1Ofc = SimpleIntegerProperty(0)
        val dms1Fsc = SimpleIntegerProperty(0)
        val dms2Ofc = SimpleIntegerProperty(0)
        val dms2Fsc = SimpleIntegerProperty(0)
        val tempOfc = SimpleIntegerProperty(0)
        val tempFsc = SimpleIntegerProperty(0)

        override fun updateModel (json: JsonObject) {
            with(json) {
                dms1Ofc.value = int("dms1Ofc")!!
                dms1Fsc.value = int("dms1Fsc")!!
                dms2Ofc.value = int("dms2Ofc")!!
                dms2Fsc.value = int("dms2Fsc")!!
                tempOfc.value = int("tempOfc")!!
                tempFsc.value = int("tempFsc")!!
            }
        }

        override fun toJSON (json: JsonBuilder) {
            with(json) {
                add("dms1Ofc", dms1Ofc.value)
                add("dms1Fsc", dms1Fsc.value)
                add("dms2Ofc", dms2Ofc.value)
                add("dms2Fsc", dms2Fsc.value)
                add("tempOfc", tempOfc.value)
                add("tempFsc", tempFsc.value)
            }
        }
    }
}