package com.finalproject.mosapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;


public class AdjustSettingsActivity extends AppCompatActivity implements View.OnClickListener {

    Bitmap baseImage;
    Matrix matrix;
    ArrayList<Bitmap> dirImages;
    int[] includeArray;

    int mProgressStatus = 0;
    ProgressBar progressBar;
    Handler mHandler = new Handler();
    SeekBar seekBar, blendseekBar;
    TextView textview, toptextview, blendtextview;
    CheckBox checkBox;
    ImageView imageView;
    ZoomInZoomOut zoomer;
    Button button;
    MosaicBuilder builder;
    Worker worker;
    double tileSize=50.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjust_settings);

        getSupportActionBar().setTitle("3. Adjust settings");

        Bundle bundle = getIntent().getExtras();
        byte[] baseArray = bundle.getByteArray("image");
        baseImage = BitmapFactory.decodeByteArray(baseArray, 0, baseArray.length);
        //float[] mValues = bundle.getFloatArray("matrix");
        //matrix = new Matrix();
        //matrix.setValues(mValues);
        dirImages = MainActivity2.imagesToUse;
        //includeArray = bundle.getIntArray("include");
        initViews();
        //updateProgressBar();
        textview.setText("Number of images we're using: " + dirImages.size());
        toptextview.setText("Use Slider to Select Tile Size");
        blendtextview.setText("Set Blend Between Base Image and Tiles");

        //baseImage = Bitmap.createBitmap(baseImage, 0,0,baseImage.getWidth(), baseImage
        // .getHeight(), matrix, true);

        //textview.setText(visualMatrix(matrix));
        //textview.setText(mValues.length);
        //for (float f : mValues) {
        //    Log.e("Matrix values", "" + f);
        //}

    }

    private void initViews() {
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        //progressBar.setMax(6000);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setProgress( (int) tileSize );

        blendseekBar = (SeekBar) findViewById(R.id.blendseekbar);
        blendseekBar.setProgress((int) calcOptimalBlend());
        blendseekBar.setEnabled(false);

        textview = (TextView) findViewById(R.id.textview);
        toptextview = (TextView) findViewById(R.id.toptextview);
        blendtextview = (TextView) findViewById(R.id.blendtextview);
        imageView = (ImageView) findViewById(R.id.imageview);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        baseImageHandler();

        checkBox = (CheckBox) findViewById(R.id.blendcheckbox);
        checkBox.setChecked(true);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                blendseekBar.setEnabled(!isChecked);
                }
            }
        );

        //imageView.setImageBitmap(baseImage);
        //imageView.setImageMatrix(matrix);
        //imageView.setScaleType(ImageView.ScaleType.MATRIX);
        //imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        //imageView.setAdjustViewBounds(true);

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
        getMenuInflater().inflate(R.menu.menu_adjust_settings, menu);
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        tileSize = 100.0  + 1.0* seekBar.getProgress();
        System.out.println(tileSize);
        if (id == R.id.button) {
            MosaicBuilderOptions options = new MosaicBuilderOptions(false);
            worker = new Worker();
            worker.callback = new MyCallback() {

                public void callbackCall() {
                    Log.e("EVENT", "reached " +
                            "callback!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    imageView.setImageBitmap(builder.getStitched());
                    builder.getStitched();
                }
            };
            Log.e("Size", seekBar.getProgress() + "");
            builder = new MosaicBuilder(getApplicationContext(),  tileSize ,baseImage,dirImages,
                    worker);
            builder.setOptions(options);
            builder.execute();

        }
    }



    private String writeFile(Bitmap bmp) {
        File outputDir = getCacheDir(); // context being the Activity pointer
        String path = "";
        try {
            File outputFile = File.createTempFile("tempPic", ".jpg", outputDir);
            path = outputFile.getAbsolutePath();
            System.out.println("output path: " + path);
            OutputStream outStream = new FileOutputStream(outputFile);
            bmp.compress(Bitmap.CompressFormat.PNG, 85, outStream);
            outStream.flush();
            outStream.close();
        } catch (IOException e) {
            System.out.println("Error writing file");
        }
        return path;
    }

    private double calcOptimalBlend()
    {
        return 100.0* (1f - (0.51605 * Math.exp(-0.031596 * dirImages.size())) );
    }
}