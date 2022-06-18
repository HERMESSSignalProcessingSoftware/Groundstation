package de.jlus.hermessgui.model

import tornadofx.*
import javax.json.JsonObject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds

class Dataframe (
    stampId: Int,
    errAdcLagging: Boolean,
    errStampLagging: Boolean,
    errNoNew: Boolean,
    errOverwritten: Boolean,
    sgr1: Int,
    sgr2: Int,
    rtd: Int,
    timestamp: Duration
): JsonModel {
    var stampId: Int = stampId
        private set
    var errAdcLagging: Boolean = errAdcLagging
        private set
    var errStampLagging: Boolean = errStampLagging
        private set
    var errNoNew: Boolean = errNoNew
        private set
    var errOverwritten: Boolean = errOverwritten
        private set
    var sgr1: Int = sgr1
        private set
    var sgr2: Int = sgr2
        private set
    var rtd: Int = rtd
        private set
    var timestamp: Duration = timestamp
        private set


    /**
     * Empty constructor for use when only creating the object itself and filling it via the loaders
     */
    constructor (): this(0, false,
        false, false, false,
        0, 0, 0, Duration.ZERO)


    /**
     * Loads the model from a json object
     */
    override fun updateModel (json: JsonObject) {
        try {
            stampId = json.int("stampId")!!
            errAdcLagging = json.bool("errAdcLagging")!!
            errStampLagging = json.bool("errStampLagging")!!
            errNoNew = json.bool("errNoNew")!!
            errOverwritten = json.bool("errOverwritten")!!
            sgr1 = json.int("sgr1")!!
            sgr2 = json.int("sgr2")!!
            rtd = json.int("rtd")!!
            timestamp = json.long("timestamp")!!.microseconds
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
            add("stampId", stampId)
            add("errAdcLagging", errAdcLagging)
            add("errStampLagging", errStampLagging)
            add("errNoNew", errNoNew)
            add("errOverwritten", errOverwritten)
            add("sgr1", sgr1)
            add("sgr2", sgr2)
            add("rtd", rtd)
            add("timestamp", timestamp.inWholeMicroseconds)
        }
    }
}