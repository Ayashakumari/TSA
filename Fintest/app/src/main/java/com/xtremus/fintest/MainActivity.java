package com.xtremus.fintest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mantra.mfs100.FingerData;
import com.mantra.mfs100.MFS100;
import com.mantra.mfs100.MFS100Event;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;



public class MainActivity extends AppCompatActivity implements MFS100Event {


    Button btnSyncCapture;

    Button btnMatchISOTemplate;
    byte[] Enroll_Template;
    byte[] Verify_Template;
    ScannerAction scannerAction = ScannerAction.Capture;
    int timeout = 10000;
    MFS100 mfs100 = null;
    private FingerData lastCapFingerData = null;
    private boolean isCaptureRunning = false;
    private int id = 15000;
    AlertDialog.Builder builder;


    TextView lblMessage;
    EditText txtEventLog;
    private View v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        id=0;
        //controls
        FindFormControls();
        try {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        } catch (Exception e) {
            Log.e("Error", e.toString());
        }
    }


    public void FindFormControls() {

        btnMatchISOTemplate = (Button) findViewById(R.id.match);

        lblMessage = (TextView) findViewById(R.id.lblMessage);
        txtEventLog = (EditText) findViewById(R.id.txtEventLog);
        btnSyncCapture = (Button) findViewById(R.id.enroll);

        if (mfs100 == null) {
            mfs100 = new MFS100(this);
            mfs100.SetApplicationContext(MainActivity.this);
            Toast.makeText(getApplicationContext(), "Init Success",
                    Toast.LENGTH_LONG).show();
            SetTextOnUIThread("Init Successful.");
        } else {
            InitScanner();
        }

        btnSyncCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InitScanner();

                scannerAction = ScannerAction.Capture;
                if (!isCaptureRunning) {
                    Toast.makeText(getApplicationContext(), "Capture will start", Toast.LENGTH_SHORT).show();
                    txtEventLog.post(new Runnable() {
                        public void run() {
                            txtEventLog.setText("", TextView.BufferType.EDITABLE);
                        }
                    });
                    Capture();
                }
            }
        });

        btnMatchISOTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scannerAction = ScannerAction.Verify;
                if (!isCaptureRunning) {
                    SetTextOnUIThread("Match will start.");
                    Capture();

                }
            }
        });


    }

    private void Capture() {
        new Thread(new Runnable() {

            @Override
            public void run() {

                SetTextOnUIThread("Capture Started");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    SetTextOnUIThread("Error in sleep");
                }
                FingerData fingerData = new FingerData();

                isCaptureRunning = true;
                try {



                    int ret = mfs100.AutoCapture(fingerData, timeout, false);
                    Log.e("StartSyncCapture.RET", "" + ret);
                    if (ret != 0) {
                        SetTextOnUIThread(mfs100.GetErrorMsg(ret));
                    } else {
                        lastCapFingerData = fingerData;
                        final Bitmap bitmap = BitmapFactory.decodeByteArray(fingerData.FingerImage(), 0,
                                fingerData.FingerImage().length);


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
                    SetTextOnUIThread("Exception in cap thread");
                } finally {

                    isCaptureRunning = false;
                }
            }
        }).start();

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
                //SetLogOnUIThread(info);
            }
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Init failed, unhandled exception",
                    Toast.LENGTH_LONG).show();
            SetTextOnUIThread("Init failed, unhandled exception");
        }
    }

    private void SetLogOnUIThread(final String str) {

        txtEventLog.post(new Runnable() {
            public void run() {
                txtEventLog.append("\n" + str);
            }
        });
    }


    @Override
    public void OnDeviceAttached(int i, int i1, boolean b) {

    }


    private void SetTextOnUIThread(final String str) {

        lblMessage.post(new Runnable() {
            public void run() {
                lblMessage.setText(str);
            }
        });
    }

    @Override
    public void OnDeviceDetached() {

    }

    @Override
    public void OnHostCheckFailed(String s) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private enum ScannerAction {
        Capture, Verify
    }

    private void WriteFile(String filename, byte[] bytes) {
        try {

            String path = Environment.getExternalStorageDirectory()
                    + "//FingerData";

            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            path = path + "//" + filename;
            file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream stream = new FileOutputStream(path);
            stream.write(bytes);
            stream.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public void SetData2(FingerData fingerData) {
        String path1;
        File file =null;
        String ISO;
        byte[] Enroll_Templat=null;
        int ret=0;
        if (scannerAction.equals(ScannerAction.Capture)) {
            id += 1;
            Enroll_Template = new byte[fingerData.ISOTemplate().length];
        System.arraycopy(fingerData.ISOTemplate(), 0, Enroll_Template, 0,
                    fingerData.ISOTemplate().length);
        } else if (scannerAction.equals(ScannerAction.Verify)) {

                Verify_Template = new byte[fingerData.ISOTemplate().length];
                System.arraycopy(fingerData.ISOTemplate(), 0, Verify_Template, 0,
                        fingerData.ISOTemplate().length);
            try {
                path1 = Environment.getExternalStorageDirectory()
                        + "//FingerData";
                SetTextOnUIThread("Path is set");

            for (int i = 1; i <= id; i++) {
                SetTextOnUIThread("For loop" + i);
                InputStream is = null;
                ISO = "ISOTemplate" + i;
                path1 += ISO;
                try{
                    file = new File(path1);
                }catch (Exception e){
                    SetLogOnUIThread("Exception in file path"+ e);
                }

                    is = new FileInputStream(file);
                    Enroll_Templat = new byte[(int)file.length()];
                try {
                    is.read(Enroll_Templat);
                }catch(Exception e)
                {
                    SetTextOnUIThread("Error in input stream");
                }

                /*System.arraycopy(path1, 0, Enroll_Template, 0,
                        fingerData.ISOTemplate().length);*/

               ret = mfs100.MatchISO(Enroll_Templat, Verify_Template);
                if (ret >= 1400)
                {
                 SetTextOnUIThread("ret if block");
                 break;
                }

           }
            if (ret < 0) {
                SetTextOnUIThread("Error: " + ret + "(" + mfs100.GetErrorMsg(ret) + ")");
            } else {
                if (ret >= 1400) {
                    SetTextOnUIThread("Finger matched with score: " + ret);
                } else {
                    SetTextOnUIThread("Finger not matched, score: " + ret);
                }
            }
            }catch(Exception e) {
                SetTextOnUIThread("Exception in path");
            }
        }
        String bmpstr = "Bitmap" + id + ".bmp";
        String isostr = "ISOTemplate" + id + ".iso";

        WriteFile(bmpstr, fingerData.FingerImage());
        WriteFile(isostr, fingerData.ISOTemplate());
    }

  //  @Override
   /* public void OnDeviceAttached(int vid, int pid, boolean hasPermission) {
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

   /* @Override
    public void OnDeviceDetached() {
        UnInitScanner();
        SetTextOnUIThread("Device removed");
    }

    @Override
    public void OnHostCheckFailed(String err) {
        try {
            SetLogOnUIThread(err);
            Toast.makeText(getApplicationContext(), err, Toast.LENGTH_LONG).show();
        } catch (Exception ignored) {
        }
    } */
}
