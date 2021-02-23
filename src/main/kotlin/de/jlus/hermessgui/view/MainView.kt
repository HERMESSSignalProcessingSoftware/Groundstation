package de.jlus.hermessgui.view

import de.jlus.hermessgui.app.*
import de.jlus.hermessgui.view.MainTab.Companion.findByTabId
import de.jlus.hermessgui.viewmodel.*
import javafx.beans.property.*
import javafx.geometry.Side
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.stage.FileChooser
import javafx.stage.Modality
import tornadofx.*
import tornadofx.controlsfx.*
import java.io.File


/**
 * Generates the main window
 */
class MainView : View("Preliminary HERMESS SPU Interface software") {
    private val projectVm by inject<ProjectViewModel>()
    private val dapiVm = DapiViewModel()
    private val tmVm = TmViewModel()

    private val statusText = SimpleStringProperty("Not connected")
    private val progressValue = SimpleDoubleProperty(0.0)
    private val tabPane = TabPane()


    override val root = borderpane {
        top = menubar {
            menu("Project") {
                item("Open project").action(::openProject)
                item("Close project") {
                    action(projectVm::closeProject)
                }
                item("Reload project") {
                    enableWhen(projectVm.isOpened)
                    action(projectVm::reloadProject)
                }
                separator()
                item("Save project").action(::saveProject)
                item("Save As project") {
                    enableWhen(projectVm.isOpened)
                    action(::saveProjectAs)
                }
                separator()
                item("Exit application").action {
                    currentStage?.close()
                }
            }

            menu("SPU Interfacing") {
                // TODO most actions missing
                menu("DAPI: Connect") {
                    enableWhen(dapiVm.itemProperty.isNull)
                    item("Reload ports")
                    separator()
                }
                item("DAPI: Disconnect") {
                    disableWhen(dapiVm.itemProperty.isNull)
                }
                item("DAPI: Configure").action { openMainTab(::SPUConfigTab) }
                item("DAPI: ADC calibrations").action { openMainTab(::CalTab) }
                item("DAPI: Readout measurements")
                separator()
                menu("TM: Connect") {
                    enableWhen(tmVm.itemProperty.isNull)
                    item("Reload ports")
                    separator()
                }
                item("TM: Disconnect") {
                    disableWhen(tmVm.itemProperty.isNull)
                }
                item("TM: Life view") {
                    disableWhen(tmVm.itemProperty.isNull)
                }
            }

            menu("About") {
                item("About this application") {
                    action { openMainTab(::WelcomeTab, "welcome") }
                }
                separator()
                item("HERMESS Website") {
                    action { hostServices.showDocument("https://www.project-hermess.com") }
                }
                item("GitHub Repositories") {
                    action { hostServices.showDocument("https://github.com/HERMESSSignalProcessingSoftware") }
                }
                item("DAPI protocol specification") {
                    action { hostServices.showDocument("https://github.com/HERMESSSignalProcessingSoftware/" +
                            "VersionedSpecs/blob/main/DAPI.md") }
                }
                item("TM protocol specification") {
                    action { hostServices.showDocument("https://github.com/HERMESSSignalProcessingSoftware/" +
                            "VersionedSpecs/blob/main/TM.md") }
                }
                separator()
                item("V. $thisVersion") {
                    disableProperty().set(true)
                }
            }
        }

        center = splitpane {
            // after window was initialized, but the separator in a nice position
            subscribe<WindowWasMaximized> {
                this@splitpane.setDividerPosition(0, 0.2)
            }

            // the left menu
            drawer {
                minWidth = 200.0
                dockingSide = Side.LEFT
                multiselect = true
                item("Project config", ImageView("imgs/icon-config-20.png"), true, true) {
                    form {
                        fieldset("Project configuration") {
                            field("Name:") {
                                label(projectVm.name)
                            }
                            field("Location:") {
                                val locationButton = button(graphic=ImageView(imgDirectory20)) {
                                    enableWhen(projectVm.isOpened)
                                }
                                val locationLabel = label("UNSAVED PROJECT") {
                                    hgrow = Priority.ALWAYS
                                }
                                projectVm.directory.onChange {
                                    locationLabel.text = it?.absolutePath ?: "UNSAVED PROJECT"
                                    if (it == null)
                                        return@onChange
                                    locationButton.action {
                                        hostServices.showDocument(it.absolutePath)
                                    }
                                }
                            }
                        }
                        buttonbar {
                            enableWhen(projectVm.isOpened)
                            button("Reload dir", graphic = ImageView(imgRefresh16)) {
                                action(projectVm::refreshFileLists)
                            }
                            button("Description") {
                                action { openMainTab(::EditProjectTab, tabIdEditProject) }
                            }
                        }
                    }
                }
                // spu config drawer item
                item("SPU configs", ImageView("imgs/icon-spu-20.png"), true, true) {
                    vbox {
                        listview(projectVm.spuConfFiles) {
                            configureFileList {
                                openMainTab(::SPUConfigTab, tabIdSPUConfigPrefix + it.name) {
                                    loadResource(it.file)
                                }
                            }
                        }
                        button("Add new configuration file", graphic = ImageView(imgAdd)) {
                            fitToParentWidth()
                            enableWhen(projectVm.isOpened)
                            action { openMainTab(::SPUConfigTab) }
                        }
                    }
                }
                // calibration drawer item
                item("Calibrations", ImageView("imgs/icon-cal-20.png"), false, true) {
                    vbox {
                        listview(projectVm.calFiles) {
                            configureFileList {
                                openMainTab(::CalTab, tabIdCalPrefix + it.name) {
                                    loadResource(it.file)
                                }
                            }
                        }
                        button("Add new calibrations file", graphic = ImageView(imgAdd)) {
                            fitToParentWidth()
                            enableWhen(projectVm.isOpened)
                            action { openMainTab(::CalTab) }
                        }
                    }
                }
                // measurements drawer item
                item("Measurements", ImageView("imgs/icon-measurement-20.png"), true, true) {
                    listview(projectVm.measurementFiles) {
                        configureFileList {  } // TODO
                    }
                }
            }

            // the main space with the TabPane
            add(tabPane.apply {
                minWidth = 500.0
                tabClosingPolicy = TabPane.TabClosingPolicy.ALL_TABS
                tabDragPolicy = TabPane.TabDragPolicy.REORDER
                openMainTab(::WelcomeTab, "welcome") {
                    tab.isClosable = false
                }
            })

        }

        bottom = statusbar(statusText, progressValue) {}
    }


    init {
        shortcut("Ctrl+O", ::openProject)
        shortcut("Ctrl+S", ::saveProject)
        shortcut("Ctrl+Shift+S", ::saveProjectAs)
    }


    /**
     * open a dialog for requesting the open project
     */
    private fun openProject () {
        val selection = chooseFile(
                "Select the project file to open",
                arrayOf(FileChooser.ExtensionFilter("HERMESS project file", "*.herpro")),
                File(System.getProperty("user.home")),
                FileChooserMode.Single,
                currentWindow
        )
        if (selection.size == 1)
            projectVm.openProject(selection[0])
    }


    /**
     * Save the project either in bg or request for a new path
     */
    private fun saveProject () {
        if (projectVm.isOpened.value)
            projectVm.saveProject()
        else
            saveProjectAs()
    }


    /**
     * Opens a dialog for saving a project
     */
    private fun saveProjectAs () {
        dialog("Save Project", Modality.APPLICATION_MODAL) {
            setPrefSize(500.0, 150.0)
            val name = SimpleStringProperty()
            val location = SimpleObjectProperty(File(System.getProperty("user.home")))
            gridpane {
                hgap = 12.0
                vgap = 12.0
                row {
                    label("A new directory containing the project files will be generated.") {
                        gridpaneConstraints {
                            columnSpan = 3
                        }
                    }
                }
                row {
                    label("New Project name: ")
                    textfield(name) {
                        gridpaneConstraints {
                            columnSpan = 2
                        }
                    }
                }
                row {
                    label("Location: ")
                    label(location.value.toString()) {
                        location.onChange {
                            text = it.toString()
                        }
                        gridpaneConstraints {
                            hGrow = Priority.ALWAYS
                        }
                    }
                    button(graphic = ImageView(imgDirectory20)).action {
                        location.value = chooseDirectory(
                                "Select parent directory for new project",
                                location.value
                        )
                    }
                }
                row {
                    button("Create project") {
                        isDefaultButton = true
                        action {
                            val finalName = name.value ?: ""
                            val finalLocation = location.value
                            if (!finalName.matches(regexProjectName))
                                error("Name must match $regexProjectName")
                            else if (finalLocation == null || !finalLocation.isDirectory || !finalLocation.exists())
                                error("Location must be set to a directory")
                            else {
                                projectVm.saveProjectAs(finalLocation.resolve("$finalName/$finalName.herpro"))
                                close()
                            }
                        }
                    }
                    button("Cancel").action(::close)
                }
            }
        }
    }



    /**
     * Helper function for creation of new MainTab
     * @param constructor You can pass a constructor as a lambda like ::MyMainTabClass
     * @param tabId the ID associated with a tab. If not null, it will try to open any associated tab
     * with that id. Could it not found such a tab, the newly created tab will have this tabId.
     * @param f the builder function. Only applied, if a new instance was created
     */
    private inline fun <reified T: MainTab> openMainTab (
            constructor: () -> T,
            tabId: String? = null,
            f: T.() -> Unit = {}
    ): T {
        // check, if tab with its id is already open
        val openTab = findByTabId<T>(tabId)
        if (openTab != null) {
            openTab.tab.select()
            return openTab
        }

        val newTab = constructor()
        tabPane.tabs.add(newTab.tab)
        newTab.tabId = tabId
        newTab.lateinit()
        newTab.f()
        newTab.tab.select()
        return newTab
    }


    private fun ListView<ProjectFileEntry>.configureFileList (onOpen: (ProjectFileEntry) -> Unit) {
        val open = fun () {
            val sel = selectedItem
            if (sel != null)
                onOpen(sel)
        }

        cellFormat {
            text = it.name
        }

        onDoubleClick(open)
        setOnKeyPressed { if (it.code == KeyCode.ENTER) open() }
        contextmenu {
            item("Open") {
                enableWhen(selectionModel.selectedItemProperty().isNotNull)
                action(open)
            }
            item("Delete file") {
                enableWhen(selectionModel.selectedItemProperty().isNotNull)
                action {
                    val sel = selectedItem ?: return@action
                    confirm("Delete file?", "Are you sure you want to delete ${sel.name}?") {
                        sel.file.delete()
                        projectVm.refreshFileLists()
                    }
                }
            }
        }
    }
}