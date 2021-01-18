package de.jlus.seriesdb.view

import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import tornadofx.Fragment
import tornadofx.select


/**
 * Extend this to be shown in a MainTab
 * Make sure to set tab.content = root in initialization
 */
abstract class MainTab(title: String): Fragment() {
    val tab = Tab(title)
}


/**
 * Helper function for creation of new MainTab
 * @param constructor You can pass a constructor as a lambda like ::MyMainTabClass
 */
fun <T: MainTab> TabPane.mainTab (constructor: () -> T, f: T.() -> Unit = {}): T {
    val newTab = constructor()
    tabs.add(newTab.tab)
    newTab.f()
    newTab.tab.select()
    return newTab
}