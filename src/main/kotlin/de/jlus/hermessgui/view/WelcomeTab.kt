package de.jlus.hermessgui.view

import de.jlus.hermessgui.app.*
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontPosture
import tornadofx.*


/**
 * Displays a welcome message with about information
 */
class WelcomeTab : MainTab("Welcome") {


    override val root = textflow {
        paddingAll = 12

        text("Welcome to Preliminary HERMESS SPU interface software\n\n") {
            fill = Color.BLUE
            font = Font(20.0)
        }

        imageview(Image("imgs/hermess_logo.png")) {
            fitWidth = 200.0
            fitHeight = 200.0
        }
        text("\t")
        imageview(Image("imgs/rexus_logo.png")) {
            fitWidth = 200.0
            fitHeight = 200.0
        }

        text("\n\nVersion: $thisVersion\n" +
                "Compatible with DAPI protocol: $dapiVersion\n" +
                "Compatible with TM protocol: $tmVersion\n\n") {
            font = Font(12.0)
            style {
                fontStyle = FontPosture.ITALIC
            }
        }

        text("""Developed and maintained by and for REXUS-HERMESS
                |
                |This Version is able to communicate with the HERMESS Signal Processing Unit (SPU) via the DAPI 
                |protocol version $dapiVersion. For more documentation on the versions of the HERMESS software stack 
                |(SPU flight software, interface software, ground station software) check out the HERMESS
                |repositories on GitHub.
                |
                |All HERMESS software products use semantic versioning.
            """.trimMargin()) {
            font = Font(16.0)
        }

    }
}