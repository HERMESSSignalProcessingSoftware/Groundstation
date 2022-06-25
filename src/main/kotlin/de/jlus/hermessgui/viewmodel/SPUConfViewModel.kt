package de.jlus.hermessgui.viewmodel

import de.jlus.hermessgui.model.Dapi
import de.jlus.hermessgui.model.SPUConfig
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import tornadofx.*
import java.io.File
import java.io.IOException
import java.lang.NullPointerException


/**
 * The ViewModel connecting an SPUConfig model with the SPUConfigTab
 */
class SPUConfViewModel(initItem: SPUConfig): ItemViewModel<SPUConfig>(initItem) {
    private val projectVm by inject<ProjectViewModel>()

    // general SPU settings
    val confName = bind(SPUConfig::confNameProperty)
    // dms adc settings
    val sgrOffsetCalInit = bind(SPUConfig::sgrOffsetCalInitProperty)
    val sgrSamplerate = bind(SPUConfig::sgrSamplerateProperty)
    val sgrPGA = bind(SPUConfig::sgrPGAProperty)
    // pt100 adc settings
    val rtdOffsetCalInit = bind(SPUConfig::rtdOffsetCalInitProperty)
    val rtdSamplerate = bind(SPUConfig::rtdSamplerateProperty)
    val rtdPGA = bind(SPUConfig::rtdPGAProperty)
    // data storage settings
    val storeOnSodsEnabled = bind(SPUConfig::storeOnSodsEnabledProperty)
    val clearOnSoeEnabled = bind(SPUConfig::clearOnSoeEnabledProperty)
    val storeMinTime = bind(SPUConfig::storeMinTimeProperty)
    val storeMaxTime = bind(SPUConfig::storeMaxTimeProperty)
    // TM settings
    val tmEnabled = bind(SPUConfig::tmEnabledProperty)


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
        val newFile = File(dir, "${confName.value}.herconf")
        if (commits.any { it.property == confName && it.changed } && newFile.exists()) {
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


    /**
     * Writes the currently saved configuration to the SPU. This function should therefore only be called when
     * the model is not dirty.
     */
    fun writeSpuConfig () {
        Dapi.commandWriteSPUConf(item)
    }


    /**
     * Reads the configuration and updates all fields in the viewmodel but not the underlying model
     */
    fun readSpuConfig () {
        Dapi.confReceiver = this
        Dapi.commandReadSPUConf()
    }
}