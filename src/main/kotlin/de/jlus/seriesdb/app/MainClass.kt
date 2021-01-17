package de.jlus.seriesdb.app

import de.jlus.seriesdb.view.MainView
import javafx.scene.image.Image
import javafx.stage.Stage
import tornadofx.App
import tornadofx.launch
import tornadofx.onChangeOnce
import tornadofx.setStageIcon

class MainClass: App(MainView::class, Styles::class) {
    override fun start(stage: Stage) {
        stage.isMaximized = true
        stage.showingProperty().onChangeOnce {
            // TODO set divider positions
        }
        setStageIcon(Image("imgs/icon.png"))
        super.start(stage)
    }
}

fun main (args: Array<String>) =
    launch<MainClass>(args)