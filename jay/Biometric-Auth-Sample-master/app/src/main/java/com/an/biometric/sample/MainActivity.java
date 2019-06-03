package com.an.biometric.sample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;
import com.mantra.*;
import com.mantra.mfs100.FingerData;
import com.mantra.mfs100.MFS100;
import com.mantra.mfs100.MFS100Event;

import java.io.File;
import java.io.FileOutputStream;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.an.biometric.BiometricCallback;
import com.an.biometric.BiometricManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements BiometricCallback {

    private Button button;
    BiometricManager mBiometricManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.btn_authenticate);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*

                mBiometricManager = new BiometricManager.BiometricBuilder(MainActivity.this)
                        .setTitle(getString(R.string.biometric_title))
                        .setSubtitle(getString(R.string.biometric_subtitle))
                        .setDescription(getString(R.string.biometric_description))
                        .setNegativeButtonText(getString(R.string.biometric_negative_button_text))
                        .build();

                *
                 * */
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        isCaptureRunning = true;
                        try {
                            FingerData fingerData = new FingerData();
                            int ret = mfs100.AutoCapture(fingerData, timeout, cbFastDetection.isChecked());
                            Log.e("StartSyncCapture.RET", ""+ret);
                            if (ret != 0) {
                                SetTextOnUIThread(mfs100.GetErrorMsg(ret));
                            } else {
                                lastCapFingerData = fingerData;
                                final Bitmap bitmap = BitmapFactory.decodeByteArray(fingerData.FingerImage(), 0,
                                        fingerData.FingerImage().length);
                                com.mantra.MFS100Test.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        imgFinger.setImageBitmap(bitmap);
                                    }
                                });

                                SetTextOnUIThread("Capture Success");
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
                                SetLogOnUIThread(log);
                                SetData2(fingerData);
                            }
                        } catch (Exception ex) {
                            SetTextOnUIThread("Error");
                        } finally {
                            isCaptureRunning = false;
                        }
                    }
                }).start();
                    }
                //start authentication
                mBiometricManager.authenticate(MainActivity.this);
            }
        });
    }


    @Override
    public void onSdkVersionNotSupported() {
        Toast.makeText(getApplicationContext(), getString(R.string.biometric_error_sdk_not_supported), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBiometricAuthenticationNotSupported() {
        Toast.makeText(getApplicationContext(), getString(R.string.biometric_error_hardware_not_supported), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBiometricAuthenticationNotAvailable() {
        Toast.makeText(getApplicationContext(), getString(R.string.biometric_error_fingerprint_not_available), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBiometricAuthenticationPermissionNotGranted() {
        Toast.makeText(getApplicationContext(), getString(R.string.biometric_error_permission_not_granted), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBiometricAuthenticationInternalError(String error) {
        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationFailed() {
//        Toast.makeText(getApplicationContext(), getString(R.string.biometric_failure), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationCancelled() {
        Toast.makeText(getApplicationContext(), getString(R.string.biometric_cancelled), Toast.LENGTH_LONG).show();
        mBiometricManager.cancelAuthentication();
    }

    @Override
    public void onAuthenticationSuccessful() {
        Toast.makeText(getApplicationContext(), getString(R.string.biometric_success), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
//        Toast.makeText(getApplicationContext(), helpString, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
//        Toast.makeText(getApplicationContext(), errString, Toast.LENGTH_LONG).show();
    }
}
