package com.karl.fingerprintmodule

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.*
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import com.digitalpersona.uareu.Reader
import com.digitalpersona.uareu.UareUException
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbException
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbHost

class MainActivity : AppCompatActivity() {

    private val GENERAL_ACTIVITY_RESULT = 1

    private var m_deviceName = ""
    private lateinit var m_selectedDevice:TextView

    private lateinit var m_getReader: Button
    private lateinit var m_captureFingerprint: Button
    private lateinit var m_identification: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        m_selectedDevice = findViewById(R.id.selected_device);
        m_getReader = findViewById(R.id.get_reader);
        m_captureFingerprint = findViewById(R.id.capture_fingerprint);
        m_identification = findViewById(R.id.identification);


        setButtonsEnabled(false)

        m_getReader.setOnClickListener {

            val i = Intent(
            //this@UareUSampleJava
            this
            , GetReaderActivity::class.java)
            i.putExtra("device_name", m_deviceName)
            startActivityForResult(i, 1)
        }

        m_captureFingerprint.setOnClickListener {
            val i = Intent(
                this,
                CaptureFingerprintActivity::class.java)
            i.putExtra("device_name", m_deviceName);
            startActivityForResult(i, 1);
        }

        m_identification.setOnClickListener {
            val i = Intent(
                this,
                IdentificationActivity::class.java)
            i.putExtra("device_name", m_deviceName);
            startActivityForResult(i, 1);
        }
    }

    private var m_reader: Reader? = null;
    private val ACTION_USB_PERMISSION = "com.digitalpersona.uareu.dpfpddusbhost.USB_PERMISSION"

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (data == null)
        {
            displayReaderNotFound()
            return;
        }

        Globals.ClearLastBitmap()

        m_deviceName = data.extras.getString("device_name")

        if (requestCode == GENERAL_ACTIVITY_RESULT)
        {

            if((m_deviceName != null) && !m_deviceName.isEmpty())
            {
                m_selectedDevice.setText("Device: " + m_deviceName);

                try {
                    val applContext = getApplicationContext();
                    m_reader = Globals.getInstance().getReader(m_deviceName, applContext);


                    val mPermissionIntent: PendingIntent
                        mPermissionIntent = PendingIntent.getBroadcast(applContext, 0,  Intent(ACTION_USB_PERMISSION), 0);
                        val filter = IntentFilter(ACTION_USB_PERMISSION);
                        applContext.registerReceiver(mUsbReceiver, filter);

                        if(DPFPDDUsbHost.DPFPDDUsbCheckAndRequestPermissions(applContext, mPermissionIntent, m_deviceName))
                        {
                            CheckDevice();
                        }

                } catch (e1 : UareUException)
                {
                    displayReaderNotFound();
                }
                catch (e : DPFPDDUsbException)
                {
                    displayReaderNotFound();
                }

            } else
            {
                displayReaderNotFound();
            }
        }
    }

    private fun displayReaderNotFound()
    {
        m_selectedDevice.setText("Device: (No Reader Selected)");
        setButtonsEnabled(false);
        val alertDialogBuilder = AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Reader Not Found");



        alertDialogBuilder.setMessage("Plug in a reader and try again.").setCancelable(false).setPositiveButton("Ok",
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

                }

            })

        val alertDialog = alertDialogBuilder.create();
        alertDialog.show()
    }

    protected fun setButtonsEnabled(enabled: Boolean)
    {
//        m_getCapabilities.setEnabled(enabled);
//        m_streamImage.setEnabled(enabled);
//        m_captureFingerprint.setEnabled(enabled);
//        m_enrollment.setEnabled(enabled);
//        m_verification.setEnabled(enabled);
//        m_identification.setEnabled(enabled);
    }

    protected fun CheckDevice()
    {
        try
        {
            m_reader!!.Open(Reader.Priority.EXCLUSIVE);
            val cap = m_reader!!.GetCapabilities();
            setButtonsEnabled(true);
//            setButtonsEnabled_Capture(cap.can_capture);
//            setButtonsEnabled_Stream(cap.can_stream);
            m_reader!!.Close();
        }
        catch ( e1 : UareUException)
        {
            displayReaderNotFound();
        }
    }

    private val  mUsbReceiver = object: BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent!!.getAction();
            if (ACTION_USB_PERMISSION.equals(action))
            {
                synchronized (this)
                {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
                    {
                        if(device != null)
                        {
                            //call method to set up device communication
                            CheckDevice();
                        }
                    }
                    else
                    {
                        setButtonsEnabled(false);
                    }
                }
            }
        }
    }

}
