package com.ravi.vicaramactest

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var wifiManager: WifiManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setButtonActions()
        setWifiService()
        setBluetoothService()
    }

    /**
     * Set button action listeners
     */
    private fun setButtonActions() {

        btnStartService.let {
            it.setOnClickListener {
                log("START THE FOREGROUND SERVICE ON DEMAND")
                actionOnService(Actions.START)
            }
        }

        btnStopService.let {
            it.setOnClickListener {
                log("STOP THE FOREGROUND SERVICE ON DEMAND")
                actionOnService(Actions.STOP)
            }
        }
    }

    /**
     * Initialize wifi manager and set wifi switch listeners
     */
    private fun setWifiService() {
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        wifi_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                wifiManager?.isWifiEnabled = true
                wifi_text.text = getString(R.string.wifi_on)
                wifi_icon.setColorFilter(resources.getColor(R.color.status_on));
            } else {
                wifiManager?.isWifiEnabled = false
                wifi_text.text = getString(R.string.wifi_off)
                wifi_icon.setColorFilter(resources.getColor(R.color.status_off));
            }
        }
    }

    /**
     * Initialize BT adapter and BT data set and set switch listners
     */
    private fun setBluetoothService() {

        val adapter = BluetoothAdapter.getDefaultAdapter()

        if (adapter.isEnabled) {
            bt_text.text = getString(R.string.bt_on)
            bt_switch.isChecked = true
            bluetooth_icon.setColorFilter(resources.getColor(R.color.status_on));
        } else {
            bt_text.text = getString(R.string.bt_off)
            bt_switch.isChecked = false
            bluetooth_icon.setColorFilter(resources.getColor(R.color.status_off));
        }

        bt_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                adapter.enable()
                bt_text.text = getString(R.string.bt_on)
                bluetooth_icon.setColorFilter(resources.getColor(R.color.status_on));
            } else {
                adapter.disable()
                bt_text.text = getString(R.string.bt_off)
                bluetooth_icon.setColorFilter(resources.getColor(R.color.status_off));
            }
        }
    }


    override fun onStart() {
        super.onStart()
        /** Register broadcast receivers */
        val intentFilter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(broadCastReceiver, intentFilter)
        registerReceiver(broadCastReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        /** Unregister broadcast receivers */
        unregisterReceiver(broadCastReceiver)
    }

    private val broadCastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            /** Set changes in bluetooth text, switch and image when bluetooth state is changed  */

            val adapter = BluetoothAdapter.getDefaultAdapter()
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        adapter.disable()
                        bt_switch.isChecked = false
                        bt_text.text = getString(R.string.bt_off)
                        bluetooth_icon.setColorFilter(resources.getColor(R.color.status_off));
                    }

                    BluetoothAdapter.STATE_ON -> {
                        adapter.enable()
                        bt_switch.isChecked = true
                        bt_text.text = getString(R.string.bt_on)
                        bluetooth_icon.setColorFilter(resources.getColor(R.color.status_on));
                    }
                }
            }


            /** Set changes in wifi text, switch and image when wifi state is changed  */

            val wifiStateExtra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN)
            when (wifiStateExtra) {
                WifiManager.WIFI_STATE_ENABLED -> {
                    wifi_switch?.isChecked = true
                    wifi_text?.text = getString(R.string.wifi_on)
                    wifi_icon.setColorFilter(resources.getColor(R.color.status_on));
                }
                WifiManager.WIFI_STATE_DISABLED -> {
                    wifi_switch?.isChecked = false
                    wifi_text?.text = getString(R.string.wifi_off)
                    wifi_icon.setColorFilter(resources.getColor(R.color.status_off));
                }
            }
        }
    }

    /**
     * Start/Stop service based on actions
     */
    private fun actionOnService(action: Actions) {
        if (getServiceState(this) == ServiceState.STOPPED && action == Actions.STOP) return

        Intent(this, EndlessService::class.java).also {
            it.action = action.name

            /** for oreo and above  start foreground service and return */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(it)
                return
            }
            /** for android below oreo start service */
            startService(it)
        }
    }

}