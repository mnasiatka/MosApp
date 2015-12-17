package com.finalproject.mosapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    int isCalculating = 0;
    int source = -1;
    Bitmap outputBitmap;
    Worker worker;
    MosaicBuilderOptions options;

    public MosaicBuilder(Context mContext, Double size, Bitmap baseImage, ArrayList
            arrList) {
        System.out.println("Building mosaic");
        this.dirImages = arrList;
        this.HorizontalDirImages = new ArrayList<>();
        this.baseSplit = new ArrayList<>();
        this.mContext = mContext;
        this.baseImage = baseImage;

        System.out.println("base image size in build mosaic: " + baseImage.getHeight() + ", " + baseImage.getWidth());

        nRows = (int) Math.round(size);
        nCols = (int) Math.round(size);
    }

    public MosaicBuilder(Context mContext, Double size, Bitmap baseImage, ArrayList
            arrList, Worker worker) {
        this(mContext,size,baseImage,arrList);
        this.worker = worker;
    }

    public MosaicBuilder(Context mContext, int size, Bitmap baseImage, ArrayList
            arrList, Worker worker) {
        this(mContext,new Double(size),baseImage,arrList);
        this.worker = worker;
    }

    public MosaicBuilder(Context mContext, int size, Bitmap baseImage, ArrayList
            arrList) {
        this(mContext,new Double(size),baseImage,arrList);
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
        baseImage = Bitmap.createScaledBitmap(baseImage, Math.round(baseImage.getWidth() * resizeScale), Math.round(baseImage
                .getHeight() * resizeScale), true);
    }

    public void execute() {
        //loadBaseImage();
        resizeBaseImage();

        sliceBase();
        readImages();
    }

    private void sliceBase() {
        System.out.println("slicing base image");
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

    private void readImages() {
        System.out.println("reading images");
        System.out.println(source);

        compDist();
        System.out.println("finished reading images");
    }

    private void getImagesFromURLs() {
        System.out.println("getting images from URLs");
        dirImages = new ArrayList<>();

        for (String url : photoUrls) {
            new getImageFromURL().execute(url);
        }
    }

    private void compDist() {
        System.out.println("computing distances");
        // calc avg rgb for each directory image
        // [image Index][channel]
        int p, R, G, B, imgWidth, imgHeight, num;
        int index = 0;
        dirImageAverages = new float[dirImages.size()][3];


        for (Bitmap dirImage : dirImages) {
            R = G = B = 0;
            imgWidth = dirImage.getWidth();
            imgHeight = dirImage.getHeight();
            num = imgHeight * imgWidth;
            Log.e("Image", dirImage.toString() + ": " + imgWidth + ", " + imgHeight);
            for (int i = 0; i < imgWidth; i++) {
                for (int j = 0; j < imgHeight; j++) {
                    //Log.e("Dimension", i + ", " + j);
                    p = dirImage.getPixel(i, j);
                    R += (p >> 16) & 0xff;
                    G += (p >> 8) & 0xff;
                    B += p & 0xff;
                }
            }
            dirImageAverages[index][0] = R / (float) num;
            dirImageAverages[index][1] = G / (float) num;
            dirImageAverages[index][2] = B / (float) num;
            index++;
        }
        resultingComparisonDistance = new int[nRows * nCols];
        for (int i = 0; i < baseSplit.size(); i++) {
            //System.out.println("Starting to compare image at " + i);
            new CompareDistances(i).execute();
        }
        //System.out.println("Finished sending comparisons. Waiting for it to finish");

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
        System.out.println("blending images");
        Toast.makeText(mContext, "Called now on index " + isCalculating, Toast.LENGTH_SHORT).show();
        double weight = 1f - (0.51605 * Math.exp(-0.031596 * dirImages.size()));
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

    private String writeFile(Bitmap bmp) {
        File outputDir = mContext.getCacheDir(); // context being the Activity pointer
        String path = "";
        try {
            File outputFile = File.createTempFile(fileName, ".jpg", outputDir);
            path = outputFile.getAbsolutePath();
            System.out.println("output path: " + path);
            OutputStream outStream = null;
            outStream = new FileOutputStream(outputFile);
            bmp.compress(Bitmap.CompressFormat.PNG, 85, outStream);
            outStream.flush();
            outStream.close();
        } catch (IOException e) {
            System.out.println("Error writing file");
        }

        return path;

    }

    private void beforeAfter() {
        /*
        new = np.zeros([output.shape[0], 1+int(output.shape[1]*2), 3])
        new[:, 0:output.shape[1],:] = base[:output.shape[0], :output.shape[1], :]
        new[:, output.shape[1]+1:,:] = output
        return new
         */
    }

    private void onFinishedCalculation() {
        isCalculating++;
        //System.out.println("Finished " + isCalculating);
        if (isCalculating == (nRows * nCols)) {
            stitchImages();
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
            onFinishedCalculation();
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
