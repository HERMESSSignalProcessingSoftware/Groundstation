package de.jlus.hermessgui.app

import de.jlus.hermessgui.view.MainView
import de.jlus.hermessgui.viewmodel.*
import javafx.scene.image.Image
import javafx.stage.Stage
import tornadofx.*
import kotlin.system.exitProcess


/**
 * Start the application
 */
class MainClass: App(MainView::class, Styles::class) {
    private val projectVm by inject<ProjectViewModel>()


    init {
        reloadStylesheetsOnFocus()
    }


    override fun start(stage: Stage) {
        stage.isMaximized = true
        setStageIcon(Image("imgs/icon.png"))

        stage.showingProperty().onChangeOnce {
            fire(WindowWasMaximized)
        }
        stage.maximizedProperty().onChange {
            if (it)
                fire(WindowWasMaximized)
        }

        stage.setOnCloseRequest {
            it.consume()
            if (!projectVm.closeProject())
                confirm("Are you sure you want to exit?", "Not all changes were saved!") {
                    stage.close()
                    exitProcess(0)
                }
            else {
                stage.close()
                exitProcess(0)
            }
        }


        super.start(stage)
    }
}


/**
 * Helper for packaging
 */
fun main (args: Array<String>) =
    launch<MainClass>(args)