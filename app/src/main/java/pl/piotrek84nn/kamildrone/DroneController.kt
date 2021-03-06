package pl.piotrek84nn.kamildrone

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.controlwear.virtual.joystick.android.JoystickView
import pl.piotrek84nn.kamildron.R
import pl.piotrek84nn.tello.TelloStatusReceiver
import java.io.IOException
import java.net.*
import java.nio.charset.StandardCharsets
import java.util.*


class DroneController : AppCompatActivity() {
    private var droneWIFI_TV: TextView? = null
    private var droneHeight_TV: TextView? = null
    private var droneBattery_TV: TextView? = null
    private var currentHeightOfDron = 0.0
    private var connectionFlag = false
    private var isStatusAvailable = false
    private var flipCounter = 0;

    private var mediaPlayer: MediaPlayer? = null
    private var statusReceiver: TelloStatusReceiver? = null
    private val RC = intArrayOf(0, 0, 0, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
        makeFlipAction()
        makeCurveAction()
        actionBtnCreate()
        leftJoystickCreate()
        rightJoystickCreate()

        connectDrone();
    }

    private fun initUI() {
        requestWindowFeature(Window.FEATURE_NO_TITLE) // remove title bar from android screen
        Objects.requireNonNull(supportActionBar)?.hide()
        setContentView(R.layout.activity_drone_controller)
        droneBattery_TV = findViewById(R.id.droneBattery)
        droneHeight_TV = findViewById(R.id.droneHeight)
        droneWIFI_TV = findViewById(R.id.droneWIFI)
        with(droneWIFI_TV) { this?.setBackgroundResource(R.drawable.rounded_corner_red) }
    }

    private fun connectDrone() {
        if(connectionFlag) return;

        sendCommandToTello("command")
        if (statusReceiver == null) {
            statusReceiver = TelloStatusReceiver(this)
            statusReceiver!!.start()
            sendCommandToTello("speed 10")
        }
        connectionFlag = true
    }

    private fun disconnectDrone() {
        if(!connectionFlag) return;

        sendCommandToTello("disconnect")
        isStatusAvailable = false;
        connectionFlag = false
        statusReceiver?.kill()
        Toast.makeText(this@DroneController, "Roz????czony", Toast.LENGTH_SHORT).show()
    }

    private fun makeFlipAction() {
        val connection = findViewById<FloatingActionButton>(R.id.makeFlip)
        connection.setOnClickListener { v: View? ->
            if(connectionFlag) {
                when (flipCounter % 4) {
                    0 -> sendCommandToTello("flip f")
                    1 -> sendCommandToTello("flip b")
                    2 -> sendCommandToTello("flip l")
                    3 -> sendCommandToTello("flip r")
                }
                flipCounter++
            }
        }
    }

    private fun makeCurveAction() {
    }

    private fun actionBtnCreate() {
        val actionTakeOff = findViewById<ImageView>(R.id.takeoff)
        actionTakeOff.setOnClickListener { v: View? ->
            if (connectionFlag) {
                sendCommandToTello("takeoff") // send takeoff command
                    var mediaPlayer = MediaPlayer.create(this, R.raw.start)
                    mediaPlayer!!.isLooping = false
                    mediaPlayer!!.start()
            }
        }

        val actionLnad = findViewById<ImageView>(R.id.land)
        actionLnad.setOnClickListener { _: View? ->
            if (connectionFlag) {
                sendCommandToTello("land") // send land command
                var mediaPlayer = MediaPlayer.create(this, R.raw.manual_land)
                mediaPlayer!!.isLooping = false
                mediaPlayer!!.start()

            }
        }
    }

    private fun leftJoystickCreate() {
        val leftJoystick = findViewById<View>(R.id.joystickViewLeft) as JoystickView
        leftJoystick.setOnMoveListener { angle: Int, strength: Int ->
            var sinus = Math.sin(Math.toRadians(angle.toDouble()));// up/down
            var cosinus = Math.cos(Math.toRadians(angle.toDouble()));// round left/round right

            RC[2] = ((strength * sinus)/2).toInt()
            RC[3] = ((strength * cosinus)/2).toInt()

            sendCommandToTello("rc ${RC[0]} ${RC[1]} ${RC[2]} ${RC[3]}")
            Arrays.fill(RC, 0)
        }
    }

    private fun rightJoystickCreate() {
        val rightJoystick = findViewById<View>(R.id.joystickViewRight) as JoystickView
        rightJoystick.setOnMoveListener { angle: Int, strength: Int ->
            var sinus = Math.sin(Math.toRadians(angle.toDouble()));// up/down
            var cosinus = Math.cos(Math.toRadians(angle.toDouble()));// round left/round right

            RC[1] = ((strength * sinus)/2).toInt()
            RC[0] = ((strength * cosinus)/2).toInt()

            sendCommandToTello("rc ${RC[0]} ${RC[1]} ${RC[2]} ${RC[3]}")
            Arrays.fill(RC, 0)
        }
    }

    private fun sendCommandToTello(strCommand: String) {
        Thread {
            try {
                if (strCommand == "disconnect") {
                    if (statusReceiver != null) statusReceiver!!.kill()
                    statusReceiver = null
                }
                val udpSocket = DatagramSocket(null)
                udpSocket.reuseAddress = true;
                udpSocket.broadcast = true;
                val serverAddr = InetAddress.getByName(HOST)
                val buf = strCommand.toByteArray(StandardCharsets.UTF_8)
                val packet = DatagramPacket(buf, buf.size, serverAddr, PORT)
                udpSocket.send(packet)
                udpSocket.close()
            } catch (e: SocketException) {
                Log.e("Socket Open:", "Error:", e)
            } catch (e: UnknownHostException) {
                Log.e("Socket Open:", "Error:", e)
            } catch (e: IOException) {
                Log.e("IOException", "error", e)
            }
        }.start()
    }

    var updateInfo = Runnable {
        if(!isStatusAvailable) {
            var mediaPlayer = MediaPlayer.create(this, R.raw.connection_ok)
            mediaPlayer!!.isLooping = false
            mediaPlayer!!.start()
            Toast.makeText(this@DroneController, "Po????czony", Toast.LENGTH_SHORT).show()
            isStatusAvailable = true;
        }
        try {
            droneBattery_TV!!.text = "BAT: " + statusReceiver!!.lastDec[10] + "%"
            if (statusReceiver!!.lastDec[10].toInt() <= 15) {
                droneBattery_TV!!.setBackgroundResource(R.drawable.rounded_corner_red)
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(this, R.raw.land)
                    mediaPlayer!!.isLooping = true
                    mediaPlayer!!.start()
                }
            } else {
                droneBattery_TV!!.setBackgroundResource(R.drawable.rounded_corner_green)
            }
            if (statusReceiver!!.lastDec[10].toInt() != 0) {
                droneWIFI_TV!!.setBackgroundResource(R.drawable.rounded_corner_green)
                droneWIFI_TV!!.text = "WIFI - OK"
            }
            currentHeightOfDron = statusReceiver!!.lastDec[9].toDouble()
            droneHeight_TV!!.text = statusReceiver!!.lastDec[9]
        } catch (e: Exception) {
            Log.e("Array out of bounds", "error", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        statusReceiver?.kill()
        statusReceiver = null

        disconnectDrone();
    }

    companion object {
        const val HOST = "192.168.10.1"
        const val UTF_8 = "UTF-8"
        const val PORT = 8889
    }
}