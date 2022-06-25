package de.jlus.hermessgui.model

import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ObservableList


/**
 * Performs data integrity checks on received dataframes
 */
class DataIntegrityChecker (collection: ObservableList<Dataframe>) {
    val isValidProperty = SimpleBooleanProperty(false)
}