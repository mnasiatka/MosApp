package com.finalproject.mosapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;


public class ImageChooserActivity extends ActionBarActivity {

    ArrayList<String> imageUrls;
    ViewGroup viewGroup;
    GridView gridView;
    CustomListAdapter mGridAdapter;
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_image_chooser);
        //gridView = (GridView) findViewById(R.id.grid_view);
       // mGridAdapter = new CustomListAdapter(getApplicationContext(), imageUrls);
        viewGroup = (ViewGroup) findViewById(R.id.ll);

        getImages();
    }

    private void getImages() {
        String s = Environment.getExternalStorageDirectory().getAbsolutePath();
        getAllFilesOfDir(new File(Environment.getExternalStorageDirectory() + "/Pictures"));
        getAllFilesOfDir(new File("/storage/external_SD"));
    }

    private void getAllFilesOfDir(File directory) {
        //Log.d("Directory", directory.getAbsolutePath());
        final File[] files = directory.listFiles();
        String[] arr = directory.list();
        if ( files != null ) {
            for ( File file : files ) {
                if ( file != null ) {
                    if ( file.isDirectory() ) {  // it is a folder...
                        Log.e("Directory", file.toString());
                        //getAllFilesOfDir(file);
                    }
                    else {  // it is a file...
                        Log.d("File",file.getAbsolutePath());
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_chooser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class CustomListAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final ArrayList<String> fileNames;
        private ArrayList<Bitmap> bmpImages;
        private ArrayList<String> tags;

        public CustomListAdapter(Context context, ArrayList<String> fileNames) {
            super(context, R.layout.grid_item, fileNames.size());
            this.context = context;
            this.fileNames = fileNames;

            bmpImages = new ArrayList<>(fileNames.size());
            tags = new ArrayList<>(fileNames.size());

            BitmapFactory.Options ops = new BitmapFactory.Options();
            ops.inPreferredConfig = Bitmap.Config.RGB_565;
            for (String fileName : fileNames) {
                System.out.println("File name: "  + fileName);
                Bitmap bmp = BitmapFactory.decodeFile(fileName, ops);
                if (bmp != null) {
                    bmp = Bitmap.createScaledBitmap(bmp, Math.round(bmp.getWidth() * .25f), Math.round
                            (bmp.getHeight() * .25f), false);
                    bmpImages.add(bmp);
                    tags.add(fileName);
                } else {
                    Log.e("Bitmpa loading error", "Something happened couldnt load image");
                }
            }
        }

        @Override
        public View getView(final int position, View rowView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final SetupViewHolder holder;
            if (rowView == null) {
                rowView = inflater.inflate(R.layout.grid_item, parent, false);
                holder = new SetupViewHolder();
                holder.text = (TextView) rowView.findViewById(R.id.text);
                holder.image = (ImageView) rowView.findViewById(R.id.image);
                rowView.setTag(holder);
            } else {
                holder = (SetupViewHolder) rowView.getTag();
            }
            holder.text.setText("Placeholder");
            holder.image.setImageBitmap(bmpImages.get(position));
            //final View rView = rowView;
            return rowView;
        }

        @Override
        public int getCount(){
            return bmpImages.size();
        }
    }

    class SetupViewHolder {
        ImageView image;
        TextView text;
    }

}
