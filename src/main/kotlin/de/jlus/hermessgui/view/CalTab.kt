package de.jlus.hermessgui.view

import de.jlus.hermessgui.app.imgTooltip
import de.jlus.hermessgui.app.regexFileName
import de.jlus.hermessgui.app.tabIdCalPrefix
import de.jlus.hermessgui.model.Calibrations
import de.jlus.hermessgui.model.Dapi
import de.jlus.hermessgui.viewmodel.CalViewModel
import de.jlus.hermessgui.viewmodel.ProjectViewModel
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart.Series
import javafx.scene.control.TextFormatter
import javafx.scene.text.Font
import javafx.util.converter.IntegerStringConverter
import tornadofx.*
import java.io.File


/**
 * the view for the adc calibration
 */
class CalTab: MainTab("ADC Cal") {
    private val vm = CalViewModel(Calibrations())
    private val projectVm by inject<ProjectViewModel>()
    private val contentWidth = 700.0


    override val root = scrollpane {
        borderpane {
            paddingAll = 15
            prefWidth = contentWidth

            top = vbox {
                spacing = 15.0

                label(this@CalTab.tabTitle) {
                    gridpaneConstraints { columnSpan = 3 }
                    font = Font(20.0)
                }

                text("""The measurements are only being transmitted at 2Hz, regardless of the samplerate configuration.
                    |Note however, that a configured samplerate of > 10SPS will negatively influence the measurement
                    |results. A very good median value can be achieved with around 120 samples. Prior
                    |to saving the results you should however check, if the measurements are more or less all aligned
                    |within a reasonably small measurement range.""".trimMargin())

                form {
                    fieldset {
                        field("Calibration file name: ") {
                            imageview(imgTooltip) {
                                tooltip("An identifier for this set of calibration data")
                            }
                            textfield(vm.calName).validator {
                                if (regexFileName.matches(it ?: ""))
                                    null
                                else
                                    error("The name must match ${regexFileName.pattern}")
                            }
                        }
                        field("Target sample size: ") {
                            imageview(imgTooltip) {
                                tooltip("The number of samples to be collected")
                            }
                            spinner(
                                1, Int.MAX_VALUE, vm.targetSize.value, 6,
                                true, vm.targetSize
                            ) {
                                required()
                                editor.textFormatter = TextFormatter(IntegerStringConverter(), vm.targetSize.value) {
                                    if (it.isContentChange && it.controlNewText.toIntOrNull() == null) null else it
                                }
                            }
                        }
                    }
                }

                buttonbar {
                    button("Save") {
                        isDefaultButton = true
                        enableWhen(projectVm.isOpened and vm.valid)
                        action(::saveResource)
                    }
                    button("Reset") {
                        enableWhen(vm.dirty)
                        action(vm::rollback)
                    }
                    button("Start calibration") {
                        action {
                            if (vm.actualSize.value > 0) {
                                confirm("This will override the current values. Continue?") {
                                    vm.startDataAcquision()
                                }
                            }
                            else
                                vm.startDataAcquision()
                        }
                        disableWhen(vm.calibrationsRunning or Dapi.activePortProperty.isNull)
                    }
                    button("Stop calibration") {
                        action(vm::stopDataAcquisition)
                        enableWhen(vm.calibrationsRunning and Dapi.activePortProperty.isNotNull)
                    }
                }

                vbox {
                    spacing = 0.0
                    hbox {
                        label("Progression for samplesize of ")
                        label(vm.targetSize)
                        label(": ")
                        label(vm.actualSize)
                    }
                    progressbar(0.0) {
                        prefWidth = contentWidth
                        val targetSize = SimpleDoubleProperty()
                        targetSize.bind(vm.targetSize)
                        progressProperty().bind(vm.actualSize / targetSize)
                    }
                }
            }

            center = gridpane {
                row {
                    linechart("SGR1", NumberAxis(), NumberAxis()) {
                        animated = true
                        xAxis.label = "Time since start [s]"
                        yAxis.label = "Relative strain"
                        for (i in vm.datapointsSgr1.indices) {
                            val series = Series<Number, Number>()
                            series.data = vm.datapointsSgr1[i]
                            series.name = "STAMP ${i+1}"
                            data.add(series)
                        }
                        prefWidth = contentWidth
                    }
                }
                row {
                    linechart("SGR2", NumberAxis(), NumberAxis()) {
                        animated = true
                        xAxis.label = "Time since start [s]"
                        yAxis.label = "Relative strain"
                        for (i in vm.datapointsSgr2.indices) {
                            val series = Series<Number, Number>()
                            series.data = vm.datapointsSgr2[i]
                            series.name = "STAMP ${i+1}"
                            data.add(series)
                        }
                        prefWidth = contentWidth
                    }
                }
                row {
                    linechart("RTD", NumberAxis(), NumberAxis()) {
                        animated = true
                        xAxis.label = "Time since start [s]"
                        yAxis.label = "Temperature dependent value"
                        for (i in vm.datapointsRtd.indices) {
                            val series = Series<Number, Number>()
                            series.data = vm.datapointsRtd[i]
                            series.name = "STAMP ${i+1}"
                            data.add(series)
                        }
                        prefWidth = contentWidth
                    }
                }
            }
        }
    }


    override fun closeResource(): Boolean {
        if (Dapi.dpReceiver == vm)
            Dapi.dpReceiver = null
        return super.closeResource()
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