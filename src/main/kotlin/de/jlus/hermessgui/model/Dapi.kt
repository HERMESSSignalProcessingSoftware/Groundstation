package de.jlus.hermessgui.model

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_CTS_ENABLED
import com.fazecast.jSerialComm.SerialPortEvent
import com.fazecast.jSerialComm.SerialPortMessageListener
import de.jlus.hermessgui.app.*
import de.jlus.hermessgui.viewmodel.LoggerViewModel
import javafx.beans.property.SimpleObjectProperty
import tornadofx.find
import tornadofx.observableListOf


/**
 * Manages all aspects regarding the DAPI connection
 */
class Dapi {
    val activePortProperty = SimpleObjectProperty<SerialPort?>(null)
    val ports = observableListOf<SerialPort>()

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
                        else -> logger.warning("Unrecognized command")
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
     * Performs the internal handling of received string messages
     */
    private fun handleStringMessage (content: ByteArray) {
        // in case a new message was received
        val msg = content.decodeToString(endIndex = content.size-1)
        when (content[content.size - 1]) {
            '0'.code.toByte() -> logger.info(msg, "DAPI")
            '1'.code.toByte() -> logger.warning(msg, "DAPI")
            '2'.code.toByte() -> logger.error(msg, "DAPI")
            else -> logger.warning("Malformed DAPI message received")
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
}