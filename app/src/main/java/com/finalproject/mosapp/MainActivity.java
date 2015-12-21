package com.finalproject.mosapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {



    private static boolean debug = true;
    private static final String TAG = "DEBUG";
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

    public static int source = -1;
    public static int USING_INSTAGRAM = 0;
    public static int USING_FACEBOOK = 1;
    public static int USING_FLICKR = 2;
    public static int USING_GALLERY = 3;

    public static ImageView ivBaseImage;
    private ZoomInZoomOut zoomer;

    ArrayList<Bitmap> dirImages;
    ImageView ivAddRow;
    TextView tvSearchFor;
    HorizontalListView hlistview;
    HorizontalScrollView scrollView;
    Button btAddTags, btSubmit, btChooseImages, btUseFacebook, btUseInstagram, btTest;
    Bitmap imageBitmap;
    File imageFile;
    String imageFilename = "";
    //int newWidth = 300;
    //int newHeight = 300;
    Double initialMin = 50.00;
    Double initialMax = 100.00;
    CustomListAdapter mListAdapter;
    ArrayList<String> enteredTags = new ArrayList<>();
    LoginButton mLoginButton;
    CallbackManager callbackManager;
    ArrayList<String> photoUrls;
    ViewGroup scrollGroup;
    InstagramApp mApp;
    int numPhotoIDs = 1;
    Double minSliderVal = initialMin;
    String baseImageUrl = "";
    GraphRequest FBasync;
    ImageLoader imageLoader;
    public int selectedPhoto;
    public View selectedView;

    RecyclerView recyclerView;
    MainAdapter adapter;
    Map<Integer,String> photoURIs;
    public static Matrix matrix, savedMatrix;

    int expectedSize = 25;
    int TAKE_PHOTO = 1;
    int CHOOSE_PHOTO = 2;
    int CHOOSE_MULTIPLE_PHOTOS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
        imageLoader = ImageLoader.getInstance();
        initViews();

        btAddTags.setVisibility(View.GONE);
        
        getSupportActionBar().setTitle("1. Pick the base image");

        Toast.makeText(getApplicationContext(),"Main Activity",Toast.LENGTH_SHORT).show();

    }

    private void initViews() {
        mLoginButton = (LoginButton) findViewById(R.id.login_button);
        ivBaseImage = (ImageView) findViewById(R.id.base_image);
        btAddTags = (Button) findViewById(R.id.button);
        btSubmit = (Button) findViewById(R.id.next);
        btChooseImages = (Button) findViewById(R.id.choose_images);
        btUseFacebook = (Button) findViewById(R.id.fb_images);
        btUseInstagram = (Button) findViewById(R.id.instagram_button);
        btTest = (Button) findViewById(R.id.test);

        //ivBaseImage.setOnClickListener(this);
        btChooseImages.setOnClickListener(this);
        btUseFacebook.setOnClickListener(this);
        btUseInstagram.setOnClickListener(this);
        btAddTags.setOnClickListener(this);
        btSubmit.setOnClickListener(this);
        btTest.setOnClickListener(this);

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

        recyclerView = (RecyclerView) findViewById(R.id.list);

        if (getIntent().getBooleanExtra("GRID", true)) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        recyclerView.setItemAnimator(new FadeInAnimator());

        resetRecyclerView();

        recyclerView.setItemAnimator(new FadeInAnimator(new OvershootInterpolator(1f)));
        recyclerView.getItemAnimator().setAddDuration(500);
        recyclerView.getItemAnimator().setRemoveDuration(500);

    }

    private void resetRecyclerView() {
        photoUrls = new ArrayList<>();
        dirImages = new ArrayList<>();
        photoURIs = new HashMap<>();

        adapter = new MainAdapter(this, new ArrayList());
        recyclerView.setAdapter(adapter);
        selectedPhoto = -1;

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener
                        .OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (selectedView != null) {
                            ImageView selectedPhotoInclude = (ImageView) selectedView.findViewById(R.id
                                    .include);
                            ImageView selectedPhotoImage = (ImageView) selectedView.findViewById(R.id.image);
                            //selectedPhotoInclude.setImageResource(R.drawable.xcheck);
                            selectedPhotoInclude.setVisibility(View.GONE);
                            selectedPhotoImage.setAlpha(.7f);
                        }
                        ImageView include = (ImageView) view.findViewById(R.id.include);
                        ImageView image = (ImageView) view.findViewById(R.id.image);
                        if (selectedPhoto == position ) { // already checked, remove and show x
                            include.setImageResource(R.drawable.xcheck);
                            include.setVisibility(View.GONE);
                            image.setAlpha(.7f);
                            selectedPhoto = -1;
                        } else { // not being shown, add to set and show check
                            include.setImageResource(R.drawable.check2);
                            image.setAlpha(1f);
                            include.setVisibility(View.VISIBLE);
                            selectedPhoto = position;
                        }
                        selectedView = view;
                        Log.e("Selected photo", "" + position);
                    }
                })
        );
    }

    private void baseImageHandler(String url) {
        baseImageUrl = url;
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(url, ivBaseImage);
        ivBaseImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        ivBaseImage.setAdjustViewBounds(true);

    }

    private void getFacebookData() {
        AccessToken at = AccessToken.getCurrentAccessToken();
        if (at == null) {
            mLoginButton.callOnClick();
        } else {
            // Gets profile picture
            String profileURL = String.format("/%s/picture", at.getUserId());
            GraphRequest request = new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    profileURL,
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            try {
                                baseImageHandler(response.getConnection().getURL().toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
            );
            Bundle parameters = new Bundle();
            parameters.putString("type", "large");
            request.setParameters(parameters);
            Log.e("Request url", request.toString());
            request.executeAsync();

            // Gets album photos
            String albumURL = String.format("/%s/photos", at.getUserId());
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
                                    System.out.println(arr);
                                    String photoID = "";
                                    numPhotoIDs = arr.length();
                                    for (int i = 0; i < arr.length(); i++) {
                                        photoID = arr.getJSONObject(i).getString("id");
                                        final int index = i;
                                        GraphRequest request = new GraphRequest(
                                                AccessToken.getCurrentAccessToken(),
                                                photoID,
                                                null,
                                                HttpMethod.GET,
                                                new GraphRequest.Callback() {
                                                    public void onCompleted(GraphResponse response) {
                                                        try {
                                                            String photoURL = response.getJSONObject().getString("picture");
                                                            gridImageHandler(index, photoURL);
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                        );
                                        Bundle parameters = new Bundle();
                                        parameters.putString("fields", "id,name,link,picture");
                                        parameters.putString("type","large");
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
            FBasync.executeAsync();




        }
    }

    private void gridImageHandler(Bitmap bmp) {
       // dirImages2.add(bmp);
        View v = new ImageView(getBaseContext());
        ImageView imgView;
        imgView = new ImageView(v.getContext());
        imgView.setImageBitmap(bmp);
        imgView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imgView.setAdjustViewBounds(true);

        adapter.add(bmp, adapter.getItemCount());
    }

    private void gridImageHandler(String[] urls) {
        for (int i=0;i<urls.length;i++) {
            gridImageHandler(i,urls[i]);
        }
    }

    private void gridImageHandler(int i,String url) {
        photoURIs.put(i, url);
        imageLoader.loadImage(url, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                Log.e(imageUri, loadedImage.getWidth() + ", " + loadedImage.getHeight());
                adapter.add(loadedImage, adapter.getItemCount());
            }
        });
        //adapter.add(((BitmapDrawable) imgView.getDrawable()).getBitmap(), adapter.getItemCount());
    }

    private class getFacebookData2 extends AsyncTask<String, Void, String> {

        String query = "";
        public getFacebookData2() {
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
                Log.e("username", mApp.getUserName());
                getInstagramInfo();
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
        if (!mApp.hasAccessToken()) {
            mApp.authorize();
        } else {
            getInstagramInfo();
        }
    }

    private void getInstagramInfo() {
        Log.e("onSuccess", "Connected to instagram");

        SharedPreferences shared = getApplicationContext().getSharedPreferences(SHARED, Context
                .MODE_PRIVATE);
        INSTAGRAM_ACCESS_TOKEN = shared.getString(API_ACCESS_TOKEN, "");
        INSTAGRAM_USER_ID = shared.getString(API_ID, "");

        Log.e("Position", "About to execute get instagram info");
        new getInstagramInfo().execute();
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
                //CustomDialogClass cdd = new CustomDialogClass(MainActivity.this, R.style
                //    .DialogSlideAnim);
                //cdd.show();
                break;
            case R.id.button:
                AddItemsDialogClass add = new AddItemsDialogClass(MainActivity.this, R.style
                        .DialogSlideAnim);
                add.show();
                break;
            case R.id.next:
                Log.e("Positoin", "sending image url at " + selectedPhoto);
                imageBitmap = ((BitmapDrawable)((ImageView)recyclerView.getChildAt(selectedPhoto)
                        .findViewById(R.id.image)).getDrawable()).getBitmap();
                System.out.println("base image size in main activity 1: " + imageBitmap.getHeight() + ", " + imageBitmap.getWidth());
                Intent i = new Intent(getApplicationContext(), MainActivity2.class);
                i.putExtra("URI",photoURIs.get(selectedPhoto));
                //ByteArrayOutputStream stream = new ByteArrayOutputStream();
                //imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                //byte[] byteArray = stream.toByteArray();
                //i.putExtra("image", byteArray);
                startActivity(i);
                break;
            case R.id.choose_images:
                resetRecyclerView();
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                startActivityForResult(intent, CHOOSE_MULTIPLE_PHOTOS);
                source = USING_GALLERY;
                onImageChosen();
                break;
            case R.id.fb_images:
                resetRecyclerView();
                getFacebookData();
                onImageChosen();
                //new getFacebookData().execute();
                break;
            case R.id.instagram_button:
                resetRecyclerView();
                useInstagram();
                onImageChosen();
                break;
            case R.id.test:
                Intent testIntent = new Intent(getApplicationContext(), CropImageActivity.class);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                imageBitmap = ((BitmapDrawable) ivBaseImage.getDrawable()).getBitmap();
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                testIntent .putExtra("image", byteArray);
                startActivity(testIntent);
                break;
            default:
                break;
        }
    }

    private class CustomDialogClass extends Dialog implements View.OnClickListener{

        public Activity c;
        public Button btTake, btChoose, btCancel;


        public CustomDialogClass(Activity context, int theme) {
            super(context, theme);
            this.c = context;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.custom_dialog_post);
            btTake = (Button) findViewById(R.id.CUSTOMDIALOGPOST_BUTTON_TAKEPHOTO);
            btChoose = (Button) findViewById(R.id.CUSTOMDIALOGPOST_BUTTON_CHOOSEPHOTO);
            btCancel = (Button) findViewById(R.id.CUSTOMDIALOGPOST_BUTTON_CANCEL);
            btTake.setOnClickListener(this);
            btChoose.setOnClickListener(this);
            btCancel.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.CUSTOMDIALOGPOST_BUTTON_TAKEPHOTO:
                    try {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(c.getPackageManager()) != null) {
                            imageFilename = "JPEG_" + "itemPic" + "_";
                            File storageDir = Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_PICTURES);
                            File image = File.createTempFile(imageFilename, ".jpg", storageDir);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                            imageFilename = image.getAbsolutePath();
                            c.startActivityForResult(takePictureIntent, TAKE_PHOTO);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.CUSTOMDIALOGPOST_BUTTON_CHOOSEPHOTO:
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    c.startActivityForResult(photoPickerIntent, CHOOSE_PHOTO);
                    break;
                case R.id.CUSTOMDIALOGPOST_BUTTON_CANCEL:
                default:
                    break;
            }
            dismiss();
        }
    }

    private class AddItemsDialogClass extends Dialog implements View.OnClickListener{

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

            if (list_view.hasFocus()){
                if (list_view.getFocusedChild() instanceof RelativeLayout) {
                    if (((RelativeLayout)list_view.getFocusedChild()).getFocusedChild() instanceof
                            EditText) {
                        String enteredString = ((EditText)((RelativeLayout) list_view.getFocusedChild())
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
            for (int i=1;i<count;i++) {
                System.out.println("On dismiss: " + enteredTags.get(i));
                searchingFor += enteredTags.get(i) + ", ";
            }
            tvSearchFor.setText(searchingFor.substring(0,searchingFor.length()-2));
            super.dismiss();
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.add_row) {
                System.out.println("adding row");
                if (list_view.hasFocus()){
                    if (list_view.getFocusedChild() instanceof RelativeLayout) {
                        if (((RelativeLayout)list_view.getFocusedChild()).getFocusedChild() instanceof
                                EditText) {
                            String enteredString = ((EditText)((RelativeLayout) list_view.getFocusedChild())
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
            if (requestCode == CHOOSE_PHOTO) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null,
                        null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();
                imageBitmap = BitmapFactory.decodeFile(filePath);
                System.out.println("base image size in main activity 1: " + imageBitmap.getHeight() + ", " + imageBitmap.getWidth());
                try {
                    int o = resolveBitmapOrientation(new File(filePath));
                    imageBitmap = applyOrientation(imageBitmap, o);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageFile = new File(filePath);

//                width = imageBitmap.getWidth();
//                height = imageBitmap.getHeight();
//
//                scaleWidth = ((float) newWidth) / width;
//                scaleHeight = ((float) newHeight) / height;
//                scale = Math.max(scaleHeight,scaleWidth);
//
//                matrix = new Matrix();
//                matrix.postScale(scale, scale);
//                matrix.postRotate(0);
//
//                //imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, width, height, matrix, true);
//                imageBitmap = Bitmap.createScaledBitmap(imageBitmap, newWidth, newHeight, false);
                Log.e("SIZE", "Width: " + imageBitmap.getWidth() + ", Height: " + imageBitmap
                        .getHeight());
                fs = filePath.split(".jpg");
                newFilePath = fs[0] + "_compressed.jpg";
                System.out.println(newFilePath);
                try {
                    FileOutputStream out = new FileOutputStream(newFilePath);
                    imageFile = new File(newFilePath);
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    System.out.println("base image size in main activity 1: " + imageBitmap.getHeight() + ", " + imageBitmap.getWidth());
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ivBaseImage.setImageBitmap(imageBitmap);
                ivBaseImage.requestLayout();
            } else if (requestCode == TAKE_PHOTO) {
                //Bundle extras = imageReturnedIntent.getExtras();
                //bmp = (Bitmap) extras.get("data");
                try {
                    imageFile = new File(imageFilename);
                    imageBitmap = BitmapFactory.decodeFile(imageFilename);
                    System.out.println("base image size in main activity 1: " + imageBitmap.getHeight() + ", " + imageBitmap.getWidth());
                    int o = resolveBitmapOrientation(new File(imageFilename));
                    imageBitmap = applyOrientation(imageBitmap, o);
//                    width = imageBitmap.getWidth();
//                    height = imageBitmap.getHeight();
//
//                    scaleWidth = ((float) newWidth) / width;
//                    scaleHeight = ((float) newHeight) / height;
//                    scale = Math.min(scaleHeight,scaleWidth);
//
//                    matrix = new Matrix();
//                    matrix.postScale(scale, scale);
//                    matrix.postRotate(0);
//
//                    imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, width, height, matrix, true);
//                    Log.e("SIZE", "Width: " + imageBitmap.getWidth() + ", Height: " + imageBitmap
//                            .getHeight());
//                    imageBitmap = Bitmap.createScaledBitmap(imageBitmap, newWidth, newHeight, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ivBaseImage.setImageBitmap(imageBitmap);
                ivBaseImage.requestLayout();
            } else if (requestCode == CHOOSE_MULTIPLE_PHOTOS) {
                Toast.makeText(getApplicationContext(), "Returned from choosing multiple " +
                        "photos!", Toast.LENGTH_LONG).show();
                Bitmap bmp;
                ClipData cp = data.getClipData();
                if (cp != null) {

                    try {
                        ClipData.Item item = cp.getItemAt(0);
                        bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), item
                                .getUri());
                        //imageBitmap = Bitmap.createScaledBitmap(bmp, 150, 150, false);
                        //ivBaseImage.setImageBitmap(imageBitmap);
                        ivBaseImage.setImageBitmap(bmp);
                        ivBaseImage.requestLayout();
                        Log.e("ClipData Item", item.getUri().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.e("null check", "" + (data.getData() == null));
                    if (data.getData() != null) {
                        try {
                            bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                            //imageBitmap = Bitmap.createScaledBitmap(bmp, 150, 150, false);
                            ivBaseImage.setImageBitmap(imageBitmap);
                            ivBaseImage.requestLayout();
                            Log.e("getData Item", data.getData().toString());
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
                    Toast.LENGTH_LONG).show();;
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

    private void onImageChosen() {
        //zoomer = new ZoomInZoomOut(getApplicationContext(),ivBaseImage);
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

    private void visualMatrix(Matrix mMatrix) {
        float[] vals = new float[9];
        mMatrix.getValues(vals);
        String s = String.format("Matrix Display\n[%f, %f, %f\n%f, %f, %f\n%f, %f, %f]", vals[0],
                vals[1], vals[2], vals[3], vals[4], vals[5], vals[6], vals[7], vals[8]);
        Log.e("Values", s);
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
        public int getCount(){
            return arr.size();
        }

        public ArrayList<String> getItems() {
            return arr;
        }
    }

    class SetupViewHolder {
        EditText etAddItem;
    }


    private class getInstagramInfo extends AsyncTask<String, Void, String[]> {

        public getInstagramInfo() {
        }

        @Override
        protected String[] doInBackground(String... params) {

            String response, photoUrl, profileUrl = "";
            URL url;
            HttpURLConnection urlConnection;

            try {
                url = new URL(String.format("https://api.instagram" +
                                ".com/v1/users/%s/?access_token=%s",
                        INSTAGRAM_USER_ID, INSTAGRAM_ACCESS_TOKEN));
                System.out.println(url.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.connect();
                response = streamToString(urlConnection.getInputStream());
                JSONObject obj = new JSONObject(response);
                profileUrl = obj.getJSONObject("data").getString("profile_picture");
                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                url = new URL(String.format("https://api.instagram" +
                                ".com/v1/users/%s/media/recent/?access_token=%s",
                        INSTAGRAM_USER_ID, INSTAGRAM_ACCESS_TOKEN));
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.connect();
                response = streamToString(urlConnection.getInputStream());
                System.out.println(response);
                JSONArray data = new JSONObject(response).getJSONArray("data");
                JSONObject obj;
                String[] photoUrls = new String[data.length() + 1];
                imageLoader = ImageLoader.getInstance();
                photoUrls[0] = profileUrl;
                for (int i = 0; i < data.length(); i++) {
                    obj = data.getJSONObject(i);
                    photoUrl = obj.getJSONObject("images").getJSONObject("low_resolution")
                            .getString("url");
                    photoUrls[i+1] = photoUrl;
                }
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
                resetRecyclerView();
                gridImageHandler(photoUrls);
            } else {
                Toast.makeText(getApplicationContext(), "Couldn't retrieve anything from " +
                        "instagram. Sorry!", Toast.LENGTH_SHORT).show();
            }

        }
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
            Picasso.with(mContext).load(R.drawable.loading).into(holder.image);

            holder.bmp = mDataSet.get(position);
            holder.image.setImageBitmap(mDataSet.get(position));
            if (selectedPhoto == position) { // should include, show check
                holder.include.setImageResource(R.drawable.check2);
                holder.include.setVisibility(View.VISIBLE);
                holder.image.setAlpha(1f);
            } else { // not in include set, show x
                holder.include.setImageResource(R.drawable.xcheck);
                holder.include.setVisibility(View.GONE);
                holder.image.setAlpha(.7f);
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
