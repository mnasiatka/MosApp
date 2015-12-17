package com.finalproject.mosapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;


public class FinalActivity extends ActionBarActivity {

    Bitmap baseImage;
    Matrix matrix;
    ArrayList<Bitmap> dirImages;
    int[] includeArray;

    int mProgressStatus = 0;
    ProgressBar progressBar;
    Handler mHandler = new Handler();
    SeekBar seekBar;
    TextView textview;

    ImageView imageView;
    ZoomInZoomOut zoomer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final);

        getSupportActionBar().setTitle("4. Output image");

        Bundle bundle = getIntent().getExtras();
        byte[] byteArray = bundle.getByteArray("image");
        baseImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        initViews();
        baseImageHandler();


    }

    private void initViews() {
        imageView = (ImageView) findViewById(R.id.imageview);

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
}
