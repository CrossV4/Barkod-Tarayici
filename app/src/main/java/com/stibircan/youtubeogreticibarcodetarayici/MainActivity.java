package com.stibircan.youtubeogreticibarcodetarayici;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
{
    private SurfaceView surfaceView;
    private Button scanNew;

    private CameraSource cameraSource;

    private static final int CAMERA_PERMISSION = 1524;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //kodları ayıralım dağınık sevmiyorum
        layoutInflateIndex();
        variableIndex();
        codeBinary();
    }

    private void codeBinary()
    {
        // lambda şart değil !
        scanNew.setOnClickListener(v ->
        {
            startActivity(new Intent(MainActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        });
    }

    private void variableIndex()
    {
        // iç ayarlamaları yapalım
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE | Barcode.CODABAR).build(); // Barkod tiplerinizi burada yazıyorsunuz. Ben QR kod ve barkod u alacağım

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(800, 600) // Çeşit kamera açısı 4:3, 16:9 kafanıza göre
                .setAutoFocusEnabled(true)
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback()
        {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder)
            {
                // kamera izinlerimizi alalım
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    // izin yok izini alalım
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
                    return; // return yazalım ki izin yoksa kamera açmaya çalışmasın.
                }
                try
                {
                    cameraSource.start(holder);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height)
            {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder)
            {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>()
        {
            @Override
            public void release()
            {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections)
            {
                // taranan kodlarlar sparse array biçiminde gelir.
                SparseArray<Barcode> scannedArray = detections.getDetectedItems();
                if (scannedArray.size() > 0)
                {
                    // kod varsa
                    // thread içerisinde runonui yapmayı sakın unutmayın yoksa taranan kodu alaazsınız.
                    Thread thread = new Thread()
                    {
                        @Override
                        public void run()
                        {
                            // sizde lambda olmayabilir bu şekilde yapmanıza illa gerek yok ama bu şekilde olsun
                            //lambda şart Değil !
                            runOnUiThread(() ->
                            {
                                /*
                                for (int a = 0; a < scannedArray.size(); a++)
                                {
                                    Barcode barcode = scannedArray.get(a);
                                }*/

                                Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE);
                                vibrator.vibrate(200);

                                // bu ikiliyi çağırmayı unutmayın yoksa çalışmaya devam edecektir.
                                cameraSource.stop();
                                cameraSource.release();

                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append("Kod Tipi : ");
                                // Kod Array biçiminde geldiğini ssöylemiştim şimdi eğer ki kodunuz çoklu ise ister foreach çalıştırarak hepsini alabilirsiniz.
                                // Ben tekli kodlarla çalıştığım için direk valueAt(0) yazıp içeri alabiliyorum foreach örnek
                                // valueFormat ize kodun tipini yazar, ben iki tane yaptığım için if else yapacağım siz çoklu yapacaksanız eğer switch case öneririm
                                if (scannedArray.valueAt(0).valueFormat == Barcode.QR_CODE)
                                    stringBuilder.append("QR-KOD");
                                else
                                    stringBuilder.append("BARKOD");

                                stringBuilder.append("\n \n");
                                stringBuilder.append("KOD : ");
                                // display Value de kodun değerin bize söyller
                                stringBuilder.append(scannedArray.valueAt(0).displayValue);

                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setMessage(stringBuilder.toString());
                                builder.show();
                            });
                        }
                    };
                    thread.start();
                }
            }
        });
    }

    private void layoutInflateIndex()
    {
        // layoutu içeriye alalım
        surfaceView = findViewById(R.id.am_surfaceView);
        scanNew = findViewById(R.id.am_scanNew);
    }
}