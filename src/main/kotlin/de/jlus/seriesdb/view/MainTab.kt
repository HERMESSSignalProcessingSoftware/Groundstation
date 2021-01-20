package de.jlus.seriesdb.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import tornadofx.Fragment
import tornadofx.onChange


/**
 * Extend this to be shown in a MainTab
 * To initiate an instance prefer using the builder function MainView::openMainTab
 * After creation lateinit should be called
 * @param title The title to show in the tab bar
 * @param initialTabId If null, no ID is associated
 */
abstract class MainTab(title: String, initialTabId: String? = null) : Fragment() {
    var tab = Tab(title)
    val isProjectDirty = SimpleBooleanProperty(false)
    var tabTitle: String = title
        set(value) { field = value; tab.text = value }
    var tabId: String? = initialTabId
        set(value) {
            // check if the ID exists already and does not allow the ID to be renamed
            if (value == null) {
                field = null
                return
            }
            if (field != value && findByTabId<MainTab>(value) == null)
                field = value
        }


    /**
     * Performs the save operation, when the superior project is saving
     * If tab can not save, make sure to inform the user by triggering an error window
     * @return true, if saving succeeded
     */
    open fun saveResource(): Boolean = true


    /**
     * Performs the save operation, when the superior project is saving
     * If tab can not save, make sure to inform the user by triggering an error window
     * @return true, if saving succeeded
     */
    open fun onProjectSave(): Boolean = saveResource()


    /**
     * Performs the close tab operation, when the superior project is closing
     * @return true, if allow to be closed
     */
    open fun onProjectClose(): Boolean {
        if (closeResource()) {
            mainTabPane?.tabs?.remove(tab)
            allOpenTabs.remove(this)
            return true
        }
        return false
    }


    /**
     * Performs the close tab operation
     * If tab can not close, make sure to inform the user by triggering an error window.
     * @return true, if allow to be closed
     */
    open fun closeResource(): Boolean {
        if (saveResource())
            return true
        tornadofx.error("Could not save resource")
        return false
    }


    /**
     * Does all the lateinit and must be called
     */
    fun lateinit() {
        // save an instant of tabPane, but only once
        if (mainTabPane == null) {
            mainTabPane = tab.tabPane
            mainTabPane!!.tabs.onChange { change ->
                if (change.list.size == 1)
                    change.list.forEach { it.isClosable = false }
                else
                    change.list.forEach { it.isClosable = true }
            }
        }

        // make the content visible
        tab.content = root

        // add handler to call when the tab was closed
        tab.setOnCloseRequest {
            // screw it. The tab freezes, if I consume the event in onCloseRequest
            val pos = mainTabPane!!.tabs.indexOf(tab)
            if (!closeResource()) {
                tab = Tab(tabTitle)
                mainTabPane!!.tabs.add(pos, tab)
                lateinit()
            }
            // do not allow close last tab
            allOpenTabs.remove(this)
        }

        // add to the list of all open tabs
        allOpenTabs.add(this)
    }


    companion object {
        val allOpenTabs = mutableListOf<MainTab>()
        private var mainTabPane: TabPane? = null

        /**
         * Find any open MainTab by the tabId and the given Type
         * @return null, if no tab was found
         */
        inline fun <reified T : MainTab> findByTabId(tabId: String?): T? {
            if (tabId == null)
                return null

            for (elem in allOpenTabs) {
                if (elem.tabId == tabId && elem is T)
                    return elem
            }
            return null
        }
    }
}