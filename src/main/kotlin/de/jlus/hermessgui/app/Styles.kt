package de.jlus.hermessgui.app

import javafx.geometry.Pos
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.px

class Styles : Stylesheet() {
    companion object {
        val heading by cssclass()
        val indicator by cssclass()
    }

    init {
        label and heading {
            padding = box(10.px)
            fontSize = 20.px
            fontWeight = FontWeight.BOLD
        }


        indicator {
            borderStyle += BorderStrokeStyle.DASHED
            borderColor += box(Color.DARKORANGE)
            borderWidth += box(3.px)
            borderRadius += box(3.px)
            padding = box(10.px)
            alignment = Pos.CENTER
            spacing = 3.px

            child("HBox") {
                spacing = 5.px
                alignment = Pos.CENTER
                child(checkBox) {
                    opacity = 1.0
                    child(".box") {
                        padding = box(3.px)
                        backgroundColor += Color.WHITE
                        borderColor += box(Color.BLACK)
                        borderWidth += box(3.px)
                        borderRadius += box(3.px)
                        child(".mark") {
                            padding = box(8.px)
                        }
                    }
                }
            }
            child("Label") {
                fontWeight = FontWeight.BOLD
            }
        }
    }
}