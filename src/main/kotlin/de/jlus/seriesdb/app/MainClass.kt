package de.jlus.seriesdb.app

import de.jlus.seriesdb.view.MainView
import tornadofx.App
import tornadofx.launch

class MainClass: App(MainView::class, Styles::class)

fun main (args: Array<String>) =
    launch<MainClass>(args)