package com.xtremus.fintest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mantra.mfs100.DeviceInfo;
import com.mantra.mfs100.FingerData;
import com.mantra.mfs100.MFS100;
import com.mantra.mfs100.MFS100Event;

public class MainActivity extends AppCompatActivity implements MFS100Event {

    TextView aView2;
    private enum ScannerAction {
        Capture, Verify
    }
    private FingerData lastCapFingerData = null;
    ScannerAction scannerAction = ScannerAction.Capture;


    int timeout = 10000;
    MFS100 mfs100 = null;
    private boolean isCaptureRunning = false;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    protected void onStart() {
        Toast.makeText(this, "Init Process started", Toast.LENGTH_LONG).show();
        if (mfs100 == null) {
            mfs100 = new MFS100(this);
            mfs100.SetApplicationContext(MainActivity.this);
            Toast.makeText(this, "Init process successful", Toast.LENGTH_SHORT).show();
        } else {
            InitScanner();
            Toast.makeText(this, "Init process successful", Toast.LENGTH_SHORT).show();
        }
        super.onStart();
    }


    private void StartSyncCapture() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                //SetTextOnUIThread("");
                isCaptureRunning = true;
                try {
                    FingerData fingerData = new FingerData();
                    int ret = mfs100.AutoCapture(fingerData, timeout, false);
                    Log.e("StartSyncCapture.RET", "" + ret);
                    if (ret != 0) {
                        //SetTextOnUIThread(mfs100.GetErrorMsg(ret));
                    } else {
                        lastCapFingerData = fingerData;
                        final Bitmap bitmap = BitmapFactory.decodeByteArray(fingerData.FingerImage(), 0,
                                fingerData.FingerImage().length);
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //SetTextOnUIThread("good");
                            }
                        });

                        //SetTextOnUIThread("Capture Success");
                        String log = "\nQuality: " + fingerData.Quality()
                                + "\nNFIQ: " + fingerData.Nfiq()
                                + "\nWSQ Compress Ratio: "
                                + fingerData.WSQCompressRatio()
                                + "\nImage Dimensions (inch): "
                                + fingerData.InWidth() + "\" X "
                                + fingerData.InHeight() + "\""
                                + "\nImage Area (inch): " + fingerData.InArea()
                                + "\"" + "\nResolution (dpi/ppi): "
                                + fingerData.Resolution() + "\nGray Scale: "
                                + fingerData.GrayScale() + "\nBits Per Pixal: "
                                + fingerData.Bpp() + "\nWSQ Info: "
                                + fingerData.WSQInfo();
                       // SetTextOnUIThread(log);
                       // SetData2(fingerData);
                    }
                } catch (Exception ex) {
                    //SetTextOnUIThread("Error");
                } finally {
                    isCaptureRunning = false;
                }
            }
        }).start();
    }



    private void SetTextOnUIThread(final String str) {

        aView2.post(new Runnable() {
            public void run() {
                aView2.setText(str);
            }
        });
    }

    private void InitScanner() {
        try {
            int ret = mfs100.Init();
            if (ret != 0) {
                SetTextOnUIThread(mfs100.GetErrorMsg(ret));
            } else {
                SetTextOnUIThread("Init success");
                String info = "Serial: " + mfs100.GetDeviceInfo().SerialNo()
                        + " Make: " + mfs100.GetDeviceInfo().Make()
                        + " Model: " + mfs100.GetDeviceInfo().Model()
                        + "\nCertificate: " + mfs100.GetCertification();
                SetTextOnUIThread(info);
            }
        } catch (Exception ex) {
            Toast.makeText(this, "Init failed, unhandled exception",
                    Toast.LENGTH_LONG).show();
            SetTextOnUIThread("Init failed, unhandled exception");
        }
    }

    public void onLoginClick(View v) {
        int id = v.getId();
        if (id == R.id.btnLog) {
            Toast.makeText(this, "Init Process started in login", Toast.LENGTH_LONG).show();
            InitScanner();

            scannerAction = ScannerAction.Capture;
            if (!isCaptureRunning) {
                StartSyncCapture();
            }
        }
    }
    private void showSuccessLog(String key) {
        SetTextOnUIThread("Init success");
        String info = "\nKey: " + key + "\nSerial: "
                + mfs100.GetDeviceInfo().SerialNo() + " Make: "
                + mfs100.GetDeviceInfo().Make() + " Model: "
                + mfs100.GetDeviceInfo().Model()
                + "\nCertificate: " + mfs100.GetCertification();
        aView2.setText(info);
    }

    @Override
    public void OnDeviceAttached(int vid, int pid, boolean hasPermission) {
        int ret = mfs100.Init();
        String key = "Without Key";
        if(ret==0)
        {
            DeviceInfo deviceInfo = null;
            deviceInfo = mfs100.GetDeviceInfo();
            if (deviceInfo != null)
            {
                String msg = mfs100.GetDeviceInfo().SerialNo();
                //showSuccessLog(key);
                Toast.makeText(this, msg,
                        Toast.LENGTH_LONG).show();
                //showSuccessLog(key);
            }
        }
    }


    @Override
    public void OnDeviceDetached() {

    }

    @Override
    public void OnHostCheckFailed(String s) {

    }
}
