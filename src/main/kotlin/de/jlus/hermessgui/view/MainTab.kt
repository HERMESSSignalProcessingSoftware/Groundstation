package de.jlus.hermessgui.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ButtonType
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import tornadofx.Fragment
import tornadofx.information
import tornadofx.onChange


/**
 * Extend this to be shown in a MainTab<br>
 * To initiate an instance prefer using the builder function MainView::openMainTab<br>
 * After creation lateinit should be called<br>
 * The default configuration for this tab is a ProjectTab, which will close (and save) itself with the project.
 * If you want to override this behavior, override onProjectClose and onProjectSave with "= true"
 * @param title The title to show in the tab bar
 * @param initialTabId If null, no ID is associated
 */
abstract class MainTab(title: String, initialTabId: String? = null) : Fragment() {
    var tab = Tab(title)
    val isProjectTab = SimpleBooleanProperty(false)
    val isDirty = SimpleBooleanProperty(false)
    val tabTitle = SimpleStringProperty(title)
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
    open fun saveResource(): Boolean {
        isDirty.value = false
        return true
    }


    /**
     * Performs the save operation, when the superior project is saving
     * If tab can not save, make sure to inform the user by triggering an error window
     * @return true, if saving succeeded
     */
    open fun onProjectSave(): Boolean {
        if (isProjectTab.value)
            return saveResource()
        return true
    }


    /**
     * Performs the close tab operation, when the superior project is closing.
     * If it is a project related tab, prefer closing the tab, when the
     * resource was successfully closed.
     * @return true, if allow to be closed
     */
    open fun onProjectClose(): Boolean {
        if (isProjectTab.value) {
            if (!closeResource())
                return false
            closeTab()
        }
        return true
    }


    /**
     * Performs the close operation for the resource.
     * If tab can not close, make sure to inform the user by triggering an error window.
     * @return true, if allow to be closed
     */
    open fun closeResource(): Boolean {
        if (isDirty.value) {
            information(
                "The tab '${tabTitle.value}' contains unsaved changes",
                "Do you want to save them now?",
                ButtonType.YES, ButtonType.NO, ButtonType.CANCEL,
                owner = currentWindow
            ) {
                if (it == ButtonType.YES && !saveResource()) {
                    tornadofx.error("Could not save content of tab '${tabTitle.value}'.", owner=currentWindow)
                    return false
                }
                else if (it == ButtonType.CANCEL)
                    return false
            }
        }
        return true
    }


    /**
     * Close the tab, without calling any handlers.
     * This does not close the resource
     */
    fun closeTab() {
        mainTabPane?.tabs?.remove(tab)
        allOpenTabs.remove(this)
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
                tab = Tab(tab.text)
                mainTabPane!!.tabs.add(pos, tab)
                lateinit()
            }
            // do not allow close last tab
            allOpenTabs.remove(this)
        }

        // add to the list of all open tabs
        allOpenTabs.add(this)
    }


    /**
     * is being called, whenever one of these
     */
    private fun updateTabTitle () {
        val signsList = mutableListOf<String>()
        if (isDirty.value) signsList.add("*")
        if (isProjectTab.value) signsList.add("P")
        val signs = if (signsList.isEmpty()) "" else signsList.joinToString(prefix = "[", postfix = "] ")
        tab.text = "$signs${tabTitle.value}"
    }


    init {
        isProjectTab.onChange { updateTabTitle() }
        isDirty.onChange { updateTabTitle() }
        tabTitle.onChange { updateTabTitle() }
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