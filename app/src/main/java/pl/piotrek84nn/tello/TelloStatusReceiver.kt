package pl.piotrek84nn.tello

import pl.piotrek84nn.kamildrone.DroneController
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.regex.Matcher
import java.util.regex.Pattern

class TelloStatusReceiver(activity: DroneController) : Thread() {
    private val statePattern = Pattern.compile("-*\\d{0,3}\\.?\\d{0,2}[^\\D\\W\\s]")
    private val RECEIVER_PORT = 8890
    private val SLEEP_TIME = 500
    private var bKeepRunning = true
    private var socket: DatagramSocket? = null
    private var activity: DroneController? = activity
    var lastDec: MutableList<String> = ArrayList()

    override fun run() {
        var message: String?
        val buffer = ByteArray(256)
        val packet = DatagramPacket(buffer, buffer.size)
        try {
            socket = DatagramSocket(RECEIVER_PORT)
            while (bKeepRunning) {
                socket!!.receive(packet)
                message = String(buffer, 0, packet.length)
                val lastMessage: String = message
                if (lastMessage.isNotEmpty()) {
                    lastDec.clear()
                    val dcml: Matcher = statePattern.matcher(lastMessage)
                    while (dcml.find()) {
                        lastDec.add(dcml.group())
                    }
                    activity?.runOnUiThread(activity?.updateInfo)
                }
                sleep(SLEEP_TIME.toLong())
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        if (socket != null) {
            socket!!.close()
        }
    }

    fun kill() {
        bKeepRunning = false
    }
}
