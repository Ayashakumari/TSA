package com.xtremus.fintest;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.mantra.mfs100.MFS100;
import com.mantra.mfs100.MFS100Event;

public class MainActivity extends AppCompatActivity implements MFS100Event {


    EditText lblMessage;


    int timeout = 10000;
    MFS100 mfs100 = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (mfs100 == null) {
            mfs100 = new MFS100(this);
            mfs100.SetApplicationContext(MainActivity.this);
        } else {
            InitScanner();
        }
    }

    private void SetTextOnUIThread(final String str) {

        lblMessage.post(new Runnable() {
            public void run() {
                lblMessage.setText(str);
            }
        });
    }

    private void showSuccessLog(String key) {
        SetTextOnUIThread("Init success");
        /*String info = "\nKey: " + key + "\nSerial: "
                + mfs100.GetDeviceInfo().SerialNo() + " Make: "
                + mfs100.GetDeviceInfo().Make() + " Model: "
                + mfs100.GetDeviceInfo().Model()
                + "\nCertificate: " + mfs100.GetCertification();
        SetLogOnUIThread(info);*/
    }


    @Override
    public void OnDeviceAttached(int vid, int pid, boolean hasPermission) {
        int ret;
        if (!hasPermission) {
            SetTextOnUIThread("Permission denied");
            return;
        }
        if (vid == 1204 || vid == 11279) {
            if (pid == 34323) {
                ret = mfs100.LoadFirmware();
                if (ret != 0) {
                    SetTextOnUIThread(mfs100.GetErrorMsg(ret));
                } else {
                    SetTextOnUIThread("Load firmware success");
                }
            } else if (pid == 4101) {
                String key = "Without Key";
                ret = mfs100.Init();
                if (ret == 0) {
                    showSuccessLog(key);
                } else {
                    SetTextOnUIThread(mfs100.GetErrorMsg(ret));
                }

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
