package com.karl.fingerprintmodule.Activities

import android.app.AlertDialog
import android.app.Dialog
import android.app.PendingIntent
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.*
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.digitalpersona.uareu.Reader
import com.digitalpersona.uareu.ReaderCollection
import com.digitalpersona.uareu.UareUException
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbException
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbHost
import com.karl.fingerprintmodule.*
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

        if (m_deviceName.isEmpty()) {
            getReaders()
        } else {
            m_selectedDevice.setText("Device: " + m_deviceName)
        }
    }

    private lateinit var viewModel: MainActivityViewModel;

    private fun init() {

        viewModel = ViewModelProviders.of(this)[MainActivityViewModel::class.java]
        ACTION_USB_PERMISSION =
            Static.ACTION_USB_PERMISSION
    }

    private lateinit var m_selectedDevice: TextView
    private lateinit var m_getReader: Button
    //private lateinit var m_captureFingerprint: Button
    private lateinit var m_register: Button
    private lateinit var login: Button

    private fun initViews() {

        m_selectedDevice = findViewById(R.id.selected_device)
        m_getReader = findViewById(R.id.get_reader)
        //m_captureFingerprint = findViewById(R.id.capture_fingerprint)
        m_register = findViewById(R.id.register)
        login = findViewById(R.id.login)
    }

    private var m_deviceName = ""

    private fun setListeners() {
        m_getReader.setOnClickListener {

            val i = Intent(
                //this@UareUSampleJava
                this
                , GetReaderActivity::class.java
            )
            i.putExtra("device_name", m_deviceName)
            startActivityForResult(i, 1)
        }

//        m_captureFingerprint.setOnClickListener {
//
//            getReaders()
//        }

        m_register.setOnClickListener {
            registerActivity();
        }

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
            displayReaderNotFound()
            return
        }

        Globals.ClearLastBitmap()

        m_deviceName = data.extras.getString("device_name")

        if (requestCode == GENERAL_ACTIVITY_RESULT) {

            if ((m_deviceName != null) && !m_deviceName.isEmpty()) {
                m_selectedDevice.setText("Device: " + m_deviceName)

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
                    displayReaderNotFound()
                } catch (e: DPFPDDUsbException) {
                    displayReaderNotFound()
                }

            } else {
                displayReaderNotFound()
            }
        }
    }

    private lateinit var alertDialog: Dialog

    private fun displayReaderNotFound() {
        m_selectedDevice.setText("Device: (No Reader Selected)")
        setButtonsEnabled(false)
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Reader Not Found")

        alertDialogBuilder.setMessage("Plug in a reader and try again.")
            .setCancelable(false)
            .setPositiveButton("Ok") { dialog, which ->
                setButtonsEnabled(true)
//                alertDialog.dismiss()
            }
            .setNegativeButton("Close") { dialog, which -> finish() }

        alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

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

        viewModel.getReader()
        viewModel.readers.observe(this, Observer<ReaderCollection> { readers ->
            if (readers == null) {
                displayReaderNotFound()
            } else {
                if (readers.isEmpty()) {
                    displayReaderNotFound()
                } else {
                    if (readers.size == 1) {

                        m_deviceName = readers.get(0).GetDescription().name

                        getUsbDevice()
                    } else {
                        Toast.makeText(this, "More than 1 reader", Toast.LENGTH_LONG).show();
                    }
                }
            }
        })
    }

    private fun getUsbDevice() {
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
            displayReaderNotFound()
        } catch (e: DPFPDDUsbException) {
            displayReaderNotFound()
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
            displayReaderNotFound()
        }
    }

    protected fun registerActivity() {

//        val i = Intent(this, RegisterActivity::class.java)
        val i = Intent(this, RegisterFromListActivity::class.java)
        i.putExtra("device_name", m_deviceName)
        startActivityForResult(i, 1)
    }


}

