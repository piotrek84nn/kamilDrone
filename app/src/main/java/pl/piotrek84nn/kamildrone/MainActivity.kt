package pl.piotrek84nn.kamildrone

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pl.piotrek84nn.kamildron.R
import java.util.*
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity() {
    private lateinit var droneControlScreen: FloatingActionButton
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaPlayer = MediaPlayer.create(this, R.raw.wifi_tello)
        mediaPlayer!!.isLooping = false
        requestWindowFeature(Window.FEATURE_NO_TITLE) // remove title bar from android screen
        supportActionBar!!.hide()
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = Color.parseColor("#000000")
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.parseColor("#000000")
        }
        droneControlScreen = findViewById(R.id.introImage)
        droneControlScreen.setOnClickListener { v: View? ->
            val droneControlScreenIntent =
                Intent(this@MainActivity, DroneController::class.java)
            startActivity(droneControlScreenIntent)
        }

        startActivity(Intent(WifiManager.ACTION_PICK_WIFI_NETWORK))
        Toast.makeText(
            this@MainActivity,
            "Połącz tablet z siecią o nazwie TELLO",
            Toast.LENGTH_LONG
        ).show()

        Timer().schedule(1000) {
            playAudio()
        }
    }

    private fun playAudio() {
        mediaPlayer!!.start()
    }

    override fun onResume() {
        super.onResume()
    }

}

