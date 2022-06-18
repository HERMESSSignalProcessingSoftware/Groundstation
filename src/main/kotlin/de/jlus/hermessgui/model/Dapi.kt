package de.jlus.hermessgui.model

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_CTS_ENABLED
import com.fazecast.jSerialComm.SerialPortEvent
import com.fazecast.jSerialComm.SerialPortMessageListener
import de.jlus.hermessgui.app.*
import de.jlus.hermessgui.viewmodel.CalViewModel
import de.jlus.hermessgui.viewmodel.LoggerViewModel
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import tornadofx.find
import tornadofx.observableListOf
import kotlin.time.Duration.Companion.microseconds


/**
 * Manages all aspects regarding the DAPI connection
 */
object Dapi {
    val activePortProperty = SimpleObjectProperty<SerialPort?>(null)
    val ports = observableListOf<SerialPort>()
    var dpReceiver: CalViewModel? = null

    private val logger = find<LoggerViewModel>()


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
            && port.setComPortParameters(dapiBaudrate, dapiDataBits, dapiStopBits, dapiParity)
            && port.setFlowControl(FLOW_CONTROL_CTS_ENABLED)) {
            // add listener for when a new log message arrives
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

                    // log, if malformed success byte
                    val successByte = p0.receivedData[size - 3].toInt()
                    if (!listOf(0x0F, 0xF0).contains(successByte))
                        return logger.warning("Malformed DAPI response: Uncertain success byte")
                    val success = successByte == 0x0F
                    val content = p0.receivedData.sliceArray(IntRange(1, size - 4))

                    when (p0.receivedData[0].toInt()) {
                        0x00 -> handleStringMessage(content)
                        0x03 -> handleLiveData(content)
                        else -> logger.warning("Unrecognized command " + p0.receivedData.toHex())
                    }
                }
            })

            // successfully connected
            activePortProperty.value = port
            logger.info("Dapi connection to ${port.descriptivePortName} requested")
            // try to probe the SPU and request an answer
            commandEcho("Dapi connection to ${port.descriptivePortName} opened")
        }

        // could not connect
        else {
            // close in case it was opened
            port.removeDataListener()
            port.closePort()
            logger.warning("Dapi could not connect to ${port.descriptivePortName}: Serial port failure")
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
        logger.info("Dapi connection closed")
    }


    /**
     * Requests a message to be retransmitted. Will show up in the log as an Info message.
     */
    fun commandEcho (msg: String, severity: Logger.LoggingSeverity = Logger.LoggingSeverity.INFO) {
        val bytes = byteArrayOf(0x00.toByte()) +
                msg.encodeToByteArray(throwOnInvalidSequence = true) +
                byteArrayOf(severity.ordinal.digitToChar().code.toByte())
        sendCommand(bytes)
    }


    /**
     * Starts the live data acquisition
     * @param start true, if the SPU shall send live feed
     */
    fun commandSetLiveDataAcquisition (start: Boolean) {
        sendCommand(byteArrayOf(if (start) 0x03 else 0x04))
    }


    /**
     * Performs the internal handling of received string messages
     */
    private fun handleStringMessage (content: ByteArray) {
        // in case a new string message was received
        val msg = content.decodeToString(endIndex = content.size-1)
        when (content[content.size - 1]) {
            '0'.code.toByte() -> logger.info(msg, "DAPI")
            '1'.code.toByte() -> logger.warning(msg, "DAPI")
            '2'.code.toByte() -> logger.error(msg, "DAPI")
            else -> logger.warning("Malformed DAPI message received")
        }
    }


    /**
     * decodes and dissiminates live DAPI live data
     */
    private fun handleLiveData (content: ByteArray) {
        val numDataframes = content[0].toInt()
        val stampValues = mutableListOf<Dataframe>()

        // get timestamp from bytes 1 to 8
        var timestamp: ULong = 0u
        for (i in 0..7) {
            timestamp = (timestamp shl 8) or (content[i + 1].toULong() and 0xFFu)
        }
        val ts = timestamp.toLong().microseconds * 250

        // i know the data conversions look terrible: kotlin makes me do that, because it implicitly carries
        // the negative bit everywhere it can
        for (i in 0 until numDataframes) {
            stampValues.add(Dataframe(
                stampId = content[(i*8)+9].toInt(),
                errAdcLagging = (content[(i*8)+10].toUInt() and 0x01U) != 0x00U,
                errStampLagging = (content[(i*8)+10].toUInt() and 0x02U) != 0x00U,
                errNoNew = (content[(i*8)+10].toUInt() and 0x04U) != 0x00U,
                errOverwritten = (content[(i*8)+10].toUInt() and 0x08U) != 0x00U,
                sgr1 = (content[(i*8)+11].toInt() shl 8) or ((content[(i*8)+12].toInt()) and 0xff),
                sgr2 = (content[(i*8)+13].toInt() shl 8) or ((content[(i*8)+14].toInt()) and 0xff),
                rtd = (content[(i*8)+15].toInt() shl 8) or ((content[(i*8)+16].toInt()) and 0xff),
                timestamp = ts
            ))
        }

        Platform.runLater {
            dpReceiver?.receiveDatapackage(stampValues)
        }
    }


    /**
     * Sends the necessary checks to send a command and padds it required by the DAPI specifications
     */
    private fun sendCommand (cmdAndContent: ByteArray) {
        if (cmdAndContent.size > 62)
            logger.warning("Could not send command: Too large content size")
        val paddingSize = 8 - ((cmdAndContent.size + 2) % 8)

        // assemble
        val bytes = ByteArray(cmdAndContent.size + paddingSize + 2) { 0x00.toByte() }
        cmdAndContent.copyInto(bytes)
        bytes[cmdAndContent.size] = 0x17.toByte()
        bytes[bytes.size - 1] = 0xF0.toByte()

        // send command
        activePortProperty.value?.writeBytes(bytes, bytes.size.toLong())
    }

    private fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
}