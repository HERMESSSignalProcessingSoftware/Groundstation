package de.jlus.seriesdb.app

import de.jlus.seriesdb.view.MainView
import javafx.scene.image.Image
import javafx.stage.Stage
import tornadofx.*


/**
 * Start the application
 */
class MainClass: App(MainView::class, Styles::class) {
    override fun start(stage: Stage) {
        stage.isMaximized = true
        stage.showingProperty().onChangeOnce {
            fire(WindowFirstShow)
        }
        setStageIcon(Image("imgs/icon.png"))
        super.start(stage)
    }
}


/**
 * Helper for packaging
 */
fun main (args: Array<String>) =
    launch<MainClass>(args)