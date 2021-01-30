package de.jlus.hermessgui.model

import de.jlus.hermessgui.app.SPUConfOverrideMode
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
    val ovrdModeProperty = SimpleObjectProperty(SPUConfOverrideMode.NONE)
    val sleepProperty = SimpleBooleanProperty(false)
    // dms adc settings
    val dmsOffsetCalInitProperty = SimpleBooleanProperty(false)
    val dmsFullscaleCalInitProperty = SimpleBooleanProperty(false)
    val dmsSamplerateProperty = SimpleObjectProperty(SPUConfSamplerate.SR_2000)
    val dmsPGAProperty = SimpleObjectProperty(SPUConfPGA.PGA_128)
    // pt100 adc settings
    val pt100OffsetCalInitProperty = SimpleBooleanProperty(false)
    val pt100FullscaleCalInitProperty = SimpleBooleanProperty(false)
    val pt100SamplerateProperty = SimpleObjectProperty(SPUConfSamplerate.SR_5)
    val pt100PGAProperty = SimpleObjectProperty(SPUConfPGA.PGA_128)
    // data storage settings
    val storeMeasurementsEnabledProperty = SimpleBooleanProperty(false)
    val storeMetadataEnabledProperty = SimpleBooleanProperty(false)
    val storeStartOnLOProperty = SimpleBooleanProperty(false)
    val storeStartOnSOEProperty = SimpleBooleanProperty(false)
    val storeStartOnSODSProperty = SimpleBooleanProperty(false)
    val storeStopOnLOProperty = SimpleBooleanProperty(false)
    val storeStopOnSOEProperty = SimpleBooleanProperty(false)
    val storeStopOnSODSProperty = SimpleBooleanProperty(false)
    val storeMinTimeProperty = SimpleIntegerProperty(0)
    val storeMaxTimeProperty = SimpleIntegerProperty(Int.MAX_VALUE)
    // TM settings
    val tmEnabledProperty = SimpleBooleanProperty(false)


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
                ovrdModeProperty.value = SPUConfOverrideMode.valueOf(string("ovrdMode")!!)
                sleepProperty.value = bool("sleep")!!
                with(jsonObject("dms")!!) {
                    dmsOffsetCalInitProperty.value = bool("offsetCalInit")!!
                    dmsFullscaleCalInitProperty.value = bool("fullscaleCalInit")!!
                    dmsSamplerateProperty.value = SPUConfSamplerate.valueOf(string("samplerate")!!)
                    dmsPGAProperty.value = SPUConfPGA.valueOf(string("pga")!!)
                }
                with(jsonObject("pt100")!!) {
                    pt100OffsetCalInitProperty.value = bool("offsetCalInit")!!
                    pt100FullscaleCalInitProperty.value = bool("fullscaleCalInit")!!
                    pt100SamplerateProperty.value = SPUConfSamplerate.valueOf(string("samplerate")!!)
                    pt100PGAProperty.value = SPUConfPGA.valueOf(string("pga")!!)
                }
                with(jsonObject("storage")!!) {
                    storeMeasurementsEnabledProperty.value = bool("measurementsEnabled")!!
                    storeMetadataEnabledProperty.value = bool("metadataEnabled")!!
                    storeStartOnLOProperty.value = bool("startOnLO")!!
                    storeStartOnSOEProperty.value = bool("startOnSOE")!!
                    storeStartOnSODSProperty.value = bool("startOnSODS")!!
                    storeStopOnLOProperty.value = bool("stopOnLO")!!
                    storeStopOnSOEProperty.value = bool("stopOnSOE")!!
                    storeStopOnSODSProperty.value = bool("stopOnSODS")!!
                    storeMinTimeProperty.value = int("minTime")!!
                    storeMaxTimeProperty.value = int("maxTime")!!
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
            add("ovrdMode", ovrdModeProperty.value.name)
            add("sleep", sleepProperty.value)
            add("dms", JsonBuilder().apply {
                add("offsetCalInit", dmsOffsetCalInitProperty.value)
                add("fullscaleCalInit", dmsFullscaleCalInitProperty.value)
                add("samplerate", dmsSamplerateProperty.value.name)
                add("pga", dmsPGAProperty.value.name)
            })
            add("pt100", JsonBuilder().apply {
                add("offsetCalInit", pt100OffsetCalInitProperty.value)
                add("fullscaleCalInit", pt100FullscaleCalInitProperty.value)
                add("samplerate", pt100SamplerateProperty.value.name)
                add("pga", pt100PGAProperty.value.name)
            })
            add("storage", JsonBuilder().apply {
                add("measurementsEnabled", storeMeasurementsEnabledProperty.value)
                add("metadataEnabled", storeMetadataEnabledProperty.value)
                add("startOnLO", storeStartOnLOProperty.value)
                add("startOnSOE", storeStartOnSOEProperty.value)
                add("startOnSODS", storeStartOnSODSProperty.value)
                add("stopOnLO", storeStopOnLOProperty.value)
                add("stopOnSOE", storeStopOnSOEProperty.value)
                add("stopOnSODS", storeStopOnSODSProperty.value)
                add("minTime", storeMinTimeProperty.value)
                add("maxTime", storeMaxTimeProperty.value)
            })
            add("tm", JsonBuilder().apply {
                add("enabled", tmEnabledProperty.value)
            })
        }
    }
}