package de.jlus.seriesdb.view

import de.jlus.seriesdb.app.Styles
import tornadofx.*

class MainView : View("Series database") {
    override val root = hbox {
        label(title) {
            addClass(Styles.heading)
        }
    }
}