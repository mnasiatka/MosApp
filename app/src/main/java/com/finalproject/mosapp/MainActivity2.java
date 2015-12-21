package com.finalproject.mosapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity2 extends AppCompatActivity implements View.OnClickListener {


    private ImageView ivBaseImage;
    boolean debug = true;
    private static final String CLIENT_ID = "3c1cafe50e1e47aa891ae09c4a2403d7";
    private static final String CLIENT_SECRET = "04561de2472e45568805bab8e27eaf69";
    private static final String CALLBACK_URL = "http://ythogh.com";
    private static final String SHARED = "Instagram_Preferences";
    private static final String API_USERNAME = "username";
    private static final String API_ID = "id";
    private static final String API_NAME = "name";
    private static final String API_ACCESS_TOKEN = "access_token";
    private static String INSTAGRAM_ACCESS_TOKEN = "";
    private static String INSTAGRAM_USER_ID = "";

    public static int USING_INSTAGRAM = 0;
    public static int USING_FACEBOOK = 1;
    public static int USING_FLICKR = 2;
    public static int USING_GALLERY = 3;

    public static int source = -1;

    String TAG = "DEBUG";
    ArrayList<String> dirImages;
    ArrayList<Bitmap> dirImages2;
    ImageView ivAddRow;
    TextView tvSearchFor;
    HorizontalListView hlistview;
    HorizontalScrollView scrollView;
    GridView gvPhotos;
    Button btAddTags, btSubmit, btChooseImages, btUseFacebook, btUseInstagram;
    Bitmap imageBitmap;
    File imageFile;
    String imageFilename = "";
    int newWidth = 300;
    int newHeight = 300;
    Double initialMin = 50.00;
    Double initialMax = 100.00;
    CustomListAdapter mListAdapter;
    ArrayList<String> enteredTags = new ArrayList<>();
    LoginButton mLoginButton;
    CallbackManager callbackManager;
    ArrayList<String> photoUrls;
    ViewGroup scrollGroup;
    InstagramApp mApp;
    int numPhotoIDs = 0;
    Double minSliderVal = initialMin;
    String minText = "Speed";
    String maxText = "Images";
    public Set<Integer> includePhoto;
    public static ArrayList<Bitmap> imagesToUse;

    int expectedSize = 25;
    //int TAKE_PHOTO = 1;
    int CHOOSE_PHOTO = 2;
    int CHOOSE_MULTIPLE_PHOTOS = 3;
    Matrix matrix;
    float[] mValues = new float[9];
    RecyclerView recyclerView;
    MainAdapter adapter;

    ImageLoader imageLoader;

    getInstagramInfo IGasync;
    GraphRequest FBasync;

    String baseImageURI = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main2);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
        imageLoader = ImageLoader.getInstance();
        initViews();
        dirImages = new ArrayList<>();
        dirImages2 = new ArrayList<>();

        getSupportActionBar().setTitle("2. Choose smaller photos");

        //matrix = MainActivity.matrix;
        //matrix.getValues(mValues);
        Toast.makeText(getApplicationContext(), "Main Activity 2", Toast.LENGTH_SHORT).show();
        try {
            //byte[] byteArray = getIntent().getByteArrayExtra("image");
            //imageBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            baseImageURI = getIntent().getExtras().getString("URI");

        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
        mLoginButton.setVisibility(View.GONE);



        btAddTags.setVisibility(View.GONE);
        //ivBaseImage.setVisibility(View.GONE);
    }

    private void initViews() {
        mLoginButton = (LoginButton) findViewById(R.id.login_button);
        // ivBaseImage = (ImageView) findViewById(R.id.base_image);
        btAddTags = (Button) findViewById(R.id.button);
        btSubmit = (Button) findViewById(R.id.search);
        btChooseImages = (Button) findViewById(R.id.choose_images);
        btUseInstagram = (Button) findViewById(R.id.instagram_button);
        btUseFacebook = (Button) findViewById(R.id.fb_images);
        tvSearchFor = (TextView) findViewById(R.id.searchfor);
        scrollView = (HorizontalScrollView) findViewById(R.id.hscrollview);
        hlistview = (HorizontalListView) findViewById(R.id.hlistview);
        scrollGroup = (ViewGroup) findViewById(R.id.llImages);

        //ivBaseImage.setOnClickListener(this);
        btChooseImages.setOnClickListener(this);
        btUseFacebook.setOnClickListener(this);
        btUseInstagram.setOnClickListener(this);
        btAddTags.setOnClickListener(this);
        btSubmit.setOnClickListener(this);

        //ivBaseImage.setScaleType(ImageView.ScaleType.MATRIX);


        recyclerView = (RecyclerView) findViewById(R.id.list);

        if (getIntent().getBooleanExtra("GRID", true)) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        recyclerView.setItemAnimator(new FadeInAnimator());

        setupAdapter();

        recyclerView.setItemAnimator(new FadeInAnimator(new OvershootInterpolator(1f)));
        recyclerView.getItemAnimator().setAddDuration(500);
        recyclerView.getItemAnimator().setRemoveDuration(500);


        final RangeSeekBar<Double> seekBar = new RangeSeekBar<>(0.00, 100.00, minText, maxText,
                getApplicationContext());
        seekBar.setSelectedMinValue(initialMin);
        seekBar.setSelectedMaxValue(initialMax);
        seekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Double>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Double minValue, Double maxValue) {
                System.out.println("******On Values Changed******");
                Log.i("tag", "User selected new range values: MIN=" + minValue + ", MAX=" + maxValue);
            }
        });
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    System.out.println("******On Touch******");
                    System.out.println(seekBar.getSelectedMinValue() + ", " + seekBar.getSelectedMaxValue());
                    minSliderVal = seekBar.getSelectedMinValue();
                }
                return false;
            }
        });
        // add RangeSeekBar to pre-defined layout
        //ViewGroup layout = (ViewGroup) findViewById(R.id.seekbar);
        //layout.addView(seekBar);

        //mLoginButton.setReadPermissions("user_friends");
        mLoginButton.setReadPermissions("user_photos");
        callbackManager = CallbackManager.Factory.create();
        mLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken at = loginResult.getAccessToken();
                if (debug) {
                    System.out.println("****************************************************");
                    Log.e(TAG, "On Success reached");
                    Log.e("Application ID", at.getApplicationId());
                    Log.e("Token", at.getToken());
                    Log.e("User ID", at.getUserId());
                    Log.e("Access Token", at.toString());
                    Log.e("Expires", at.getExpires().toString());
                    for (String s : at.getPermissions()) {
                        Log.e("Permissions", s);
                    }
                    for (String s : at.getDeclinedPermissions()) {
                        Log.e("Declined Permissions", s);
                    }
                    System.out.println("****************************************************");
                }
                useFacebook();
            }

            @Override
            public void onCancel() {
                Log.e(TAG, "On Cancel reached");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e(TAG, "On Error reached");
                Log.e(TAG, exception.toString());
            }
        });
    }

    private void setupAdapter() {
        adapter = new MainAdapter(this, new ArrayList());
        recyclerView.setAdapter(adapter);
        includePhoto = new HashSet<>();

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener
                        .OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        ImageView include = (ImageView) view.findViewById(R.id.include);
                        ImageView image = (ImageView) view.findViewById(R.id.image);
                        if (includePhoto.contains(position)) { // already checked, remove and show x
                            include.setImageResource(R.drawable.xcheck);
                            image.setAlpha(.3f);
                            includePhoto.remove(position);
                        } else { // not being shown, add to set and show check
                            include.setImageResource(R.drawable.check2);
                            image.setAlpha(1f);
                            includePhoto.add(position);
                        }
                        Log.e("Include check", position + " should have a " + (includePhoto
                                .contains(position) ? "check" : "x-mark"));
                    }
                })
        );

    }

    private void resetScrollView() {
        photoUrls = new ArrayList<>();
        dirImages = new ArrayList<>();
        dirImages2 = new ArrayList<>();
        scrollGroup.removeAllViewsInLayout();
        scrollGroup.removeAllViews();
    }

    private void resetRecyclerView() {
        photoUrls = new ArrayList<>();
        dirImages = new ArrayList<>();
        dirImages2 = new ArrayList<>();

        setupAdapter();
    }

    private void imageHandler(ArrayList<String> urls) {
        for (String url : urls) {
            imageHandler(url);
        }
    }

    private void imageHandler(String[] urls) {
        for (String url : urls) {
            imageHandler(url);
        }
    }

    private void imageHandler(String url) {
        photoUrls.add(url);
        View v = new ImageView(getBaseContext());
        ImageView imgView;
        imgView = new ImageView(v.getContext());
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(url, imgView);
        imgView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imgView.setAdjustViewBounds(true);
        scrollGroup.addView(imgView);
        //dirImages.add(imageLoader.loadImageSync(url));
    }

    private void imageHandler(Bitmap bmp) {
        dirImages2.add(bmp);
        View v = new ImageView(getBaseContext());
        ImageView imgView;
        imgView = new ImageView(v.getContext());
        imgView.setImageBitmap(bmp);
        imgView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imgView.setAdjustViewBounds(true);
        scrollGroup.addView(imgView);
    }

    private void gridImageHandler(Bitmap bmp) {
        dirImages2.add(bmp);
        View v = new ImageView(getBaseContext());
        ImageView imgView;
        imgView = new ImageView(v.getContext());
        imgView.setImageBitmap(bmp);
        imgView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imgView.setAdjustViewBounds(true);

        includePhoto.add(adapter.getItemCount());
        adapter.add(bmp, adapter.getItemCount());
    }

    private void gridImageHandler(String[] urls) {
        for (String url : urls) {
            gridImageHandler(url);
        }
    }

    private void gridImageHandler(String url) {
        photoUrls.add(url);
        imageLoader.loadImage(url, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                dirImages2.add(loadedImage);
                includePhoto.add(adapter.getItemCount());
                adapter.add(loadedImage, adapter.getItemCount());
            }
        });
        //adapter.add(((BitmapDrawable) imgView.getDrawable()).getBitmap(), adapter.getItemCount());
    }

    public void updateAdapter(ArrayList<Bitmap> imgs) {
        dirImages2 = imgs;
        hlistview.setAdapter(mHorizontalAdapter);
        hlistview.requestLayout();
    }

    private void useFacebook() {
        AccessToken at = AccessToken.getCurrentAccessToken();
        if (at == null) {
            mLoginButton.callOnClick();
        } else {
            String albumURL = String.format("/%s/photos", at.getUserId());
            resetScrollView();
            resetRecyclerView();
            FBasync = new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    albumURL,
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            try {
                                if (response == null) {
                                    Log.e(TAG, "Response is null");
                                } else {
                                    JSONArray arr = response.getJSONObject().getJSONArray
                                            ("data");
                                    String photoID = "";
                                    numPhotoIDs = arr.length();
                                    for (int i = 0; i < arr.length(); i++) {
                                        photoID = arr.getJSONObject(i).getString("id");
                                        GraphRequest request = new GraphRequest(
                                                AccessToken.getCurrentAccessToken(),
                                                photoID,
                                                null,
                                                HttpMethod.GET,
                                                new GraphRequest.Callback() {
                                                    public void onCompleted(GraphResponse response) {
                                                        try {
                                                            String photoURL = response.getJSONObject().getString("picture");
                                                            gridImageHandler(photoURL);
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                        );
                                        Bundle parameters = new Bundle();
                                        parameters.putString("fields", "id,name,link,picture");
                                        parameters.putInt("limit",100);
                                        request.setParameters(parameters);
                                        request.executeAsync();
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
            );
            if (IGasync != null) IGasync.cancel(true); ;
            FBasync.executeAsync();
        }
    }

    private class getFacebookData extends AsyncTask<String, Void, String> {

        String query = "";

        public getFacebookData() {
            AccessToken at = AccessToken.getCurrentAccessToken();
            query = "https://graph.facebook.com/v2.4/" + at.getUserId() + "/photos?access_token="
                    + at.getToken() + "&format=json&sdk=android";
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(query);
                System.out.println(url.toString());
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.connect();

                // InputStream input = connection.getInputStream();
                System.out.println("Response message");
                System.out.println(connection.getResponseMessage());
                System.out.println("Response message");
                System.out.println(connection.getInputStream());
                System.out.println("Response message");
                System.out.println(connection.getOutputStream());
                System.out.println("Response message");

                connection.disconnect();
                return "";
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String str) {

        }
    }

    private void useInstagram() {
        mApp = new InstagramApp(this, CLIENT_ID,
                CLIENT_SECRET, CALLBACK_URL);
        Log.e("Usage", "In useInstagram method");
        mApp.setListener(new InstagramApp.OAuthAuthenticationListener() {
            @Override
            public void onSuccess() {
                mApp.getUserName();
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(MainActivity2.this, error, Toast.LENGTH_SHORT).show();
            }
        });
        if (mApp.hasAccessToken()) {
            getInstagramInfo();
        } else {
            mApp.authorize();
        }

    }

    private void getInstagramInfo() {
        Log.e("onSuccess", "Connected to instagram");

        SharedPreferences shared = getApplicationContext().getSharedPreferences(SHARED, Context
                .MODE_PRIVATE);
        INSTAGRAM_ACCESS_TOKEN = shared.getString(API_ACCESS_TOKEN, "");
        INSTAGRAM_USER_ID = shared.getString(API_ID, "");

        Log.e("Position", "About to execute get instagram info");
        IGasync = new getInstagramInfo();
        IGasync.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.base_image:
                //CustomDialogClass cdd = new CustomDialogClass(MainActivity2.this, R.style
                //    .DialogSlideAnim);
                //cdd.show();
                break;
            case R.id.button:
                AddItemsDialogClass add = new AddItemsDialogClass(MainActivity2.this, R.style
                        .DialogSlideAnim);
                add.show();
                break;
            case R.id.search:

                Intent intent = new Intent(getApplicationContext(), AdjustSettingsActivity.class);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                //imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                //byte[] byteArray = stream.toByteArray();
                //intent.putExtra("image", byteArray);

                intent.putExtra("URI", baseImageURI);

                imagesToUse = new ArrayList<>(includePhoto.size());
                ByteArrayOutputStream dirStream = new ByteArrayOutputStream();
                byte[] dirArray;
                int index = 0;
                String key = "";

                for (int i = 0; i<dirImages2.size(); i++) {
                    if (includePhoto.contains(i)) {
                        /*
                        key = "dirImage_" + index;

                        dirImages2.get(i).compress(Bitmap.CompressFormat.PNG,100,dirStream);
                        dirArray = dirStream.toByteArray();
                        intent.putExtra(key, dirArray);
                        index++;
                        */
                        imagesToUse.add(dirImages2.get(i));
                    }
                }

                //intent.putExtra("image_count", includePhoto.size());



                startActivity(intent);

                //new MosaicBuilder(getApplicationContext(), 50.0, imageBitmap, dirImages2)
                //        .execute();

                break;
            case R.id.choose_images:
                Intent intentPick = new Intent(Intent.ACTION_PICK);
                intentPick.setType("image/*");
                intentPick.setAction(Intent.ACTION_GET_CONTENT);
                intentPick.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intentPick, CHOOSE_MULTIPLE_PHOTOS);
                break;
            case R.id.fb_images:
                useFacebook();
                //new getFacebookData().execute();
                break;
            case R.id.instagram_button:
                useInstagram();
                break;
            default:
                break;
        }
    }

    private int[] includeSetToArray() {
        int[] intArray = new int[includePhoto.size()];
        Iterator it = includePhoto.iterator();
        for (int i = 0; i < includePhoto.size(); i++) {
            intArray[i] = (int)it.next();
        }
        return intArray;
    }

    private class CustomDialogClass extends Dialog implements View.OnClickListener {

        public Activity c;
        public Button /*btTake,*/ btChoose, btCancel;


        public CustomDialogClass(Activity context, int theme) {
            super(context, theme);
            this.c = context;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.custom_dialog_post2);
            btChoose = (Button) findViewById(R.id.CUSTOMDIALOGPOST_BUTTON_CHOOSEPHOTO);
            btCancel = (Button) findViewById(R.id.CUSTOMDIALOGPOST_BUTTON_CANCEL);
            btChoose.setOnClickListener(this);
            btCancel.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.CUSTOMDIALOGPOST_BUTTON_CHOOSEPHOTO:
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    source = USING_GALLERY;
                    c.startActivityForResult(intent, CHOOSE_MULTIPLE_PHOTOS);
                    break;
                case R.id.CUSTOMDIALOGPOST_BUTTON_CANCEL:
                default:
                    break;
            }
            dismiss();
        }
    }

    private class AddItemsDialogClass extends Dialog implements View.OnClickListener {

        Activity c;
        ListView list_view;

        public AddItemsDialogClass(Activity context, int theme) {
            super(context, theme);
            this.c = context;
            enteredTags.add("");
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.list_view);
            list_view = (ListView) findViewById(R.id.list_view);
            mListAdapter = new CustomListAdapter(getApplicationContext(), enteredTags);
            list_view.setAdapter(mListAdapter);
            list_view.requestLayout();
            ivAddRow = (ImageView) findViewById(R.id.add_row);
            ivAddRow.setOnClickListener(this);


        }

        @Override
        public void dismiss() {

            if (list_view.hasFocus()) {
                if (list_view.getFocusedChild() instanceof RelativeLayout) {
                    if (((RelativeLayout) list_view.getFocusedChild()).getFocusedChild() instanceof
                            EditText) {
                        String enteredString = ((EditText) ((RelativeLayout) list_view.getFocusedChild())
                                .getFocusedChild()).getText().toString();
                        System.out.println(enteredString);
                        enteredTags.add(enteredString);
                        mListAdapter.notifyDataSetChanged();
                    }
                } else {

                }
                list_view.clearFocus();
            }
            int count = mListAdapter.getCount();
            int count2 = enteredTags.size();
            System.out.println("Counts");
            System.out.println(count);
            System.out.println(count2);
            System.out.println("************Strings***************");
            String searchingFor = "We'll look for images related to: ";
            for (int i = 1; i < count; i++) {
                System.out.println("On dismiss: " + enteredTags.get(i));
                searchingFor += enteredTags.get(i) + ", ";
            }
            tvSearchFor.setText(searchingFor.substring(0, searchingFor.length() - 2));
            super.dismiss();
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.add_row) {
                System.out.println("adding row");
                if (list_view.hasFocus()) {
                    if (list_view.getFocusedChild() instanceof RelativeLayout) {
                        if (((RelativeLayout) list_view.getFocusedChild()).getFocusedChild() instanceof
                                EditText) {
                            String enteredString = ((EditText) ((RelativeLayout) list_view.getFocusedChild())
                                    .getFocusedChild()).getText().toString();
                            System.out.println(enteredString);
                            enteredTags.add(enteredString);
                            mListAdapter.notifyDataSetChanged();
                        }
                    } else {

                    }
                    list_view.clearFocus();
                }

            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            int width, height;
            float scaleWidth, scaleHeight, scale;
            Matrix matrix;
            String[] fs;
            String newFilePath;
            if (requestCode == CHOOSE_MULTIPLE_PHOTOS) {
                Toast.makeText(getApplicationContext(), "Returned from choosing multiple " +
                        "photos!", Toast.LENGTH_LONG).show();
                Bitmap bmp;
                resetScrollView();
                resetRecyclerView();
                System.out.println("choosing multiple photos");

                ClipData cp = data.getClipData();
                System.out.println("cp: " + cp);
                if (cp != null) {
                    for (int i = 0; i < cp.getItemCount(); i++) {
                        try {
                            ClipData.Item item = cp.getItemAt(i);
                            String path = getRealPathFromURI(this, item.getUri());
                            System.out.println("path " + i + " " + path);
                            bmp = BitmapFactory.decodeFile(path);
                            bmp = Bitmap.createScaledBitmap(bmp, 150, 150, false);
                            Log.e("ClipData Item", path);
                            dirImages.add(path);
                            gridImageHandler(bmp);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Log.e("null check", "" + (data.getData() == null));
                    if (data.getData() != null) {
                        try {
                            for (int i = 0; i < cp.getItemCount(); i++) {
                                bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                                bmp = Bitmap.createScaledBitmap(bmp, 150, 150, false);
                                Log.e("getData Item", data.getData().toString());
                                System.out.println(this.getContentResolver() + " " + data.getData());
                                String path = getRealPathFromURI(getApplicationContext(), cp.getItemAt(0).getUri());
                                dirImages.add(path);
                                gridImageHandler(bmp);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

            } else {
                callbackManager.onActivityResult(requestCode, resultCode, data);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Could not change picture",
                    Toast.LENGTH_LONG).show();
            ;
        }
    }

    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null,
                    null, null);
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int resolveBitmapOrientation(File file) throws IOException {
        ExifInterface exif = null;
        exif = new ExifInterface(file.getAbsolutePath());

        return exif
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
    }

    private Bitmap applyOrientation(Bitmap bitmap, int orientation) {
            int rotate = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
            default:
                return bitmap;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix mtx = new Matrix();
        mtx.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    private class CustomListAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final ArrayList<String> arr;
        private final int initialSize;

        @Override
        public void notifyDataSetChanged() {
            Log.e("data", "data set changed");
            super.notifyDataSetChanged();
        }

        @Override
        public void add(String str) {
            super.add(str);

        }

        public CustomListAdapter(Context context, ArrayList<String> arr) {
            super(context, R.layout.list_item, new ArrayList<String>(arr.size() + 1));
            this.context = context;
            this.arr = arr;
            this.initialSize = arr.size();

        }

        @Override
        public View getView(final int position, View rowView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final SetupViewHolder holder;
            if (rowView == null) {
                rowView = inflater.inflate(R.layout.list_item, parent, false);
                holder = new SetupViewHolder();
                holder.etAddItem = (EditText) rowView.findViewById(R.id.additem);
                holder.etAddItem.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        InputMethodManager imm = (InputMethodManager) getBaseContext().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        return true;
                    }
                });
                rowView.setTag(holder);
            } else {
                holder = (SetupViewHolder) rowView.getTag();
            }
            return rowView;
        }

        @Override
        public int getCount() {
            return arr.size();
        }

        public ArrayList<String> getItems() {
            return arr;
        }
    }

    class SetupViewHolder {
        EditText etAddItem;
    }

    private BaseAdapter mHorizontalAdapter = new BaseAdapter() {

        private View.OnClickListener mOnButtonClicked = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                System.out.println("height: " + v.getHeight() + ", " + v.getMeasuredHeight());
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity2.this);
                builder.setMessage("hello from " + v);
                builder.setPositiveButton("Cool", null);
                builder.show();

            }
        };

        @Override
        public int getCount() {
            return dirImages2.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View retval = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewitem, null);
            //retval.setOnClickListener(mOnButtonClicked);
            ImageView image = (ImageView) retval.findViewById(R.id.image);
            image.setImageBitmap(dirImages2.get(position));
            return retval;
        }

    };

    private class getInstagramInfo extends AsyncTask<String, Void, String[]> {

        public getInstagramInfo() {
        }

        @Override
        protected String[] doInBackground(String... params) {

            String response = "";
            System.out.println("*********************************");
            try {
                URL url = new URL(String.format("https://api.instagram" +
                                ".com/v1/users/%s/media/recent/?access_token=%s",
                        INSTAGRAM_USER_ID, INSTAGRAM_ACCESS_TOKEN));
                System.out.println(url.toString());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.connect();
                response = streamToString(urlConnection.getInputStream());
                System.out.println(response);
                JSONArray data = new JSONObject(response).getJSONArray("data");
                JSONObject obj;
                String photoUrl;
                String[] photoUrls = new String[data.length()];
                ImageLoader imageLoader = ImageLoader.getInstance();
                for (int i = 0; i < data.length(); i++) {
                    obj = data.getJSONObject(i);
                    photoUrl = obj.getJSONObject("images").getJSONObject("low_resolution")
                            .getString("url");
                    System.out.println(photoUrl);

                    photoUrls[i] = photoUrl;
                }
                //String profileImageUrl = data.getString("profile_picture");
                //System.out.println(profileImageUrl);
                //System.out.println(profileImageUrl);
                System.out.println("*********************************");

                urlConnection.disconnect();
                return photoUrls;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] photoUrls) {
            if (photoUrls != null) {
                resetScrollView();
                resetRecyclerView();
                gridImageHandler(photoUrls);
            } else {
                Toast.makeText(getApplicationContext(), "Couldn't retrieve anything from " +
                        "instagram. Sorry!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private String streamToString(InputStream is) throws IOException {
        String str = "";
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
            } finally {
                is.close();
            }
            str = sb.toString();
        }
        return str;
    }

    public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

        private Context mContext;
        private Activity mActivity;
        private List<Bitmap> mDataSet;
        private Map<Integer,ViewHolder> mHolderSet;

        public MainAdapter(Context context, List<Bitmap> dataSet) {
            mContext = context;
            mDataSet = dataSet;
            mHolderSet = new HashMap<>();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.layout_list_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            //Picasso.with(mContext).load(R.drawable.loading).into(holder.image);

            holder.bmp = mDataSet.get(position);
            holder.image.setImageBitmap(mDataSet.get(position));
            if (includePhoto.contains(position)) { // should include, show check
                holder.include.setImageResource(R.drawable.check2);
                holder.image.setAlpha(1f);
            } else { // not in include set, show x
                holder.include.setImageResource(R.drawable.xcheck);
                holder.image.setAlpha(.3f);
            }
        }

        @Override
        public int getItemCount() {
            return mDataSet.size();
        }

        public void remove(int position) {
            mDataSet.remove(position);
            notifyItemRemoved(position);
        }

        public void add(Bitmap bmp, int position) {
            mDataSet.add(position, bmp);
            notifyItemInserted(position);
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            public ImageView image, include;
            public Bitmap bmp;
            public boolean isChecked;

            public ViewHolder(View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.image);
                include = (ImageView) itemView.findViewById(R.id.include);
                isChecked = true;
            }
        }
    }

}

