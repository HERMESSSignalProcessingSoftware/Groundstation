package de.jlus.hermessgui.model

import de.jlus.hermessgui.app.SPUConfCalibrationTypes
import de.jlus.hermessgui.app.SPUConfPGA
import de.jlus.hermessgui.app.SPUConfSamplerate
import javafx.beans.property.*
import tornadofx.*
import java.io.File
import java.io.IOException
import javax.json.JsonObject


/**
 * The model holding all information, that can be configured to an SPU.
 */
class SPUConfig: JsonModel {
    // General SPU settings
    val confNameProperty = SimpleStringProperty("UNDEFINED")
    // dms adc settings
    val sgrOffsetCalInitProperty = SimpleObjectProperty(SPUConfCalibrationTypes.SystemOffset)
    val sgrSamplerateProperty = SimpleObjectProperty(SPUConfSamplerate.SR_1000)
    val sgrPGAProperty = SimpleObjectProperty(SPUConfPGA.PGA_64)
    // pt100 adc settings
    val rtdOffsetCalInitProperty = SimpleObjectProperty(SPUConfCalibrationTypes.SelfOffset)
    val rtdSamplerateProperty = SimpleObjectProperty(SPUConfSamplerate.SR_10)
    val rtdPGAProperty = SimpleObjectProperty(SPUConfPGA.PGA_16)
    // data storage settings
    val storeOnSodsEnabledProperty = SimpleBooleanProperty(true)
    val clearOnSoeEnabledProperty = SimpleBooleanProperty(false)
    val storeMinTimeProperty = SimpleIntegerProperty(0)
    val storeMaxTimeProperty = SimpleIntegerProperty(800)
    // TM settings
    val tmEnabledProperty = SimpleBooleanProperty(true)


    /**
     * saves the SPUConfig in a json file
     * @param file the file to store to. The containing directory must exist. It is not required for
     * the file to exist, it should be writable however.
     * @throws IOException if cannot write to file
     */
    fun saveToFile (file: File) {
        if (!file.canWrite() && file.exists())
            throw IOException("Can not write to file: ${file.absolutePath}")
        file.writeText(JsonBuilder().add("SPU-Config", this).build().toPrettyString())
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
        updateModel(loadJsonObject(file.toPath()).getJsonObject("SPU-Config"))
    }


    /**
     * Loads the model from a json object
     */
    override fun updateModel (json: JsonObject) {
        try {
            with(json) {
                confNameProperty.value = string("confName")!!
                with(jsonObject("sgr")!!) {
                    sgrOffsetCalInitProperty.value = SPUConfCalibrationTypes.valueOf(string("offsetCalInit")!!)
                    sgrSamplerateProperty.value = SPUConfSamplerate.valueOf(string("samplerate")!!)
                    sgrPGAProperty.value = SPUConfPGA.valueOf(string("pga")!!)
                }
                with(jsonObject("rtd")!!) {
                    rtdOffsetCalInitProperty.value = SPUConfCalibrationTypes.valueOf(string("offsetCalInit")!!)
                    rtdSamplerateProperty.value = SPUConfSamplerate.valueOf(string("samplerate")!!)
                    rtdPGAProperty.value = SPUConfPGA.valueOf(string("pga")!!)
                }
                with(jsonObject("storage")!!) {
                    storeOnSodsEnabledProperty.value = bool("storeOnSodsEnabled")!!
                    clearOnSoeEnabledProperty.value = bool("clearOnSoeEnabled")!!
                    storeMaxTimeProperty.value = int("maxTime")!!
                    storeMinTimeProperty.value = int("minTime")!!
                }
                with(jsonObject("tm")!!) {
                    tmEnabledProperty.value = bool("enabled")!!
                }
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
            add("confName", confNameProperty.value)
            add("sgr", JsonBuilder().apply {
                add("offsetCalInit", sgrOffsetCalInitProperty.value.name)
                add("samplerate", sgrSamplerateProperty.value.name)
                add("pga", sgrPGAProperty.value.name)
            })
            add("rtd", JsonBuilder().apply {
                add("offsetCalInit", rtdOffsetCalInitProperty.value.name)
                add("samplerate", rtdSamplerateProperty.value.name)
                add("pga", rtdPGAProperty.value.name)
            })
            add("storage", JsonBuilder().apply {
                add("storeOnSodsEnabled", storeOnSodsEnabledProperty.value)
                add("clearOnSoeEnabled", clearOnSoeEnabledProperty.value)
                add("minTime", storeMinTimeProperty.value)
                add("maxTime", storeMaxTimeProperty.value)
            })
            add("tm", JsonBuilder().apply {
                add("enabled", tmEnabledProperty.value)
            })
        }
    }
}