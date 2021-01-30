package de.jlus.hermessgui.view

import de.jlus.hermessgui.app.*
import de.jlus.hermessgui.model.SPUConfig
import de.jlus.hermessgui.viewmodel.ProjectViewModel
import de.jlus.hermessgui.viewmodel.SPUConfViewModel
import javafx.scene.layout.*
import javafx.scene.text.Font
import tornadofx.*


/**
 * A MainTab displaying either the
 */
class SPUConfigTab: MainTab("SPU Conf") {
    private val vm = SPUConfViewModel(SPUConfig())
    private val projectVm by inject<ProjectViewModel>()


    override val root = scrollpane {
        gridpane {
            hgap = 15.0
            vgap = 15.0
            paddingAll = 20
            prefWidth = 700.0 // based on the buttonbar on the bottom - widest object

            row {
                label(this@SPUConfigTab.tabTitle) {
                    gridpaneConstraints { columnSpan = 3 }
                    font = Font(20.0)
                }
            }

            // heading: general settings
            row {
                label("General SPU settings") {
                    gridpaneConstraints { columnSpan = 3 }
                    font = Font(14.0)
                }
            }
            // configuration name
            row {
                imageview(imgTooltip) {
                    tooltip("The name of the configuration will also be transfered to the SPU and can be " +
                            "read out later.")
                }
                label("Configuration name: ")
                textfield(vm.confName).validator {
                    if (regexConfName.matches(it ?: ""))
                        null
                    else
                        error("The name must match ${regexConfName.pattern}")
                }
            }
            // override Master/Slave
            row {
                imageview(imgTooltip) {
                    tooltip("Override the hardware jumper for primary/secondary mode")
                }
                label("Ovrd mode: ")
                combobox(vm.ovrdMode, SPUConfOverrideMode.values().toList()) {
                    cellFormat { text = it.text }
                }
            }
            // sleep mode
            row {
                imageview(imgTooltip) {
                    tooltip("Do not use any capabilities of the SPU until the next configuration.")
                }
                label("Put SPU in sleep: ")
                checkbox("Put into sleep", vm.sleep)
            }

            // heading: Strain gauge rosettes (DMS) settings
            row {
                label("Strain gauge rosettes (DMS) ADC settings") {
                    gridpaneConstraints { columnSpan = 3 }
                    font = Font(14.0)
                }
            }
            // read from configuration storage
            row {
                imageview(imgTooltip) {
                    tooltip("Loads the prerecorded data for offset and full-scale calibration " +
                            "into the ADCs.")
                }
                label("Initiate with calibration replay: ")
                vbox {
                    spacing = 10.0
                    checkbox("Offset calibration register", vm.dmsOffsetCalInit)
                    checkbox("Full-scale calibration register", vm.dmsFullscaleCalInit)
                }
            }
            // samplerate adcs for strain gauge
            row {
                imageview(imgTooltip) {
                    tooltip("Select the ADC samplerate. Check the TI ADS1147 datasheet for data on input " +
                            "accuracy for every setting.")
                }
                label("DMS ADC Samplerate: ")
                combobox(vm.dmsSamplerate, SPUConfSamplerate.values().toList()) {
                    cellFormat { text = it.text }
                }
            }
            // adc pgas for strain gauge
            row {
                imageview(imgTooltip) {
                    tooltip("Select the ADC programmable gain amplifier value. Make sure to set for a " +
                            "proper value to include the highest interesting values.")
                }
                label("DMS ADC PGA: ")
                combobox(vm.dmsPGA, SPUConfPGA.values().toList()) {
                    cellFormat { text = it.numeric.toString() }
                }
            }

            // heading: temperature (PT-100) settings
            row {
                label("Temperature (PT-100) ADC settings") {
                    gridpaneConstraints { columnSpan = 3 }
                    font = Font(14.0)
                }
            }
            // read from configuration storage
            row {
                imageview(imgTooltip) {
                    tooltip("Loads the prerecorded data for offset and full-scale calibration " +
                            "into the ADCs.")
                }
                label("Initiate with calibration replay: ")
                vbox {
                    spacing = 10.0
                    checkbox("Offset calibration register", vm.pt100OffsetCalInit)
                    checkbox("Full-scale calibration register", vm.pt100FullscaleCalInit)
                }
            }
            // samplerate adcs for pt-100
            row {
                imageview(imgTooltip) {
                    tooltip("Select the ADC samplerate. Check the TI ADS1147 datasheet for data on input " +
                            "accuracy for every setting.")
                }
                label("PT-100 ADC Samplerate: ")
                combobox(vm.pt100Samplerate, SPUConfSamplerate.values().toList()) {
                    cellFormat { text = it.text }
                }
            }
            // adc pgas for pt-100
            row {
                imageview(imgTooltip) {
                    tooltip("Select the ADC programmable gain amplifier value. Make sure to set for a " +
                            "proper value to include the highest interesting values.")
                }
                label("PT-100 ADC PGA: ")
                combobox(vm.pt100PGA, SPUConfPGA.values().toList()) {
                    cellFormat { text = it.numeric.toString() }
                }
            }

            // heading: data storage
            row {
                label("Data storage settings") {
                    gridpaneConstraints { columnSpan = 3 }
                    font = Font(14.0)
                }
            }
            // enable saving measurements or metadata
            row {
                imageview(imgTooltip) {
                    tooltip("Enables the storage of metadata or measurement data. Disable to " +
                            "safe lifecycles on the flash.")
                }
                label("Enable storage of: ")
                vbox {
                    spacing = 10.0
                    checkbox("Measurements", vm.storeMeasurementsEnabled)
                    checkbox("Metadata", vm.storeMetadataEnabled)
                }
            }
            // start storage on
            row {
                imageview(imgTooltip) {
                    tooltip("If any of the conditions are met, start the recording of " +
                            "measurements and metadata.")
                }
                label("Start recordings on [ANY HIGH]: ")
                hbox {
                    spacing = 10.0
                    checkbox("LO", vm.storeStartOnLo)
                    checkbox("SOE", vm.storeStartOnSOE)
                    checkbox("SODS", vm.storeStartOnSODS)
                }
            }
            // stop storage on
            row {
                imageview(imgTooltip) {
                    tooltip("If all of the conditions are met, stop the recording of " +
                            "measurements and metadata.")
                }
                label("Stop recordings on [ALL LOW]: ")
                hbox {
                    spacing = 10.0
                    checkbox("LO", vm.storeStopOnLo)
                    checkbox("SOE", vm.storeStopOnSOE)
                    checkbox("SODS", vm.storeStopOnSODS)
                }
            }
            // minimum and maximum recording time
            row {
                imageview(imgTooltip) {
                    tooltip("After initialisation of data storage keep the data recording time in " +
                            "these bound. Useful to prevent too short recordings caused by bouncing signal " +
                            "lines")
                }
                label("Recording time min/max: ")
                vbox {
                    spacing = 10.0
                    hbox {
                        label("Min [s]: ")
                        spinner(0, Int.MAX_VALUE, vm.storeMinTime.value, property = vm.storeMinTime)
                    }
                    hbox {
                        label("Max [s]: ")
                        spinner(0, Int.MAX_VALUE, vm.storeMaxTime.value, property = vm.storeMaxTime)
                    }
                }
            }

            // heading: TM
            row {
                label("Telemetry settings") {
                    gridpaneConstraints { columnSpan = 3 }
                    font = Font(14.0)
                }
            }
            // configuration name
            row {
                imageview(imgTooltip) {
                    tooltip("Enable sending telemetry data via RXSM")
                }
                label("Enable telemetry: ")
                checkbox("Enable TM", vm.tmEnabled)
            }

            // buttonbar
            row {
                buttonbar {
                    gridpaneConstraints {
                        columnSpan = 3
                    }
                    button("Save") {
                        isDefaultButton = true
                        enableWhen(projectVm.isOpened and vm.valid)
                        action(::saveResource)
                    }
                    button("Reset fields") {
                        enableWhen(vm.dirty)
                        action(vm::rollback)
                    }
                    button("Program SPU").action {
                        information("Not implemented yet")
                    }
                    button("Read SPU configurations").action {
                        information("Not implemented yet")
                    }
                }
            }

        }
    }


    override fun saveResource(): Boolean {
        // safe to file
        if (!vm.commit())
            return false
        // update the title
        tabTitle.value = "SPU Conf: " + vm.confName.value
        // at least after saving this beast is a project tab
        isProjectTab.value = true
        tabId = tabIdSPUConfigPrefix + vm.confName.value
        return true
    }


    /**
     * loads the resource from a string defined by the name of the file within the project
     * directory without the extension
     * @return true, if succeeded
     */
    fun loadResource (name: String): Boolean {
        if (!vm.loadFile(name))
            return false
        tabTitle.value = "SPU Conf: " + vm.confName.value
        isProjectTab.value = true
        tabId = tabIdSPUConfigPrefix + vm.confName.value
        return true
    }


    init {
        isDirty.bind(vm.dirty)
    }
}