package de.jlus.hermessgui.app

import javafx.scene.image.Image

// versions
const val thisVersion = "0.0.1"
const val dapiVersion = "0.0.1"
const val tmVersion = "0.0.1"

// tabIds
const val tabIdEditProject = "PROJECT:EDIT"

// images
val imgDirectory20 = Image("imgs/icon-directory-20.png")
val imgTooltip = Image("imgs/icon-tooltip-12.png")
val imgAdd = Image("imgs/icon-add.png")

// regex
val regexProjectName = Regex("[A-Za-z0-9]{1,20}")