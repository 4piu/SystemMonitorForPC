package com.notaworkshop.systemmonitorforpc

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.collection.CircularArray
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.*
import org.json.JSONObject

private const val POLLING_INTERVAL = 500L
private const val MAX_HISTORY = 120

class MonitorFragment : Fragment() {
    companion object {
        private val TAG = MonitorFragment::class.qualifiedName
    }
    private var pollingJob: Job = Job()
    private var lastPolling = 0L
    private var url = ""
    private var isAuth = false
    private var username = ""
    private var password = ""
    private val history = CircularArray<JSONObject>(120)

    private val onPrefChange = SharedPreferences.OnSharedPreferenceChangeListener {
            sharedPreferences, key ->
        Log.w(TAG, "CHANGED") // TODO not working
    }

    override fun onStart() {
        super.onStart()
        // get settings value
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val host = sharedPref.getString("preference_host", "")
        val port = Integer.valueOf(sharedPref.getString("preference_port", "80"))
        url = "http://${host}:${port}"
        isAuth = sharedPref.getBoolean("preference_basic_auth", false)
        username = sharedPref.getString("preference_auth_username", "")!!
        password = sharedPref.getString("preference_auth_password", "")!!
        sharedPref.registerOnSharedPreferenceChangeListener(onPrefChange)
    }

    override fun onStop() {
        super.onStop()
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.unregisterOnSharedPreferenceChangeListener(onPrefChange)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // start polling
        pollingJob = lifecycleScope.launch {
            while (pollingJob.isActive) {
                lastPolling = System.currentTimeMillis()
                pollingStats()
                delay(lastPolling + POLLING_INTERVAL - System.currentTimeMillis())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_monitor, container, false)
    }

    private suspend fun pollingStats() {
        withContext(Dispatchers.Default) {
            // make request
            val queue = Volley.newRequestQueue(context)
            val request = object : JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener<JSONObject> {
                    if (history.size()>= MAX_HISTORY) {
                        history.popFirst()
                    }
                    history.addLast(it)
                },
                Response.ErrorListener {
                    Log.w(TAG, "${it.message}")
                    // blank data
                    if (history.size()>= MAX_HISTORY) {
                        history.popFirst()
                    }
                    history.addLast(null)
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    if (isAuth) {
                        params["Authorization"] = "Basic " + Base64.encodeToString(
                            "${username}:${password}".toByteArray(),
                            Base64.DEFAULT
                        )
                    }
                    return params
                }
            }
            queue.add(request)
        }
        withContext(Dispatchers.Main) {
            updateStatsView()
        }
    }

    private fun updateStatsView() {
        val text: TextView? = view?.findViewById(R.id.dummy_text)
    }
}