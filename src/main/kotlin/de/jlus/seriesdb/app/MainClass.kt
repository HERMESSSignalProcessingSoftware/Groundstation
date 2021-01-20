package de.jlus.seriesdb.app

import de.jlus.seriesdb.view.MainView
import de.jlus.seriesdb.viewmodel.*
import javafx.scene.image.Image
import javafx.stage.Stage
import tornadofx.*


/**
 * Start the application
 */
class MainClass: App(MainView::class, Styles::class) {
    private val projectVm by inject<ProjectViewModel>()


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
                }
            else
                stage.close()
        }


        super.start(stage)
    }
}


/**
 * Helper for packaging
 */
fun main (args: Array<String>) =
    launch<MainClass>(args)