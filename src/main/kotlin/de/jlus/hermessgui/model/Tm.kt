package de.jlus.hermessgui.model

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortEvent
import com.fazecast.jSerialComm.SerialPortMessageListener
import de.jlus.hermessgui.app.*
import de.jlus.hermessgui.viewmodel.LoggerViewModel
import de.jlus.hermessgui.viewmodel.ProjectViewModel
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import tornadofx.find
import tornadofx.observableListOf
import java.util.LinkedList

object Tm {
    val activePortProperty = SimpleObjectProperty<SerialPort?>(null)
    val ports = observableListOf<SerialPort>()

    val successRate = SimpleDoubleProperty(.0)
    val timestamp = SimpleIntegerProperty(0)
    val tmToggle = SimpleBooleanProperty(false)
    val restartAfterWdTriggered = SimpleBooleanProperty(false)
    val loAsserted = SimpleBooleanProperty(false)
    val soeAsserted = SimpleBooleanProperty(false)
    val sodsAsserted = SimpleBooleanProperty(false)
    val clearingMemory = SimpleBooleanProperty(false)
    val recordingToMemory = SimpleBooleanProperty(false)
    val wpAsserted = SimpleBooleanProperty(false)
    val memoryClearedBeforeSods = SimpleBooleanProperty(false)
    val numMessagesReceived = SimpleIntegerProperty(0)

    private val successList = LinkedList(List(15) {1})
    private var lastFrameId: UByte? = null
    private val timestampFragments = mutableListOf<Int>()
    private var capturingTimestampFragments = false
    private val textMsg = mutableListOf<Byte>()
    private val logger = find<LoggerViewModel>()
    private val projectVM = find<ProjectViewModel>()


    /**
     * Relead the ports observable list with possible candidates
     */
    fun reloadPorts () {
        for (port in SerialPort.getCommPorts()) {
            if (!ports.map { it.systemPortPath }.contains(port.systemPortPath))
                ports.add(port)
        }
    }


    /**
     * Connects to and configures a serial port for DAPI use.
     */
    fun connect (port: SerialPort) {
        // disconnect prior to connecting to a new port
        if (activePortProperty.value != null)
            disconnect()

        // open port
        if (port.openPort()
            && port.setComPortParameters(projectVM.tmBaudrate.value, tmDataBits, tmStopBits, tmParity)) {
            // add data listener
            port.addDataListener(object : SerialPortMessageListener {
                override fun getListeningEvents () =
                    SerialPort.LISTENING_EVENT_DATA_RECEIVED or SerialPort.LISTENING_EVENT_PORT_DISCONNECTED
                override fun getMessageDelimiter () = byteArrayOf(0x17.toByte(), 0xF0.toByte())
                override fun delimiterIndicatesEndOfMessage () = true
                override fun serialEvent (p0: SerialPortEvent?) {
                    p0 ?: return
                    // in case it simply disconnected
                    if (p0.eventType == SerialPort.LISTENING_EVENT_PORT_DISCONNECTED)
                        return disconnect()

                    // some content arrives
                    val size = p0.receivedData.size

                    // take note of any malformations
                    val receivedCrc: Int = (p0.receivedData[60].toUInt().shl(8)
                            or p0.receivedData[61].toUInt()).toInt()
                    val calculatedCrc = crc16.CCITT_FALSE(p0.receivedData, 0, 60)
                    if (size != 64) { //|| receivedCrc != calculatedCrc) {
                        // size or CRC wrong
                        updateSuccessRate(false)
                    }
                    else {
                        // toggle visual receiver
                        tmToggle.value = !tmToggle.value

                        // detect missing frames in between
                        val tmpLastFrameId = lastFrameId
                        val receivedFrameId = p0.receivedData[0].toUByte()
                        lastFrameId = (if (tmpLastFrameId == null || tmpLastFrameId >= receivedFrameId)
                            receivedFrameId
                        else
                            tmpLastFrameId.inc())
                        if (lastFrameId != receivedFrameId) {
                            // throw away all stored data in between frames
                            lastFrameId = receivedFrameId
                            updateSuccessRate(false)
                            textMsg.clear()
                            timestampFragments.clear()
                            capturingTimestampFragments = false
                        }
                        else {
                            // successfully received data with correct length and checksum
                            updateSuccessRate(true)

                            // decode string message
                            textMsg.addAll(p0.receivedData.slice(6..59))
                            if (textMsg.contains(0)) {
                                // a full message was read
                                val str = textMsg.toByteArray().toString(Charsets.US_ASCII).trim { it <= ' ' }
                                val groups = Regex("""^(.+)([012])$""",
                                    setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE)).find(str)?.groupValues
                                textMsg.clear()

                                if (groups == null && str.isNotBlank()) {
                                    logger.warning("Dropped corrupted TM text message")
                                }
                                else if (str.isNotBlank()) {
                                    Platform.runLater {
                                        numMessagesReceived.value++
                                    }
                                    groups!!
                                    if (groups[2] == "0") logger.info(groups[1], "TM")
                                    else if (groups[2] == "1") logger.warning(groups[1], "TM")
                                    else if (groups[2] == "2") logger.error(groups[1], "TM")
                                }
                            }
                        }

                        // update general status
                        val sta0 = p0.receivedData[1].toUInt()
                        restartAfterWdTriggered.value = sta0.shr(7).and(1U) == 1U
                        loAsserted.value = sta0.shr(6).and(1U) == 1U
                        soeAsserted.value = sta0.shr(5).and(1U) == 1U
                        sodsAsserted.value = sta0.shr(4).and(1U) == 1U
                        clearingMemory.value = sta0.shr(3).and(1U) == 1U
                        recordingToMemory.value = sta0.shr(2).and(1U) == 1U
                        wpAsserted.value = sta0.shr(1).and(1U) == 1U
                        memoryClearedBeforeSods.value = sta0.and(1U) == 1U
                        capturingTimestampFragments = p0.receivedData[2].toUInt().and(1U) == 1U

                        // store partial timestamp, even if some messages were skipped. In that case the
                        // previous timestampFragments were deleted anyway
                        if (capturingTimestampFragments || timestampFragments.size > 8) {
                            timestampFragments.clear()
                        }

                        timestampFragments.add(p0.receivedData[3].toInt())
                        if (timestampFragments.size == 8) {
                            var ts: ULong = 0u
                            for (i in 0..7) {
                                ts = (ts shl 8) or (timestampFragments[i].toULong() and 0xFFu)
                            }
                            Platform.runLater {
                                timestamp.value = (ts / 4000U).toInt()
                            }
                            timestampFragments.clear()
                        }
                    }
                }
            })
            // reset state
            textMsg.clear()
            timestampFragments.clear()
            capturingTimestampFragments = false
            numMessagesReceived.value = 0
            successRate.value = 0.0

            // inform the other members
            activePortProperty.value = port
            logger.info("TM connection to ${port.descriptivePortName} opened")
        }
    }


    /**
     * Closes the connection, reloads all ports and logs it
     */
    fun disconnect () {
        if (activePortProperty.value?.isOpen == true) {
            activePortProperty.value?.removeDataListener()
            activePortProperty.value?.closePort()
        }
        activePortProperty.value = null
        reloadPorts()
        logger.info("TM connection closed")
    }


    private fun updateSuccessRate (success: Boolean) {
        // add to end
        successList.add(if (success) 1 else 0)
        // remove first
        successList.remove()
        // fold from old to new
        Platform.runLater {
            successRate.value = successList.sum().toDouble() / 15
        }
    }
}