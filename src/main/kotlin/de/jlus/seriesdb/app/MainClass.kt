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
    val projectVm by inject<ProjectViewModel>()


    override fun start(stage: Stage) {
        stage.isMaximized = true
        setStageIcon(Image("imgs/icon.png"))

        stage.showingProperty().onChangeOnce {
            fire(WindowFirstShow)
        }

        stage.setOnCloseRequest {
            it.consume()
            if (!projectVm.closeProject())
                confirm("Are you sure you want to exit?", "Not all changes were saved!") {
                    stage.close()
                }
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