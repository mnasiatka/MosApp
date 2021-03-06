package com.finalproject.mosapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
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
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class AdjustSettingsActivity extends AppCompatActivity implements View.OnClickListener,
        ZoomCallback {

    Bitmap baseImage, baseImageGrid;
    Matrix matrix;
    ArrayList<Bitmap> dirImages;
    int[] includeArray;
    Context mContext;

    int mProgressStatus = 0;
    ProgressBar progressBar;
    Handler mHandler = new Handler();
    SeekBar seekBar, blendseekBar;
    TextView textview, toptextview, blendtextview;
    CheckBox checkBox, noneCheckBox, gridCheckBox;
    ImageView imageView;
    ZoomInZoomOut zoomer;
    Button button;
    MosaicBuilder builder;
    Worker worker;
    double tileSize=50.0;
    public static Bitmap output_image;
    String baseImageURI = "";

    private int base_source = -1;
    private int source = -1;
    private final static int USING_INSTAGRAM = 0;
    private final static int USING_FACEBOOK = 1;
    private final static int USING_FLICKR = 2;
    private final static int USING_GALLERY = 3;
    private final static int USING_URLS = 0;
    private final static int USING_BITMAPS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjust_settings);

        getSupportActionBar().setTitle("3. Adjust settings");

        Bundle bundle = getIntent().getExtras();
        //byte[] baseArray = bundle.getByteArray("image");
        //baseImage = BitmapFactory.decodeByteArray(baseArray, 0, baseArray.length);
        baseImageURI = bundle.getString("URI");
        source = bundle.getInt("source");
        base_source = bundle.getInt("base_source");
        //float[] mValues = bundle.getFloatArray("matrix");
        //matrix = new Matrix();
        //matrix.setValues(mValues);
        dirImages = MainActivity2.imagesToUse;
        //includeArray = bundle.getIntArray("include");
        mContext = getApplicationContext();
        initViews();

        //updateProgressBar();
        textview.setText("Number of images we're using: " + dirImages.size());
        toptextview.setText("Use Slider to Select Tile Size");
        blendtextview.setText("Set Blend Between Base Image and Tiles");

        new getBitmapFromURL().execute();

        //baseImage = Bitmap.createBitmap(baseImage, 0,0,baseImage.getWidth(), baseImage
        // .getHeight(), matrix, true);

        //textview.setText(visualMatrix(matrix));
        //textview.setText(mValues.length);
        //for (float f : mValues) {
        //    Log.e("Matrix values", "" + f);
        //}

    }

    @Override
    public void zoomCallback(float level) {
        int lvl = Math.round(level);
        Log.e("Zoom Callback", "" + Math.min(lvl,3));

    }

    private void initViews() {
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        progressBar.setMax(6000);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setProgress((int) tileSize);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(gridCheckBox.isChecked()) {
                    baseImageGrid = addGridLines(baseImage);
                    baseImageHandler(baseImageGrid);
                } else {
                    baseImageHandler(baseImage);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(gridCheckBox.isChecked()) {
                    baseImageGrid = addGridLines(baseImage);
                    baseImageHandler(baseImageGrid);
                } else {
                    baseImageHandler(baseImage);
                }
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(gridCheckBox.isChecked()) {
                    baseImageGrid = addGridLines(baseImage);
                    baseImageHandler(baseImageGrid);
                } else {
                    baseImageHandler(baseImage);
                }
            }
        });


        blendseekBar = (SeekBar) findViewById(R.id.blendseekbar);
        blendseekBar.setProgress((int) (100.0 - calcOptimalBlend()));
        blendseekBar.setEnabled(false);

        textview = (TextView) findViewById(R.id.textview);
        toptextview = (TextView) findViewById(R.id.toptextview);
        blendtextview = (TextView) findViewById(R.id.blendtextview);
        imageView = (ImageView) findViewById(R.id.imageview);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);

        gridCheckBox = (CheckBox) findViewById(R.id.gridcheckbox);
        gridCheckBox.setChecked(true);

        gridCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                @Override
                                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                    if (isChecked) {
                                                        baseImageHandler(baseImageGrid);
                                                    } else
                                                        baseImageHandler(baseImage);
                                                }
                                            }
        );

        checkBox = (CheckBox) findViewById(R.id.blendcheckbox);
        checkBox.setChecked(true);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                @Override
                                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                    if (isChecked) {
                                                        noneCheckBox.setChecked(false);
                                                        blendseekBar.setEnabled(false);
                                                        blendseekBar.setProgress((int) (100.0 - calcOptimalBlend()));
                                                    } else
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

    private void baseImageHandler(Bitmap baseImage) {
        if (imageView.getDrawable() == null) {
            imageView.setImageBitmap(baseImage);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setAdjustViewBounds(true);
            zoomer = new ZoomInZoomOut(getApplicationContext(), imageView, this);
        }
        else
        {
            imageView.setImageBitmap(baseImage);
        }
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
        tileSize = 50.0  + 1.0* seekBar.getProgress();
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
                    Log.e("EVENT", "reached callback!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    //imageView.setImageBitmap(builder.getStitched());
                    output_image = builder.getStitched();

                    Intent intent = new Intent(getApplicationContext(), FinalActivity.class);
                    System.out.println("started activity");
                    startActivity(intent);

                }
            };
            Log.e("Size", seekBar.getProgress() + "");
            builder = new MosaicBuilder(getApplicationContext(), tileSize ,baseImage, blendweight, dirImages,
                    worker);
            builder.setOptions(options);
            builder.execute();
        }

    }

    private class getBitmapFromURL extends AsyncTask<Void, Void, Boolean> {

        public getBitmapFromURL() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.e("Source", base_source + ":" + source);
            if (base_source != USING_GALLERY) {
                try {
                    Log.e("Runinng", "running");
                    URL url = new URL(baseImageURI);
                    HttpURLConnection connection = (HttpURLConnection) url
                            .openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    baseImage = BitmapFactory.decodeStream(connection.getInputStream());
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                try {
                    baseImage = MediaStore.Images.Media.getBitmap(getContentResolver(),
                            Uri.parse(baseImageURI));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        }

        @Override
        protected void onPostExecute(Boolean b) {
            if (b) {
                baseImageGrid = addGridLines(baseImage);
                if(gridCheckBox.isChecked()) {
                    baseImageHandler(baseImageGrid);
                } else {
                    baseImageHandler(baseImage);
                }

            } else {
                Toast.makeText(getApplicationContext(), "Couldn't retrieve anything from " +
                        "the facebook. Sorry!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private double calcOptimalBlend()
    {
        return 100.0* (1f - (0.51605 * Math.exp(-0.031596 * dirImages.size())) );
    }

    private Bitmap addGridLines(Bitmap base)
    {
        System.out.println("started adding grid");

        Bitmap output = Bitmap.createBitmap(base.getWidth(), base.getHeight(), Bitmap.Config.RGB_565);
        Canvas c = new Canvas(output);
        c.drawBitmap(base, 0, 0, new Paint());

        int size = (int) (50  + seekBar.getProgress() );
        int nRows = (int) Math.round(size);
        int nCols = (int) Math.round(size);

        int cellWidth = base.getWidth() / nCols;
        int cellHeight = base.getHeight() / nRows;

        int r = 0 & 0xff, g = 0 & 0xff, b = 0 & 0xff;
        int pixel = (r << 16) | (g << 8) | b;
        System.out.println(pixel);
        System.out.println("base image size: " + base.getHeight() + ", " + base.getWidth());
        System.out.println("base image size: " + output.getHeight() + ", " + output.getWidth());

        System.out.println("started adding grid loop");

        int[] pixels = new int[base.getHeight()];

        for (int i=0; i< pixels.length; i++) {
            pixels[i] = pixel;
        }

        int offset = 0;

        Log.e("Base dimensions", "width=" + base.getWidth() + ", height=" + base.getHeight());

        System.out.println("Width******************************");
        for (int i=cellWidth; i + offset < base.getWidth(); i+=cellWidth) {
            System.out.println(i);
            output.setPixels(pixels, 0, 1, i + offset , 0, 1 , base.getHeight());
            offset++;
        }

        System.out.println("Height******************************");
        offset  = 0;
        pixels = new int[base.getWidth()];
        for (int i=0; i< pixels.length; i++) {
            pixels[i] = pixel;
        }
        for (int i=cellHeight; i + offset < base.getHeight(); i+=cellHeight) {
            System.out.println(i);
            output.setPixels(pixels, 0, base.getWidth(), 0, i + offset, base.getWidth(), 1);
            offset++;
        }


        return output;
    }

}