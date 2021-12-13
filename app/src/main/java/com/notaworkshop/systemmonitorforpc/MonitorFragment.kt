package com.notaworkshop.systemmonitorforpc

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap


class MonitorFragment : Fragment() {
    companion object {
        private val TAG = MonitorFragment::class.qualifiedName

        // TODO convert the constants to preferences
        private const val POLLING_INTERVAL = 500L
        private const val REQ_TIMEOUT = 1000
        private const val HISTORY_SIZE = 120
    }

    private var pollingJob: Job? = null
    private var lastPolling = 0L
    private var url = ""
    private var isAuth = false
    private var username = ""
    private var password = ""
    private var requestQueue: RequestQueue? = null
    private val history = LinkedList<JSONObject?>()// replace array with linked list

    private val onPrefChange =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            loadPreference(sharedPreferences)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestQueue =
            Volley.newRequestQueue(context)  // creating request queue in loop causes memory leak
        // init history
        while (history.size < HISTORY_SIZE) {
            history.add(null)
        }
        // get settings value
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        loadPreference(sharedPreferences)
        sharedPreferences.registerOnSharedPreferenceChangeListener(onPrefChange)
        // start polling
        pollingJob = pollingStats()
    }

    override fun onDestroy() {
        super.onDestroy()
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.unregisterOnSharedPreferenceChangeListener(onPrefChange)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_monitor, container, false)
        childFragmentManager
            .beginTransaction()
            .replace(R.id.meter_2, CpuMeterFragment())
            .replace(R.id.chart_1, CoreUtilizationFragment())
            .replace(R.id.chart_2, NetworkActivityFragment())
            .replace(R.id.chart_3, DiskIoFragment())
            .commit()
        return view
    }

    private fun loadPreference(sharedPreferences: SharedPreferences) {
        val host = sharedPreferences.getString("preference_host", "")
        val port = Integer.valueOf(sharedPreferences.getString("preference_port", "80")!!)
        url = "http://${host}:${port}"
        isAuth = sharedPreferences.getBoolean("preference_basic_auth", false)
        username = sharedPreferences.getString("preference_auth_username", "")!!
        password = sharedPreferences.getString("preference_auth_password", "")!!
    }

    private fun pollingStats(): Job {
        return lifecycleScope.launch {
            while (isActive) {
                lastPolling = System.currentTimeMillis()
                // make request
                val request = object : JsonObjectRequest(Request.Method.GET, url, null,
                    Response.Listener { updateStats(it) },
                    Response.ErrorListener { updateStats(null) }) {
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
                request.retryPolicy = DefaultRetryPolicy(
                    REQ_TIMEOUT,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
                requestQueue?.add(request)
                delay(lastPolling + POLLING_INTERVAL - System.currentTimeMillis())
            }
        }
    }

    private fun updateStats(data: JSONObject?) {
        // uses one global object for history storage
        Log.d(TAG, "updateStats")
        history.removeFirst()
        history.add(data)
        // reduce duplicate code by using interface
        for (frag: Fragment in childFragmentManager.fragments) {
            if (frag is HistoryViewer) frag.updateView(history, HISTORY_SIZE)
        }
    }
}