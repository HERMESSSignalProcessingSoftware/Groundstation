package de.jlus.hermessgui.view

import de.jlus.hermessgui.app.imgTooltip
import de.jlus.hermessgui.app.regexFileName
import de.jlus.hermessgui.app.tabIdCalPrefix
import de.jlus.hermessgui.model.Calibrations
import de.jlus.hermessgui.viewmodel.CalViewModel
import de.jlus.hermessgui.viewmodel.ProjectViewModel
import javafx.scene.text.Font
import tornadofx.*
import java.io.File


/**
 * the view for the adc calibration
 */
class CalTab: MainTab("ADC Cal") {
    private val vm = CalViewModel(Calibrations())
    private val projectVm by inject<ProjectViewModel>()


    override val root = scrollpane {
        gridpane {
            hgap = 15.0
            vgap = 15.0
            paddingAll = 20
            prefWidth = 700.0 // based on the buttonbar on the bottom - widest object

            row {
                label(this@CalTab.tabTitle) {
                    gridpaneConstraints { columnSpan = 3 }
                    font = Font(20.0)
                }
            }

            // configuration name
            row {
                imageview(imgTooltip) {
                    tooltip("An identifier for this set of calibration data")
                }
                label("Calibration file name: ")
                textfield(vm.calName).validator {
                    if (regexFileName.matches(it ?: ""))
                        null
                    else
                        error("The name must match ${regexFileName.pattern}")
                }
            }

            // list all stamps
            for (i in vm.stamps.indices) {
                // stamp heading
                row {
                    label("STAMP $i calibration data") {
                        gridpaneConstraints { columnSpan = 3 }
                        font = Font(14.0)
                    }
                }
                // dms1
                row {
                    imageview(imgTooltip) {
                        tooltip("Offset calibration data. Check the ADS114x manual for mor information.")
                    }
                    label("DMS 1 OFC: ")
                    spinner(-8388608, 8388607, vm.stamps[i].dms1Ofc.value,
                        1, true, vm.stamps[i].dms1Ofc).required()
                }
                row {
                    imageview(imgTooltip) {
                        tooltip("Fullscale calibration data. Check the ADS114x manual for mor information.")
                    }
                    label("DMS 1 FSC: ")
                    spinner(-8388608, 8388607, vm.stamps[i].dms1Fsc.value,
                        1, true, vm.stamps[i].dms1Fsc).required()
                }
                // dms2
                row {
                    imageview(imgTooltip) {
                        tooltip("Offset calibration data. Check the ADS114x manual for mor information.")
                    }
                    label("DMS 2 OFC: ")
                    spinner(-8388608, 8388607, vm.stamps[i].dms2Ofc.value,
                        1, true, vm.stamps[i].dms2Ofc).required()
                }
                row {
                    imageview(imgTooltip) {
                        tooltip("Fullscale calibration data. Check the ADS114x manual for mor information.")
                    }
                    label("DMS 2 FSC: ")
                    spinner(-8388608, 8388607, vm.stamps[i].dms2Fsc.value,
                        1, true, vm.stamps[i].dms2Fsc).required()
                }
                // temperature
                row {
                    imageview(imgTooltip) {
                        tooltip("Offset calibration data. Check the ADS114x manual for mor information.")
                    }
                    label("Temperature OFC: ")
                    spinner(-8388608, 8388607, vm.stamps[i].tempOfc.value,
                        1, true, vm.stamps[i].tempOfc).required()
                }
                row {
                    imageview(imgTooltip) {
                        tooltip("Fullscale calibration data. Check the ADS114x manual for mor information.")
                    }
                    label("Temperature FSC: ")
                    spinner(-8388608, 8388607, vm.stamps[i].tempFsc.value,
                        1, true, vm.stamps[i].tempFsc).required()
                }
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
                }
            }
            row {
                buttonbar {
                    gridpaneConstraints {
                        columnSpan = 3
                    }
                    button("Trigger OFC").action {
                        information("Not implemented yet")
                    }
                    button("Trigger FSC").action {
                        information("Not implemented yet")
                    }
                    button("Read calibrations").action {
                        information("Not implemented yet")
                    }
                    button("Write calibrations").action {
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
        tabTitle.value = "ADC Cal: " + vm.calName.value
        // at least after saving this beast is a project tab
        isProjectTab.value = true
        tabId = tabIdCalPrefix + vm.calName.value
        return true
    }


    /**
     * loads the resource from a string defined by the name of the file within the project
     * directory without the extension
     * @return true, if succeeded
     */
    fun loadResource (file: File): Boolean {
        if (!vm.loadFile(file))
            return false
        tabTitle.value = "ADC Cal: " + vm.calName.value
        isProjectTab.value = true
        tabId = tabIdCalPrefix + vm.calName.value
        return true
    }


    init {
        isDirty.bind(vm.dirty)
    }
}