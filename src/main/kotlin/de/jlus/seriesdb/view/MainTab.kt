package de.jlus.seriesdb.view

import javafx.scene.Parent
import javafx.scene.control.Tab
import tornadofx.Fragment

/**
 * Extend this to be shown in a MainTab
 * Make sure to follow conventions listed below
 */
abstract class MainTab(title: String): Fragment() {
    val tab = Tab(title)
}


/*
class WelcomeTab : MainTab("Welcome") {
    override val root = ...
    init {
        tab.content = root
    }
}

Helper function for building
fun TabPane.[some]Tab(f: [some]Tab.() -> Unit = {}): [some]Tab {
    val newTab = [some]Tab()
    tabs.add(newTab.tab)
    newTab.f()
    newTab.tab.select()
    return newTab
}*/