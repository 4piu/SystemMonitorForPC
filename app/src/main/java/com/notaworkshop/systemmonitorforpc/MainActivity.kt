package com.notaworkshop.systemmonitorforpc

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import java.lang.System.currentTimeMillis

class MainActivity : AppCompatActivity() {
    private var pressAgainToExit = 0L
    private var isFullScreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container_view, MonitorFragment())
            .commit()
    }

    override fun onBackPressed() {
        when {
            pressAgainToExit + 2000 > currentTimeMillis() -> {
                super.onBackPressed()
                return
            }
            isFullScreen -> {
                quitFullScreen()
            }
            else -> {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.exit_confirm),
                    Snackbar.LENGTH_SHORT
                ).show()
                pressAgainToExit = currentTimeMillis()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.actionbar_menu, menu)
        return true
    }

    private fun fullScreen() {
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        // hide action bar
        supportActionBar?.hide()
        isFullScreen = true
    }

    private fun quitFullScreen() {
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        // show action bar
        supportActionBar?.show()
        isFullScreen = false
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_fullscreen -> {
                fullScreen()
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}