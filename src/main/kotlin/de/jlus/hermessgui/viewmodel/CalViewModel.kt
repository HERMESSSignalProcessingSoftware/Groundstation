package de.jlus.hermessgui.viewmodel

import de.jlus.hermessgui.model.Calibrations
import de.jlus.hermessgui.model.Dapi
import de.jlus.hermessgui.model.Dataframe
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ListChangeListener
import javafx.scene.chart.XYChart
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import tornadofx.*
import java.io.File
import java.io.IOException
import java.lang.NullPointerException


/**
 * The ViewModel connecting the Calibration model with the CalTab
 */
class CalViewModel(initItem: Calibrations): ItemViewModel<Calibrations>(initItem) {
    private val projectVm by inject<ProjectViewModel>()
    private val dataframes = observableListOf<Dataframe>()
    val calibrationsRunning = SimpleBooleanProperty(false)

    val calName = bind(Calibrations::calName)
    val targetSize = bind(Calibrations::targetSize)
    val actualSize = bind(Calibrations::actualSize)
    val datapointsSgr1 = List(6) { observableListOf<XYChart.Data<Number, Number>>() }
    val datapointsSgr2 = List(6) { observableListOf<XYChart.Data<Number, Number>>() }
    val datapointsRtd = List(6) { observableListOf<XYChart.Data<Number, Number>>() }


    fun startDataAcquision () {
        if (Dapi.dpReceiver == null) {
            dataframes.clear()
            actualSize.value = 0
            calibrationsRunning.value = true
            Dapi.dpReceiver = this
            Dapi.commandSetLiveDataAcquisition(true)
        }
    }


    fun stopDataAcquisition () {
        Dapi.commandSetLiveDataAcquisition(false)
        calibrationsRunning.value = false
        Dapi.dpReceiver = null
    }


    /**
     * Make sure to keep the dataframes list and the measurement values in sync
     */
    init {
        rebind {
            dataframes.bind(item.dataframes) { it }
        }
        dataframes.onChange { change: ListChangeListener.Change<out Dataframe> ->
            while (change.next()) {
                // handle the added items
                if (change.wasAdded()) {
                    for (df in change.addedSubList) {
                        datapointsSgr1[df.stampId].add(XYChart.Data(df.timestamp.inWholeSeconds, df.sgr1))
                        datapointsSgr2[df.stampId].add(XYChart.Data(df.timestamp.inWholeSeconds, df.sgr2))
                        datapointsRtd[df.stampId].add(XYChart.Data(df.timestamp.inWholeSeconds, df.rtd))
                    }
                }
                if (change.wasRemoved()) {
                    // handled removed items
                    for (df in change.removed) {
                        datapointsSgr1[df.stampId].removeIf { it.xValue == df.timestamp.inWholeSeconds }
                        datapointsSgr2[df.stampId].removeIf { it.xValue == df.timestamp.inWholeSeconds }
                        datapointsRtd[df.stampId].removeIf { it.xValue == df.timestamp.inWholeSeconds }
                    }
                }
            }
        }
    }


    /**
     * inserts the received datapackage to the calibration model
     * and marks the model dirty
     */
    fun receiveDatapackage (dp: List<Dataframe>) {
        for (df in dp) {
            dataframes.add(df)
            actualSize.value++
        }
        if (actualSize.value >= targetSize.value)
            stopDataAcquisition()
    }


    /**
     * after flushing the data to the model this function will be called automatically. It will
     * store the data in the filesystem.
     */
    override fun onCommit(commits: List<Commit>) {
        // check if project is loaded and a parent directory is available
        val dir = projectVm.directory.value
        if (!projectVm.isOpened.value || dir == null)
            error("Could not save to file, because no project within a directory is loaded.")

        // check, if user should confirm overwriting a file, when the filename has changed
        // and the new file already exists
        val newFile = File(dir, "${calName.value}.hercal")
        if (commits.any { it.property == calName && it.changed } && newFile.exists()) {
            alert(
                Alert.AlertType.CONFIRMATION,
                "Overwrite file?",
                "The file in ${newFile.absolutePath} already exists. Are you sure you want to override?",
                ButtonType.YES, ButtonType.NO
            ) {
                if (it == ButtonType.NO)
                    return@onCommit
            }
        }

        val frameCopy = List(dataframes.size) { dataframes[it] }
        item.dataframes.clear()
        item.dataframes.addAll(frameCopy)
        // write to file
        try {
            item.saveToFile(newFile)
            projectVm.refreshFileLists()
        }
        catch (ex: IOException) {
            error(ex.message ?: ex.stackTraceToString())
        }
    }


    /**
     * reads the file from the specified name within project path, if it exists
     * @return true, if loading was successful
     */
    fun loadFile (file: File): Boolean {
        try {
            item.readFromFile(file)
            return true
        }
        catch (ex: IOException) {
            error(ex.message ?: ex.stackTraceToString())
        }
        catch (ex: NullPointerException) {
            error(ex.message ?: ex.stackTraceToString())
        }
        return false
    }
}