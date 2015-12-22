package com.finalproject.mosapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;

public class MosaicBuilder {

    String image_dir = "/storage/emulated/0/DCIM/100_CFV5/";
    String base_dir = "";
    public String fileName = "tempPic";
    public Bitmap baseImage, stitched;
    ArrayList<Bitmap> dirImages, baseSplit, HorizontalDirImages;
    ArrayList<String> photoUrls;
    int[] resultingComparisonDistance;
    float[][] dirImageAverages;
    float resizeScale = 15.0f;
    int cellWidth, cellHeight, cellArea;
    int nRows = 100, nCols = 100;
    int listviewImageSize = 85;
    Context mContext;
    int isCalculatingDistnace = 0;
    int isCalculatingRGB = 0;
    int source = -1;
    Bitmap outputBitmap;
    Worker worker;
    MosaicBuilderOptions options;
    double weight;

    public MosaicBuilder(Context mContext, Double size, Bitmap baseImage, double weight, ArrayList
            arrList) {
        System.out.println("Building mosaic");
        this.dirImages = arrList;
        this.HorizontalDirImages = new ArrayList<>();
        this.baseSplit = new ArrayList<>();
        this.mContext = mContext;
        this.baseImage = baseImage;
        this.weight = weight;

        System.out.println("base image size in build mosaic: " + baseImage.getHeight() + ", " + baseImage.getWidth());

        nRows = (int) Math.round(size);
        nCols = (int) Math.round(size);
    }

    public MosaicBuilder(Context mContext, Double size, Bitmap baseImage, double weight, ArrayList
            arrList , Worker worker) {
        this(mContext,size,baseImage, weight, arrList);
        this.worker = worker;
    }

    public MosaicBuilder(Context mContext, int size, Bitmap baseImage, double weight, ArrayList
            arrList, Worker worker) {
        this(mContext,new Double(size),baseImage, weight, arrList);
        this.worker = worker;
    }

    public MosaicBuilder(Context mContext, int size, Bitmap baseImage, double weight, ArrayList
            arrList) {
        this(mContext,new Double(size),baseImage, weight, arrList);
        this.worker = worker;
    }

    private void getAllFilesOfDir(File directory) {
        //Log.d("Directory", directory.getAbsolutePath());

        System.out.println("getting all files of directory");

        final File[] files = directory.listFiles();
        String[] arr = directory.list();
        for (String s : arr) {
            System.out.println("List: " + s);
        }

        if (files != null) {
            for (File file : files) {
                if (file != null) {
                    if (file.isDirectory()) {  // it is a folder...
                        Log.e("Directory", file.toString());
                        getAllFilesOfDir(file);
                    } else {  // it is a file...
                        //Log.d(TAG, "File: " + file.getAbsolutePath() + "\n");
                    }
                }
            }
        }
    }

    private void loadBaseImage() {
        System.out.println("loading base image");
        BitmapFactory.Options ops = new BitmapFactory.Options();
        ops.inPreferredConfig = Bitmap.Config.RGB_565;
        baseImage = BitmapFactory.decodeFile(base_dir, ops);
        Log.e("Base Image", "" + (baseImage == null));
    }

    private void resizeBaseImage() {
        System.out.println("resizing base image");
        Log.e("Base Image", "" + (baseImage == null));
        if( baseImage.getWidth() * baseImage.getHeight() < 300*300)
            baseImage = Bitmap.createScaledBitmap(baseImage, Math.round(baseImage.getWidth() *
                    resizeScale), Math.round(baseImage
                    .getHeight() * resizeScale), true);
    }

    public void execute() {
        //loadBaseImage();
        //resizeBaseImage();

        sliceBase();
        compDist();
    }

    private void sliceBase() {
        System.out.println("slicing base image");
        Log.e("Size", baseImage.getWidth() + ", " + baseImage.getHeight());
        cellWidth = baseImage.getWidth() / nCols;
        cellHeight = baseImage.getHeight() / nRows;
        cellArea = cellHeight * cellWidth;
        baseSplit = new ArrayList<>();
        int yCoord = 0;
        for (int x = 0; x < nRows; x++) {
            int xCoord = 0;
            for (int y = 0; y < nCols; y++) {
                baseSplit.add(Bitmap.createBitmap(baseImage, xCoord, yCoord, cellWidth, cellHeight));
                xCoord += cellWidth;
            }
            yCoord += cellHeight;
        }
        System.out.println(baseSplit.size());
    }

    private void getImagesFromURLs() {
        System.out.println("getting images from URLs");
        dirImages = new ArrayList<>();

        for (String url : photoUrls) {
            new getImageFromURL().execute(url);
        }
    }

    private void compDist() {
        // calc avg rgb for each directory image
        // [image Index][channel]
        dirImageAverages = new float[dirImages.size()][3];
        for(int i=0;i<dirImages.size();i++) {
            new ComputeDistances(i).execute();
        }
    }

    private void stitchImages() {
        System.out.println("stiching images");
        stitched = Bitmap.createBitmap(baseImage, 0, 0, cellWidth * nCols, cellHeight * nRows);
        int[] pixels;
        int yCoord = 0, imageIndex = -1;
        for (int x = 0; x < nRows; x++) {
            int xCoord = 0;
            for (int y = 0; y < nCols; y++) {
                imageIndex = x * nCols + y;
                //System.out.println("Image Index: " + imageIndex + " ==> " +
                //        resultingComparisonDistance[imageIndex]);

                Bitmap bmp = dirImages.get
                        (resultingComparisonDistance[imageIndex]);
                pixels = new int[bmp.getWidth() * bmp.getHeight()];
                bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
                stitched.setPixels(pixels, 0, bmp.getWidth(), xCoord, yCoord, cellWidth,
                        cellHeight);
                xCoord += cellWidth;
            }
            yCoord += cellHeight;
            int percentDone = (int) (x / (float) nRows * 100.0);
            System.out.println("Percent done: " + percentDone + "%");
        }
        if (options == null || options.doBlend) {
            blend();
        } else {
            onFinishedBuilding();

        }
    }

    private void onFinishedBuilding() {
        Log.e("Finished", "finished building.");
        worker.onEvent();
    }

    public Bitmap getStitched() {
        return stitched;
    }

    public void setOptions(MosaicBuilderOptions options) {
        this.options = options;
    }

    /**
     * TODO: Last method called
     * Robustified optimal weighting algorithm introduced by Nasiatka, Nasiatka et al. 2015
     */
    private void blend() {
        double weight = this.weight;
        System.out.println("blending weight: " +weight);
        System.out.println("blending images");
        //System.out.println(baseImage == null);
        //System.out.println(stitched == null);
        baseImage = Bitmap.createBitmap(baseImage, 0, 0, stitched.getWidth(), stitched.getHeight());
        int stitchedRGB, baseRGB, pixel = 0;
        int R, G, B;
        int num = stitched.getWidth() * stitched.getHeight();
        for (int i = 0; i < stitched.getWidth(); i++) {
            Log.e("Progress", (i * 100.0 / stitched.getWidth()) + "% done");
            for (int j = 0; j < stitched.getHeight(); j++) {
                stitchedRGB = stitched.getPixel(i, j);
                baseRGB = baseImage.getPixel(i, j);
                R = ((int) Math.round(((stitchedRGB >> 16) & 0xff) * weight) + (int) Math.round((
                        (baseRGB >> 16) & 0xff) * (1 - weight))) & 0xff;
                G = ((int) Math.round(((stitchedRGB >> 8) & 0xff) * weight) + (int) Math.round((
                        (baseRGB >> 8) & 0xff) * (1 - weight))) & 0xff;
                B = ((int) Math.round((stitchedRGB & 0xff) * weight) + (int) Math.round((
                        baseRGB & 0xff) * (1 - weight))) & 0xff;
                pixel = (R << 16) | (G << 8) | B;

                //System.out.println(R +"," + G +"," +B+","+pixel);

                stitched.setPixel(i, j, pixel);
            }
        }
        System.out.println(stitched.getWidth() + " " + stitched.getHeight());
        Log.e("Finished", "Finished image!");
        //String path = writeFile(stitched);
        //System.out.println("returned output path: "+ path);
        //MainActivity3.imageView.setImage(ImageSource.bitmap(stitched));
        onFinishedBuilding();

    }

    private void onFinishedDistanceComparison() {
        isCalculatingDistnace++;
        //System.out.println("Finished " + isCalculating);
        if (isCalculatingDistnace == (nRows * nCols)) {
            stitchImages();
        }
    }

    private void onFinishedRGBComputation() {
        isCalculatingRGB++;
        //System.out.println("Finished " + isCalculating);
        if (isCalculatingRGB == (dirImages.size())) {
            resultingComparisonDistance = new int[nRows * nCols];
            for (int i = 0; i < baseSplit.size(); i++) {
                new CompareDistances(i).execute();
            }
        }
    }

    private class ComputeDistances extends AsyncTask<Integer, Void, Void> {

        int index;

        // calc avg rgb for each directory image
        // [image Index][channel]
        public ComputeDistances(int index) {
            this.index = index;
        }

        @Override
        protected Void doInBackground(Integer... params) {
            int p, R, G, B, imgWidth, imgHeight, num;
            R = G = B = 0;
            Bitmap dirImage;

            dirImage = dirImages.get(index);
            dirImage = Bitmap.createScaledBitmap(dirImage, cellWidth,cellHeight,true);
            dirImages.set(index, dirImage);
            imgWidth = dirImage.getWidth();
            imgHeight = dirImage.getHeight();
            num = imgHeight * imgWidth;
            //Log.e("Image", dirImage.toString() + ": " + imgWidth + ", " + imgHeight);
            for (int col = 0; col < imgWidth; col++) {
                for (int row = 0; row < imgHeight; row++) {
                    p = dirImage.getPixel(col, row);
                    R += (p >> 16) & 0xff;
                    G += (p >> 8) & 0xff;
                    B += p & 0xff;
                }
            }
            dirImageAverages[index][0] = R / (float) num;
            dirImageAverages[index][1] = G / (float) num;
            dirImageAverages[index][2] = B / (float) num;
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            onFinishedRGBComputation();
        }
    }

    private class CompareDistances extends AsyncTask<Integer, Void, Integer> {

        int imageIndex;

        // for this image go through 30 directory images
        public CompareDistances(int imageIndex) {
            this.imageIndex = imageIndex;
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            Bitmap thisCell = baseSplit.get(imageIndex);
            int R, G, B, p, smallestIndex = -1;
            float Ravg, Gavg, Bavg;
            R = G = B = 0;
            for (int i = 0; i < cellWidth; i++) {
                for (int j = 0; j < cellHeight; j++) {
                    p = thisCell.getPixel(i, j);
                    R += (p >> 16) & 0xff;
                    G += (p >> 8) & 0xff;
                    B += p & 0xff;
                }
            }
            Ravg = R / (float) cellArea;
            Gavg = G / (float) cellArea;
            Bavg = B / (float) cellArea;

            // for each image
            double thisDistance = Double.MAX_VALUE;
            double minDistance = thisDistance;

            for (int i = 0; i < dirImageAverages.length; i++) {
                thisDistance = Math.pow(dirImageAverages[i][0] - Ravg, 2) + Math.pow
                        (dirImageAverages[i][1] -
                                Gavg, 2) + Math.pow(dirImageAverages[i][2] - Bavg, 2);
                if (thisDistance < minDistance) {
                    minDistance = thisDistance;
                    smallestIndex = i;
                }
            }

            return smallestIndex;
        }

        @Override
        protected void onPostExecute(Integer smallestDistanceIndex) {
            resultingComparisonDistance[imageIndex] = smallestDistanceIndex;
            //System.out.println("Just finished index " + imageIndex);
            onFinishedDistanceComparison();
        }
    }

    private class getImageFromURL extends AsyncTask<String, Void, Bitmap> {

        public getImageFromURL() {
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                String src = params[0];
                java.net.URL url = new java.net.URL(src);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bmp = BitmapFactory.decodeStream(input);

                int width = bmp.getWidth();
                int height = bmp.getHeight();
                float scaleWidth = ((float) cellWidth) / width;
                float scaleHeight = ((float) cellHeight) / height;
                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleHeight);
                return Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, false);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bmp) {
            dirImages.add(bmp);
            if (dirImages.size() == photoUrls.size()) {
                compDist();
            }
        }
    }

}
