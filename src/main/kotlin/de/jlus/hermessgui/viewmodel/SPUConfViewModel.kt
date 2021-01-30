package de.jlus.hermessgui.viewmodel

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
    val ovrdMode = bind(SPUConfig::ovrdModeProperty)
    val sleep = bind(SPUConfig::sleepProperty)
    // dms adc settings
    val dmsOffsetCalInit = bind(SPUConfig::dmsOffsetCalInitProperty)
    val dmsFullscaleCalInit = bind(SPUConfig::dmsFullscaleCalInitProperty)
    val dmsSamplerate = bind(SPUConfig::dmsSamplerateProperty)
    val dmsPGA = bind(SPUConfig::dmsPGAProperty)
    // pt100 adc settings
    val pt100OffsetCalInit = bind(SPUConfig::pt100OffsetCalInitProperty)
    val pt100FullscaleCalInit = bind(SPUConfig::pt100FullscaleCalInitProperty)
    val pt100Samplerate = bind(SPUConfig::pt100SamplerateProperty)
    val pt100PGA = bind(SPUConfig::pt100PGAProperty)
    // data storage settings
    val storeMeasurementsEnabled = bind(SPUConfig::storeMeasurementsEnabledProperty)
    val storeMetadataEnabled = bind(SPUConfig::storeMetadataEnabledProperty)
    val storeStartOnLo = bind(SPUConfig::storeStartOnLOProperty)
    val storeStartOnSOE = bind(SPUConfig::storeStartOnSOEProperty)
    val storeStartOnSODS = bind(SPUConfig::storeStartOnSODSProperty)
    val storeStopOnLo = bind(SPUConfig::storeStopOnLOProperty)
    val storeStopOnSOE = bind(SPUConfig::storeStopOnSOEProperty)
    val storeStopOnSODS = bind(SPUConfig::storeStopOnSODSProperty)
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
    fun loadFile (name: String): Boolean {
        // check if project is loaded and a parent directory is available
        val dir = projectVm.directory.value
        if (!projectVm.isOpened.value || dir == null)
            error("Could not read from file, because no project within a directory is loaded.")

        // read from file
        try {
            item.readFromFile(File(dir, "$name.herconf"))
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