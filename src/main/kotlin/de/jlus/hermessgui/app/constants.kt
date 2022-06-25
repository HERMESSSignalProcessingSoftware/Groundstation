package de.jlus.hermessgui.app

import com.fazecast.jSerialComm.SerialPort
import javafx.scene.image.Image

// versions
const val thisVersion = "1.1.0"
const val dapiVersion = "1.1.0"
const val tmVersion = "Not supported"

// tabIds
const val tabIdEditProject = "PROJECT:EDIT"
const val tabIdSPUConfigPrefix = "SPUCONF:"
const val tabIdCalPrefix = "CAL:"

// images
val imgDirectory20 = Image("imgs/icon-directory-20.png")
val imgTooltip = Image("imgs/icon-tooltip-12.png")
val imgAdd = Image("imgs/icon-add.png")
val imgRefresh16 = Image("imgs/icon-refresh-16.png")
val imgClear16 = Image("imgs/icon-clear-16.png")

// regex
val regexProjectName = Regex("""[A-Za-z\d]{1,20}""")
val regexFileName = Regex("""[A-Za-z\d _]{0,20}[A-Za-z\d]""")
val regexSPUConfName = Regex("""[A-Za-z\d _]{0,15}[A-Za-z\d]""")

// dapi connection
const val dapiBaudrate = 115200
const val dapiParity = SerialPort.EVEN_PARITY
const val dapiStopBits = SerialPort.ONE_STOP_BIT
const val dapiDataBits = 8

// enums for SPU configuration
const val maxRecordingTimeSeconds = 3600
enum class SPUConfSamplerate (val numeric: Int, val text: String) {
    SR_5(0, "5 SPS"),
    SR_10(1, "10 SPS"),
    SR_20(2, "20 SPS"),
    SR_40(3, "40 SPS"),
    SR_80(4, "80 SPS"),
    SR_160(5, "160 SPS"),
    SR_320(6, "320 SPS"),
    SR_640(7, "640 SPS"),
    SR_1000(8, "1k SPS"),
    SR_2000(9, "2k SPS");
    companion object {
        fun fromNumeric (numeric: Int): SPUConfSamplerate {
            for (item in SPUConfSamplerate.values())
                if (item.numeric == numeric)
                    return item
            return SR_5
        }
    }
}
enum class SPUConfPGA (val numeric: Int, val text: String) {
    PGA_1(0 shl 4, "1"),
    PGA_2(1 shl 4, "2"),
    PGA_4(2 shl 4, "4"),
    PGA_8(3 shl 4, "8"),
    PGA_16(4 shl 4, "16"),
    PGA_32(5 shl 4, "32"),
    PGA_64(6 shl 4, "64"),
    PGA_128(7 shl 4, "128");
    companion object {
        fun fromNumeric (numeric: Int): SPUConfPGA {
            for (item in SPUConfPGA.values())
                if (item.numeric == numeric)
                    return item
            return PGA_1
        }
    }
}
enum class SPUConfCalibrationTypes (val numeric: Int) {
    None(0),
    SelfOffset(2),
    SystemOffset(1);
    companion object {
        fun fromNumeric (numeric: Int): SPUConfCalibrationTypes {
            for (item in SPUConfCalibrationTypes.values())
                if (item.numeric == numeric)
                    return item
            return None
        }
    }
}