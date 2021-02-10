package de.jlus.hermessgui.viewmodel

import de.jlus.hermessgui.model.Calibrations
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

    val calName = bind(Calibrations::calName)
    val stamps = Array(3) {Stamp(this, it)}


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


    class Stamp (vm: CalViewModel, index: Int) {
        val dms1Ofc = vm.bind {vm.item.stamps[index].dms1Ofc}
        val dms1Fsc = vm.bind {vm.item.stamps[index].dms1Fsc}
        val dms2Ofc = vm.bind {vm.item.stamps[index].dms2Ofc}
        val dms2Fsc = vm.bind {vm.item.stamps[index].dms2Fsc}
        val tempOfc = vm.bind {vm.item.stamps[index].tempOfc}
        val tempFsc = vm.bind {vm.item.stamps[index].tempFsc}
    }
}