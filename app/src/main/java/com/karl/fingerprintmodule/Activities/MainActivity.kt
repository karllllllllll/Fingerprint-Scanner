package com.karl.fingerprintmodule.Activities

import android.app.AlertDialog
import android.app.Dialog
import android.app.PendingIntent
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.digitalpersona.uareu.Reader
import com.digitalpersona.uareu.ReaderCollection
import com.digitalpersona.uareu.UareUException
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbException
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbHost
import com.karl.fingerprintmodule.Globals
import com.karl.fingerprintmodule.R
import com.karl.fingerprintmodule.Static
import com.karl.fingerprintmodule.ViewModels.MainActivityViewModel

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
        initViews()
        setListeners()
        setButtonsEnabled(false)

        getReaders()
    }

    override fun onResume() {
        super.onResume()

//        if (m_deviceName.isEmpty()) {
//            getReaders()
//        } else {
//            getUsbDevice()
//        }
    }

    private lateinit var viewModel: MainActivityViewModel;

    private fun init() {

        applContext = applicationContext

        viewModel = ViewModelProviders.of(this)[MainActivityViewModel::class.java]
        ACTION_USB_PERMISSION =
            Static.ACTION_USB_PERMISSION
    }

    private lateinit var m_selectedDevice: TextView
    private lateinit var m_getReader: Button
    private lateinit var m_register: CardView
    private lateinit var login: CardView

    private fun initViews() {

        m_selectedDevice = findViewById(R.id.selected_device)
        m_getReader = findViewById(R.id.get_reader)
        m_register = findViewById(R.id.register)
        login = findViewById(R.id.login)
    }

    private var m_deviceName = ""

    private fun setListeners() {
        m_getReader.setOnClickListener {

            val i = Intent(
                this
                , GetReaderActivity::class.java
            )
            i.putExtra("device_name", m_deviceName)
            startActivityForResult(i, 1)
        }

        m_register.setOnClickListener { registerActivity(); }

        login.setOnClickListener {
            val i = Intent(
                this,
                TimeInActivity::class.java
            )
            i.putExtra("device_name", m_deviceName)
            startActivityForResult(i, 1)
        }
    }

    protected fun setButtonsEnabled(enabled: Boolean) {

        m_register.setEnabled(enabled)
        login.setEnabled(enabled)
    }

    private val GENERAL_ACTIVITY_RESULT = 1
    private var m_reader: Reader? = null
    private var ACTION_USB_PERMISSION: String? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (data == null) {


//            if (m_deviceName.isEmpty()) {
//                getReaders()
//            } else {
//                getUsbDevice()
//            }

            //IF FROM GETREADERACTIVITY
        } else {

            Globals.ClearLastBitmap()

            m_deviceName = data.extras.getString("device_name")

            if (requestCode == GENERAL_ACTIVITY_RESULT) {

                if (m_deviceName.isNotEmpty()) {
                    m_selectedDevice.setText("Device: $m_deviceName")

                    try {
                        val applContext = applicationContext
                        m_reader = Globals.getInstance()
                            .getReader(m_deviceName, applContext)


                        val mPermissionIntent: PendingIntent
                        mPermissionIntent = PendingIntent.getBroadcast(
                            applContext,
                            0,
                            Intent(ACTION_USB_PERMISSION),
                            0
                        )
                        val filter = IntentFilter(ACTION_USB_PERMISSION)
                        applContext.registerReceiver(mUsbReceiver, filter)

                        if (DPFPDDUsbHost.DPFPDDUsbCheckAndRequestPermissions(
                                applContext,
                                mPermissionIntent,
                                m_deviceName
                            )
                        ) {
                            CheckDevice()
                        }

                    } catch (e1: UareUException) {
                        displayReaderNotFound("UareUException", e1.toString())
                    } catch (e: DPFPDDUsbException) {
                        displayReaderNotFound("DPFPDDUsbException", e.toString())
                    }
                } else {
                    displayReaderNotFound("Error!", "No Device found.")
                }
            }
        }
    }

    private lateinit var alertDialog: Dialog

    private val mUsbReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent!!.action
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized(this)
                {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            //call method to set up device communication
                            CheckDevice()
                        }
                    } else {
                        setButtonsEnabled(false)
                    }
                }
            }
        }
    }

    private fun getReaders() {

//        Toast.makeText(
//            this,
//            "Dumaan.",
//            Toast.LENGTH_LONG
//        ).show()


        val rc = viewModel.readers.value

        if (rc == null) {

            viewModel.getReader()

            viewModel.readers.observe(this, Observer<ReaderCollection> { readers ->
                if (readers == null) {
                    displayReaderNotFound("Empty Readers", "No Reader found. Try Again?")
                } else {
                    getFirstReader(readers)
                }
            })
        } else {
            getFirstReader(rc)
        }
//        val rc = Globals.getInstance().getReaders(applicationContext)
//
//        if (rc == null) {
//            displayReaderNotFound("Empty Readers", "No Reader found. Try Again?")
//        } else {
//            getFirstReader(rc)
//        }
    }

    private fun getFirstReader(readers: ReaderCollection) {

//        Toast.makeText(
//            this,
//            "Dumaan2.",
//            Toast.LENGTH_LONG
//        ).show()

        if (readers.isEmpty()) {
            displayReaderNotFound(
                "Device Disconnected",
                "Please make sure the device is connected properly"
            )
        } else {

            m_deviceName = readers.get(0).GetDescription().name

            if (readers.size > 1) {
                Toast.makeText(
                    this,
                    "More than 1 reader.\nFirst Reader chosen.",
                    Toast.LENGTH_LONG
                ).show(); }

            getUsbDevice()
        }
    }

    private lateinit var applContext: Context;

    private fun getUsbDevice() {

//        Toast.makeText(
//            this,
//            "Dumaan3.",
//            Toast.LENGTH_LONG
//        ).show()

        try {
            m_reader = Globals.getInstance().getReader(m_deviceName, applContext)

            val mPermissionIntent: PendingIntent = PendingIntent.getBroadcast(
                applContext,
                0,
                Intent(ACTION_USB_PERMISSION),
                0
            )
            val filter = IntentFilter(ACTION_USB_PERMISSION)
            applContext.registerReceiver(mUsbReceiver, filter)

            if (DPFPDDUsbHost.DPFPDDUsbCheckAndRequestPermissions(
                    applContext,
                    mPermissionIntent,
                    m_deviceName
                )
            ) {
                CheckDevice()
            }
        } catch (e1: UareUException) {

            //Triggered when onResume exit app but dont close
            displayReaderNotFound("UareUException", e1.toString())
        } catch (e: DPFPDDUsbException) {
            displayReaderNotFound("DPFPDDUsbException", e.toString())
        }
    }

    protected fun CheckDevice() {

        try {
            m_reader!!.Open(Reader.Priority.EXCLUSIVE)
            val cap = m_reader!!.GetCapabilities()

            setButtonsEnabled(true)
            //registerActivity()

            m_reader!!.Close()
        } catch (e1: UareUException) {
            displayReaderNotFound("UareUException", e1.toString())
        }
    }

    protected fun registerActivity() {

//        val i = Intent(this, RegisterActivity::class.java)

        val i = Intent(this, RegisterFromListActivity::class.java)
        i.putExtra("device_name", m_deviceName)
        startActivityForResult(i, 1)
    }

    private fun displayReaderNotFound(title: String, body: String) {
        //m_selectedDevice.setText("Device: (No Reader Selected)")
        m_selectedDevice.setText("Device: $m_deviceName")
        setButtonsEnabled(false)

        val alertDialogBuilder = AlertDialog.Builder(this)

        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(body)
            .setCancelable(false)
            .setPositiveButton("Retry") { dialog, which ->
                //setButtonsEnabled(true)
                getReaders()
            }
            .setNegativeButton("Close") { dialog, which ->

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    finishAffinity()
                } else {
                    finish()
                }
            }

        alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


}

