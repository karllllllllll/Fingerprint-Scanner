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
import android.graphics.Color
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.PopupMenu
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.digitalpersona.uareu.*
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbException
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbHost
import com.karl.fingerprintmodule.*
import com.karl.fingerprintmodule.Fragments.*
import com.karl.fingerprintmodule.Models.User
import com.karl.fingerprintmodule.ViewModels.FmdViewModel
import com.karl.fingerprintmodule.ViewModels.MainActivityViewModel
import com.karl.fingerprintmodule.ViewModels.TimekeepingViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var ownerDialog: Dialog;
    private lateinit var ownerNotFoundDialog: Dialog;

    private fun dismissDialogs() {
        if (ownerDialog.isShowing()) {
            ownerDialog.dismiss()
        }
        if (ownerNotFoundDialog.isShowing()) {
            ownerNotFoundDialog.dismiss()
        }
    }

    private fun showOwnerNotFound() {

        dismissDialogs();

        val cv_use_pin: CardView = ownerNotFoundDialog.findViewById(R.id.cv_use_pin)
        val cv_register: CardView = ownerNotFoundDialog.findViewById(R.id.cv_register)


        val bundle = Bundle()

        cv_use_pin.setOnClickListener {

            dismissDialogs();
            bundle.putInt("flag", Flags.USE_PIN)

            val rflf = RegisterFromListFragment()
            rflf.arguments = bundle


            Globals.register = true
            changeFragment("RegisterFromListFragment", rflf);
        }

        cv_register.setOnClickListener {
            dismissDialogs();

            bundle.putInt("flag", Flags.REGISTER)

            val rflf = RegisterFromListFragment()
            rflf.arguments = bundle


            Globals.register = true
            changeFragment("RegisterFromListFragment", rflf);
        }

        ownerNotFoundDialog.show()
    }

    private fun showOwner() {
        dismissDialogs();

        val iv_avatar: ImageView =
            ownerDialog.findViewById(R.id.iv_avatar)
        val tv_fullname: TextView = ownerDialog.findViewById(R.id.tv_fullname)
        val tv_current_time: TextView = ownerDialog.findViewById(R.id.tv_current_time)
        val tv_time_in: TextView = ownerDialog.findViewById(R.id.tv_time_in)
        val tv_time_out: TextView = ownerDialog.findViewById(R.id.tv_time_out)
        val btn_clock_in_out: Button =
            ownerDialog.findViewById(R.id.btn_clock_in_out)
        val time: String = helper.now()

        try {
            val fullName =
                fingerPrintOwner!!.f_name + " " + fingerPrintOwner!!.l_name
            tv_time_in.setText(helper.convertToReadableTime(fingerPrintOwner!!.time_in))
            tv_time_out.setText(helper.convertToReadableTime(fingerPrintOwner!!.time_out))
            tv_current_time.setText(helper.convertToReadableTime(time))
            tv_fullname.text = fullName
            Glide.with(this)
                .load(fingerPrintOwner!!.image_path)
                .apply(RequestOptions.circleCropTransform())
                .into(iv_avatar)
            val blankTime = "null"
            if (fingerPrintOwner!!.time_in != blankTime && fingerPrintOwner!!.time_out != blankTime) {
                btn_clock_in_out.isEnabled = false
                btn_clock_in_out.text = "You have already clocked out for the day!"

                //val color = ContextCompat.getColor(this, R.color.gry500);
                val color = getResources().getColor(R.color.gry500);
                btn_clock_in_out.setBackgroundColor(Color.GRAY)
            } else {
                btn_clock_in_out.isEnabled = true
                if (fingerPrintOwner!!.time_in == blankTime) {
                    btn_clock_in_out.text = "Clock in"

                    //val color = ContextCompat.getColor(this, R.color.success);
                    val color = getResources().getColor(R.color.success);
                    btn_clock_in_out.setBackgroundColor(Color.GREEN)
                } else {
                    btn_clock_in_out.text = "Clock out"
                    //val color = ContextCompat.getColor(this, R.color.pending);
                    val color = getResources().getColor(R.color.pending);
                    btn_clock_in_out.setBackgroundColor(Color.MAGENTA)
                }
                btn_clock_in_out.setOnClickListener {
                    tkviewModel.sendClockInOut(fingerPrintOwner)
                }
            }

            btn_clock_in_out.invalidate()

        } catch (e: java.lang.Exception) {
            Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_LONG).show()
        }

        ownerDialog.show()
    }

    private var m_DPI = 0
    private var m_engine: Engine? = null

    private fun initReader() {
        try {
            m_reader!!.Open(Reader.Priority.EXCLUSIVE)
            m_DPI = Globals.GetFirstDPI(m_reader)
            m_engine = UareUGlobal.GetEngine()

            runThread()
            changeFragment("ScannerReadyFragment", ScannerReadyFragment())
        } catch (e: java.lang.Exception) {
            onBackPressed()
            return
        }
    }

    private var m_reset = false
    private var cap_result: Reader.CaptureResult? = null
    private var searchFMD: Fmd? = null

    private fun runThread() {

        Globals.captureResultObservable = CaptureResultObservable()

        Thread(Runnable {
            m_reset = false
            while (!m_reset) {
                try {
                    cap_result = m_reader!!.Capture(
                        Fid.Format.ANSI_381_2004,
                        Globals.DefaultImageProcessing,
                        m_DPI,
                        -1
                    )
                } catch (e: Exception) {
                    if (!m_reset) {
                        Log.w(
                            "UareUSampleJava",
                            "error during capture: $e"
                        )
                        m_deviceName = ""
                        //onBackPressed();
                    }
                }
                // an error occurred
                if (cap_result == null || cap_result!!.image == null) continue

                try {

                    if (Globals.register) {
                        //Globals.captureResult = cap_result
                        Globals.captureResultObservable.captureResult = cap_result;
                    }

                    searchFMD = m_engine!!.CreateFmd(cap_result!!.image, Fmd.Format.ANSI_378_2004)
                    val r = fmdViewModel.findUserFMD(searchFMD)
                    fingerPrintOwner = if (r!!.status == "success") {
                        tkviewModel.findUserByUserID(r.message)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    Log.w("UareUSampleJava", "Engine error: $e")
                }

                runOnUiThread { UpdateGUI() }
            }
        }).start()
    }

    private var fingerPrintOwner: User? = null;

    private fun UpdateGUI() {

        if (!Globals.register) {
            if (fingerPrintOwner == null)
                showOwnerNotFound()
            else
                showOwner()
        }
    }

    private fun onDeviceReady() {

        tkviewModel.getUsers()

        tkviewModel.userArrayList.observe(this, Observer {

            if (it != null && it.size > 0) {

                fmdViewModel.retrieveFingerPrints()
                fmdViewModel.getfmdListConversionResult().observe(this, Observer {
                    if (it != null) {

                        initReader()

//                        val bundle = Bundle()
//                        bundle.putInt("flag",Flags.USE_PIN)
//
//                        val rflf = RegisterFromListFragment()
//                        rflf.arguments = bundle
//
//
//                        Globals.register = true
//                        changeFragment("RegisterFromListFragment", rflf);
                    } else {
                        Toast.makeText(this, "No Reader", Toast.LENGTH_LONG).show()
                    }
                })
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
        initViews()
        changeFragment("LoadingScreenFragment", LoadingScreenFragment())
        setListeners()
        //setButtonsEnabled(false)

        //DEBUG
        getReaders()
        //onDeviceReady()
    }

    override fun onResume() {
        super.onResume()

        getPendingUpdates()
    }

    private fun getPendingUpdates() {

        val tv_updates: TextView = findViewById(R.id.tv_updates)

        val pu = tkviewModel.pendingUpdatesAsArray()

        var StringPU = "";
        for (pi in pu) {
            StringPU = StringPU.plus(pi.user_id + " " + pi.time).plus("\n")
        }

        tv_updates.setText(StringPU)
    }

    private lateinit var viewModel: MainActivityViewModel;
    private lateinit var tkviewModel: TimekeepingViewModel;
    private lateinit var fmdViewModel: FmdViewModel;
    private lateinit var helper: Helper;

    private fun init() {

        applContext = applicationContext

        viewModel = ViewModelProviders.of(this)[MainActivityViewModel::class.java]
        tkviewModel = ViewModelProviders.of(this)[TimekeepingViewModel::class.java]
        fmdViewModel = ViewModelProviders.of(this)[FmdViewModel::class.java]
        ACTION_USB_PERMISSION =
            Static.ACTION_USB_PERMISSION

        helper = Helper.getInstance(this)
    }

    private lateinit var m_selectedDevice: TextView
    private lateinit var m_getReader: Button
    private lateinit var iv_more: ImageView

    private fun initViews() {

        m_selectedDevice = findViewById(R.id.selected_device)
        m_getReader = findViewById(R.id.get_reader)

        ownerDialog = Dialog(this, R.style.AppTheme_SlideAnimation)
        ownerNotFoundDialog = Dialog(this, R.style.AppTheme_SlideAnimation)
        ownerDialog.setContentView(R.layout.dialog_fingerprint_owner_info)
        ownerNotFoundDialog.setContentView(R.layout.dialog_user_not_found)
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

        tkviewModel.fragment.observe(this, Observer {
            if (it != null) {

                changeFragment("Test", it)
            }
        })
    }

    private fun changeFragment(tag: String, f: Fragment) {

        val manager = supportFragmentManager;
        val fragment = supportFragmentManager.findFragmentByTag(tag)

        //If fragment exists in backstack
        if (fragment != null && fragment.isAdded) {

            manager.popBackStackImmediate(tag, 0)
        } else {
            addFragmentToContainer(tag, f)
        }
    }

    private fun addFragmentToContainer(
        tag: String,
        f: Fragment
    ) {


        //val bundle = Bundle();
        //bundle.putString("device_name", m_deviceName);   //parameters are (key, value).
        //val sf = SliderFragment();
        //sf.setArguments(bundle);
        //f.setArguments(bundle);

        val manager = supportFragmentManager;
        val transaction = manager.beginTransaction();
        //transaction.add(R.id.fragment_container, sf, tag);
        transaction.add(R.id.fragment_container, f, tag);
        transaction.addToBackStack(f.tag);
        transaction.commit();
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
                        displayReaderNotFound("No Device", "Device not Found!")
                    }
                }
            }
        }
    }

    private fun getReaders() {

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
    }

    private fun getFirstReader(readers: ReaderCollection) {

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

            onDeviceReady()

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


