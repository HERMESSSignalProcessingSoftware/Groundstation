package de.jlus.seriesdb.view

import tornadofx.*


/**
 *
 */
class EditProjectTab : MainTab("Edit Project") {
    override fun saveResource(): Boolean { println("Saved"); return true }


    override fun closeResource(): Boolean {
        confirm("Really close") {
            return super.closeResource()
        }
        return false
    }


    override val root = form {
        fieldset("Test") {
            field("Test") {
                textfield()
            }
        }
    }
}