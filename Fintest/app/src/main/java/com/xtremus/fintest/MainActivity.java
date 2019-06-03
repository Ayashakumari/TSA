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


    @Override
    public void OnDeviceAttached(int i, int i1, boolean b) {

    }

    @Override
    public void OnDeviceDetached() {

    }

    @Override
    public void OnHostCheckFailed(String s) {

    }
}
