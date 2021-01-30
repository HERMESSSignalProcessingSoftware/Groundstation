package de.jlus.hermessgui.app

import javafx.scene.image.Image

// versions
const val thisVersion = "0.0.1"
const val dapiVersion = "0.0.1"
const val tmVersion = "0.0.1"

// tabIds
const val tabIdEditProject = "PROJECT:EDIT"
const val tabIdSPUConfigPrefix = "SPUCONF:"

// images
val imgDirectory20 = Image("imgs/icon-directory-20.png")
val imgTooltip = Image("imgs/icon-tooltip-12.png")
val imgAdd = Image("imgs/icon-add.png")
val imgRefresh16 = Image("imgs/icon-refresh-16.png")

// regex
val regexProjectName = Regex("[A-Za-z0-9]{1,20}")
val regexConfName = Regex("[A-Za-z0-9]{1,20}")

// enums for SPU configuration
enum class SPUConfOverrideMode (val text: String) {
    NONE("No override (jumper setting)"),
    PRIMARY("Primary"),
    SECONDARY("Secondary")
}
enum class SPUConfSamplerate (val numeric: Int, val text: String) {
    SR_5(5, "5 SPS"),
    SR_10(10, "10 SPS"),
    SR_20(20, "20 SPS"),
    SR_40(40, "40 SPS"),
    SR_80(80, "80 SPS"),
    SR_160(160, "160 SPS"),
    SR_320(320, "320 SPS"),
    SR_640(640, "640 SPS"),
    SR_1000(1000, "1k SPS"),
    SR_2000(2000, "2k SPS")
}
enum class SPUConfPGA (val numeric: Int) {
    PGA_1(1),
    PGA_2(2),
    PGA_4(4),
    PGA_8(8),
    PGA_16(16),
    PGA_32(32),
    PGA_64(64),
    PGA_128(128)
}