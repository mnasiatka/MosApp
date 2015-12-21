package com.finalproject.mosapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


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
    CheckBox checkBox, noneCheckBox;
    ImageView imageView;
    ZoomInZoomOut zoomer;
    Button button;
    MosaicBuilder builder;
    Worker worker;
    double tileSize=50.0;
    Button saveButton;
    Bitmap output_image;

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
        progressBar.setMax(6000);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setProgress((int) tileSize);

        blendseekBar = (SeekBar) findViewById(R.id.blendseekbar);
        blendseekBar.setProgress((int) (100.0 - calcOptimalBlend()));
        blendseekBar.setEnabled(false);

        textview = (TextView) findViewById(R.id.textview);
        toptextview = (TextView) findViewById(R.id.toptextview);
        blendtextview = (TextView) findViewById(R.id.blendtextview);
        imageView = (ImageView) findViewById(R.id.imageview);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);

        saveButton = (Button) findViewById(R.id.button2);
        saveButton.setOnClickListener(this);

        baseImageHandler();

        checkBox = (CheckBox) findViewById(R.id.blendcheckbox);
        checkBox.setChecked(true);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        noneCheckBox.setChecked(false);
                        blendseekBar.setEnabled(false);
                        blendseekBar.setProgress((int) (100.0 - calcOptimalBlend()));
                    }
                    else
                        blendseekBar.setEnabled(true);
                }
            }
        );

        noneCheckBox = (CheckBox) findViewById(R.id.nonecheckbox);
        noneCheckBox.setChecked(false);

        noneCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        checkBox.setChecked(false);
                        blendseekBar.setEnabled(false);
                        blendseekBar.setProgress(0);
                    }
                    else
                        blendseekBar.setEnabled(true);
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
        double blendweight = 0.0;
        if (blendseekBar.isEnabled()) {
            blendweight = 1- (blendseekBar.getProgress()/100.0);
        }
        else
        {
            blendweight = calcOptimalBlend()/100.0;
        }

        System.out.println("bw: " +blendweight);
        System.out.println("tilesize: " +tileSize);
        if (id == R.id.button) {
            MosaicBuilderOptions options = new MosaicBuilderOptions(!noneCheckBox.isChecked());
            worker = new Worker();
            worker.callback = new MyCallback() {

                public void callbackCall() {
                    Log.e("EVENT", "reached " +
                            "callback!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    imageView.setImageBitmap(builder.getStitched());
                    output_image = builder.getStitched();

//                    Intent intent = new Intent(getApplicationContext(), FinalActivity.class);
//
//                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                    output_image.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                    byte[] byteArray = stream.toByteArray();
//                    intent.putExtra("image", byteArray);
//                    System.out.println("starting final activity");
//                    startActivity(intent);
//                    System.out.println("started activity");
                }
            };
            Log.e("Size", seekBar.getProgress() + "");
            builder = new MosaicBuilder(getApplicationContext(),  tileSize ,baseImage, blendweight, dirImages,
                    worker);
            builder.setOptions(options);
            builder.execute();
        }

        else if (id == R.id.button2) {
            if(output_image == null)
            {
                System.out.println("bitmap is null");
            }
            else {
                writeFile(output_image);
            }

        }
    }

    private double calcOptimalBlend()
    {
        return 100.0* (1f - (0.51605 * Math.exp(-0.031596 * dirImages.size())) );
    }


    private void writeFile(Bitmap bmp)
    {


        try {
            String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
            String path = ("IM_" + timeStamp );
            String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "mosaic";

            File outputDir= new File(dir);

            outputDir.mkdirs();
            File newFile = new File(dir+"/"+path+"-"+".jpg");
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