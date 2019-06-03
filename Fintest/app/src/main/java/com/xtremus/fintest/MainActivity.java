package com.xtremus.fintest;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.mantra.mfs100.MFS100Event;


public class MainActivity extends AppCompatActivity implements MFS100Event {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
