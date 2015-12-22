package com.finalproject.mosapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class FinalActivity extends ActionBarActivity implements View.OnClickListener {

    Bitmap baseImage;
    Matrix matrix;
    ArrayList<Bitmap> dirImages;
    int[] includeArray;

    int mProgressStatus = 0;
    ProgressBar progressBar;
    Handler mHandler = new Handler();
    SeekBar seekBar;
    TextView textview;
    Button saveButton;

    ImageView imageView;
    ZoomInZoomOut zoomer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("initializing final activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final);

        getSupportActionBar().setTitle("4. Output image");

        System.out.println("unpacking final activity");

        Bundle bundle = getIntent().getExtras();
//        byte[] byteArray = bundle.getByteArray("image");
//        baseImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        baseImage = AdjustSettingsActivity.output_image;

        System.out.println("unpacked image final activity");

        initViews();

        System.out.println("initialized views");

        baseImageHandler();

        System.out.println("handled base image");


    }

    private void initViews() {
        imageView = (ImageView) findViewById(R.id.imageview);
        saveButton = (Button) findViewById(R.id.button);

        saveButton.setOnClickListener(this);

    }

    private void baseImageHandler() {
        imageView.setImageBitmap(baseImage);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setAdjustViewBounds(true);
        zoomer = new ZoomInZoomOut(getApplicationContext(), imageView);
    }

    private void updateProgressBar() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mProgressStatus < 100) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mProgressStatus++;
                            progressBar.setProgress(mProgressStatus);
                            Log.e("Progress", "" + mProgressStatus);
                        }
                    }, 1000);
                }
            }
        }).start();
    }

    private int doWork() {
        return mProgressStatus++;
    }

    private String visualMatrix(Matrix mMatrix) {
        float[] vals = new float[9];
        mMatrix.getValues(vals);
        String s = String.format("Matrix Display\n[%f, %f, %f\n%f, %f, %f\n%f, %f, %f]", vals[0],
                vals[1], vals[2], vals[3], vals[4], vals[5], vals[6], vals[7], vals[8]);
        Log.e("Values", s);
        return s;
    }

    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.button) {
            if(baseImage != null)
                writeFile(baseImage);
            else
                System.out.println("image is null");

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_final, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    private void writeFile(Bitmap bmp)
    {


        try {

            String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
            String path = ("IM_" + timeStamp );
            String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "mosaic";

            File outputDir= new File(dir);

            outputDir.mkdirs();
            File newFile = new File(dir+"/"+path+".jpg");
            FileOutputStream out = new FileOutputStream(newFile);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);

            out.close();
            System.out.println("saved image to : " + newFile.toString());
            Intent mediaScanIntent = new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(newFile);
            mediaScanIntent.setData(contentUri);
            getApplicationContext().sendBroadcast(mediaScanIntent);

        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
