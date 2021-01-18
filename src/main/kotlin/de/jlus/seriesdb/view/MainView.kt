package de.jlus.seriesdb.view

import com.sun.javafx.collections.ObservableListWrapper
import de.jlus.seriesdb.app.*
import de.jlus.seriesdb.controller.MainController
import javafx.beans.property.*
import javafx.geometry.Side
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.layout.Priority
import javafx.scene.paint.Paint
import javafx.stage.Modality
import tornadofx.*
import tornadofx.controlsfx.*
import java.io.File


/**
 * Generates the main window
 */
class MainView : View("Preliminary HERMESS SPU Interface software") {
    private val controller: MainController by inject()

    private val statusText = SimpleStringProperty("Not connected")
    private val progressValue = SimpleDoubleProperty(0.0)
    private val tabPane = TabPane()


    override val root = borderpane {
        top = menubar {
            menu("Project") {
                item("New project").action(::newProjectDialog)
                item("Open project")
                item("Close project") {
                    disableWhen(controller.projectViewModel.isDummy)
                }
                item("Reload project") {
                    disableWhen(controller.projectViewModel.isDummy)
                }
                separator()
                item("Save project") {
                    disableWhen(controller.projectViewModel.isDummy)
                }
                item("Save As project") {
                    disableWhen(controller.projectViewModel.isDummy)
                }
                separator()
                item("Application settings")
                separator()
                item("Exit application")
            }

            menu("SPU Interfacing") {
                menu("DAPI: Connect") {
                    disableWhen(controller.dapiConnected)
                    item("Reload ports")
                    separator()
                }
                item("DAPI: Disconnect") {
                    enableWhen(controller.dapiConnected)
                }
                item("DAPI: Configure") {
                    enableWhen(controller.dapiConnected)
                }
                item("DAPI: Readout") {
                    enableWhen(controller.dapiConnected)
                }
                separator()
                menu("TM: Connect") {
                    disableWhen(controller.tmConnected)
                    item("Reload ports")
                    separator()
                }
                item("TM: Disconnect") {
                    enableWhen(controller.tmConnected)
                }
                item("TM: Life view") {
                    enableWhen(controller.tmConnected)
                }
            }

            menu("About") {
                item("About this application") {
                    action { tabPane.mainTab(::WelcomeTab) }
                }
                separator()
                item("HERMESS Website") {
                    action { hostServices.showDocument("https://www.project-hermess.com") }
                }
                item("GitHub Repositories") {
                    action { hostServices.showDocument("https://github.com/HERMESSSignalProcessingSoftware") }
                }
                item("DAPI protocol specification") {
                    // TODO link to DAPI spec
                }
                item("TM protocol specification") {
                    // TODO link to TM spec
                }
                separator()
                item("V. $thisVersion") {
                    disableProperty().set(true)
                }
            }
        }

        center = splitpane {
            // after window was initialized, but the separator in a nice position
            subscribe<WindowFirstShow> {
                this@splitpane.setDividerPosition(0, 0.2)
            }

            // the left menu
            drawer {
                minWidth = 200.0
                dockingSide = Side.LEFT
                multiselect = true
                item("Project config", ImageView("imgs/icon-config-20.png"), true) {
                    form {
                        fieldset("Projects name") {
                            field("Name:") {
                                label("Example project") {
                                    isWrapText = true
                                }
                            }
                            field("Location:") {
                                label("C://Benutzer/Jonathan/Desktop/anotherPath/test/undoweite/esGehtLange") {
                                    isWrapText = true
                                }
                            }
                            field("Description:") {
                                label("Hier kann ein längerer Text stehen und so weiter.. Es ist alles denkbar...")
                            }
                        }
                        button("Edit")
                    }
                }
                item("SPU configs", ImageView("imgs/icon-spu-20.png"), true) {
                    listview(tree.children) {
                        cellFormat {
                            textProperty().bind(it.descriptor)
                        }
                    }
                }
                item("Measurements", ImageView("imgs/icon-measurement-20.png"), true) {
                    listview(tree.children) {
                        cellFormat {
                            textProperty().bind(it.descriptor)
                        }
                    }
                }
            }

            // the main space with the TabPane
            add(tabPane.apply {
                minWidth = 500.0
                tabClosingPolicy = TabPane.TabClosingPolicy.ALL_TABS
                tabDragPolicy = TabPane.TabDragPolicy.REORDER
                mainTab(::WelcomeTab)
            })

        }

        bottom = statusbar(statusText, progressValue) {}
    }


    /**
     * Opens a dialog for a new project
     */
    fun newProjectDialog () {
        dialog("New Project", Modality.APPLICATION_MODAL) {
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
                    button(graphic = ImageView("imgs/icon-directory-20.png")).action {
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
                                controller.createNewProject(finalName, finalLocation)
                                close()
                            }
                        }
                    }
                    button("Cancel").action(::close)
                }
            }
        }
    }
}


/**
 * Represents a single item in the TreeView on the left side
 * TODO remove all this shit
 */
class ProjectOverviewItem(descriptor: String) {
    val descriptor = SimpleStringProperty(descriptor)
    val children = ObservableListWrapper(mutableListOf<ProjectOverviewItem>())
    val fillColor = SimpleObjectProperty(Paint.valueOf("#000000"))

    /**
     * create ProjectOverviewItem and add to this children
     */
    fun item(descriptor: String, f: ProjectOverviewItem.() -> Unit = {}): ProjectOverviewItem {
        val item = ProjectOverviewItem(descriptor)
        item.f()
        children.add(item)
        return item
    }
}

val tree = ProjectOverviewItem("Project overview").apply {
    item("Project configuration") {
        item("Test")
        fillColor.set(Paint.valueOf("#006000"))
    }
    item("SPU configuration files")
    item("Measurement readout files")
}