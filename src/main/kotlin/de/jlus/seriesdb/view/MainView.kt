package de.jlus.seriesdb.view

import com.sun.javafx.collections.ObservableListWrapper
import de.jlus.seriesdb.app.*
import javafx.beans.property.*
import javafx.geometry.Side
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.paint.Paint
import tornadofx.*
import tornadofx.controlsfx.*


/**
 * Generates the main window
 */
class MainView : View("Preliminary HERMESS SPU Interface software") {
    val statusText = SimpleStringProperty("Not connected")
    val progressValue = SimpleDoubleProperty(0.0)
    val tabPane = TabPane()
    val dapiConnected = SimpleBooleanProperty(false)
    val tmConnected = SimpleBooleanProperty(false)


    override val root = borderpane {
        top = menubar {
            menu("File") {
                item("Open project")
                item("Reload project") {
                    disableProperty().set(true)
                }
                item("Close project") {
                    disableProperty().set(true)
                }
                item("Save project")
                item("Save As project")
                separator()
                item("Application settings")
                separator()
                item("Exit application")
            }

            menu("SPU Interfacing") {
                menu("DAPI: Connect") {
                    disableWhen(dapiConnected)
                    item("Reload ports")
                    separator()
                }
                item("DAPI: Disconnect") {
                    enableWhen(dapiConnected)
                }
                item("DAPI: Configure") {
                    enableWhen(dapiConnected)
                }
                item("DAPI: Readout") {
                    enableWhen(dapiConnected)
                }
                separator()
                menu("TM: Connect") {
                    disableWhen(tmConnected)
                    item("Reload ports")
                    separator()
                }
                item("TM: Disconnect") {
                    enableWhen(tmConnected)
                }
                item("TM: Life view") {
                    enableWhen(tmConnected)
                }
            }

            menu("About") {
                item("About this application") {
                    action { tabPane.welcomeTab() }
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
                item("V. 0.0.1") {
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
                welcomeTab()
            })

        }

        bottom = statusbar(statusText, progressValue) {}
    }
}


/**
 * Represents a single item in the TreeView on the left side
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

    fun onOpen() {
        println("Opened" + descriptor.value)
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