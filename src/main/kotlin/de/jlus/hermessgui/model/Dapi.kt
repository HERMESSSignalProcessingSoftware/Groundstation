package de.jlus.hermessgui.model

import com.fazecast.jSerialComm.SerialPort
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
            && port.setComPortParameters(dapiBaudrate, dapiDataBits, dapiStopBits, dapiParity)) {
            // add listener for when a new log message arrives
            port.addDataListener(object : SerialPortMessageListener {
                override fun getListeningEvents () =
                    SerialPort.LISTENING_EVENT_DATA_RECEIVED or SerialPort.LISTENING_EVENT_PORT_DISCONNECTED
                override fun getMessageDelimiter () = byteArrayOf('\n'.code.toByte(), 0x00.toByte())
                override fun delimiterIndicatesEndOfMessage () = true
                override fun serialEvent (p0: SerialPortEvent?) {
                    p0 ?: return
                    // in case it simply disconnected
                    if (p0.eventType == SerialPort.LISTENING_EVENT_PORT_DISCONNECTED)
                        return disconnect()
                    // in case a new message was received
                    val msg = p0.receivedData.decodeToString(endIndex = p0.receivedData.size-4)
                    when (p0.receivedData[p0.receivedData.size-3]) {
                        '0'.code.toByte() -> logger.info(msg, "DAPI")
                        '1'.code.toByte() -> logger.warning(msg, "DAPI")
                        '2'.code.toByte() -> logger.error(msg, "DAPI")
                        else -> logger.warning("Malformed DAPI message received")
                    }
                }
            })

            // try to probe the SPU and request an answer
            port.writeBytes(byteArrayOf(0xAA.toByte()), 1)

            // successfully connected
            logger.info("Dapi connection to ${port.descriptivePortName} opened")
            activePortProperty.value = port
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
        if (activePortProperty.value?.isOpen == true)
            activePortProperty.value?.closePort()
        activePortProperty.value = null
        reloadPorts()
        logger.info("Dapi connection closed")
    }
}