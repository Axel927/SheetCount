/**
 * © 2023 Nom Prenom
 * email
 *
 * This file contains the class CountingActivity which print the image.
 */

package com.ensibs_project.sheetcount.controller;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.ensibs_project.sheetcount.R;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.ensibs_project.sheetcount.model.FindSheets;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * Class for the activity to count the sheets
 */
public class CountingActivity extends AppCompatActivity {
    private ImageView imageViewer;
    private Button pictureButton;
    private String photoPath;
    private Button galleryButton;
    private static final int BACK_TAKE_PICTURE = 1;
    private static final int BACK_CHOOSE_PICTURE = 2;

    /**
     * Set the items
     * @param savedInstanceStata description
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceStata){
        super.onCreate(savedInstanceStata);
        setContentView(R.layout.activity_counting);

        pictureButton = findViewById(R.id.takePictBtn);
        galleryButton = findViewById(R.id.galleryBtn);

        imageViewer = findViewById(R.id.imageView);

        pictureButton.setOnClickListener(view -> takePicture());
        galleryButton.setOnClickListener(view -> choosePicture());
    }

    /**
     * Function called to choose a picture in the gallery.
     * NOTA: SheetCount must be allowed in 'Files and media permission' in 'Settings' of the phone.
     */
    private void choosePicture(){
        Intent intent = new Intent(Intent.ACTION_PICK);

        // Define thee type as image/*.
        // This guarantee than just image type components are selected
        intent.setType("image/*");
        // We give an array with MIME types accepted
        // This guarantee than just MIME type components are targeted
        String[] mimeTypes = {"image/jpeg", "image/png","image/jpg"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        startActivityForResult(intent, BACK_CHOOSE_PICTURE);
    }

    /**
     * Function to open the camera and take a picture
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void takePicture(){
        // Creation of a window to take a picture
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Creation of an unique filename
        @SuppressLint("SimpleDateFormat") String time = new SimpleDateFormat("yyyMMdd_hhmmss").format(new Date());
        File photoDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {

            File photoFile = File.createTempFile("photo" + time, ".jpg", photoDir);

            // Saving of the full path
            photoPath = photoFile.getAbsolutePath();

            // Creation of the URI
            Uri photoUri = FileProvider.getUriForFile(CountingActivity.this, CountingActivity.this.getApplicationContext().getPackageName()+
                    ".provider", photoFile);

            // Transfer URI to the intent to save the picture in a temp file
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

            // Open the activity
            startActivityForResult(intent, BACK_TAKE_PICTURE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the image and print it
     * @param requestCode return code
     * @param resultCode result code (must be ok)
     * @param data the data
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case BACK_TAKE_PICTURE:
                    // When a picture has just been taken

                    // Getting of the image

                    Bitmap image = null;
                    try {

                        Mat src = Imgcodecs.imread(photoPath);
                        //Converting the image to Gray
                        Mat gray = new Mat();
                        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGBA2GRAY);
                        //Detecting the edges
                        Mat edges = new Mat();
                        Imgproc.Canny(gray, edges, 60, 60*3, 3, false);
                        // Changing the color of the canny
                        Mat cannyColor = new Mat();
                        Imgproc.cvtColor(edges, cannyColor, Imgproc.COLOR_GRAY2BGR);
                        //Detecting the hough lines from (canny)
                        Mat lines = new Mat();
                        Imgproc.HoughLines(edges, lines, 1, Math.PI/180, 150);
                        for (int i = 0; i < lines.rows(); i++) {
                            double[] data1 = lines.get(i, 0);
                            double rho = data1[0];
                            double theta = data1[1];
                            double a = Math.cos(theta);
                            double b = Math.sin(theta);
                            double x0 = a*rho;
                            double y0 = b*rho;
                            //Drawing lines on the image
                            Point pt1 = new Point();
                            Point pt2 = new Point();
                            pt1.x = Math.round(x0 + 1000*(-b));
                            pt1.y = Math.round(y0 + 1000*(a));
                            pt2.x = Math.round(x0 - 1000*(-b));
                            pt2.y = Math.round(y0 - 1000 *(a));
                            Imgproc.line(cannyColor, pt1, pt2, new Scalar(0, 0, 255), 3);
                        }
                        Utils.matToBitmap(cannyColor, image);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Print the image
                    imageViewer.setImageBitmap(image);
                    break;

                case BACK_CHOOSE_PICTURE:
                    // When we just have selected a picture

                    Uri selectedImage = data.getData();
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };
                    // Get the cursor
                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    // Move to first line
                    cursor.moveToFirst();
                    // Get the column index of MediaStore.Images.Media.DATA
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    // Get the string value in the column
                    String imgDecodableString = cursor.getString(columnIndex);
                    cursor.close();

                    // Creation of an unique filename
                    @SuppressLint("SimpleDateFormat") String time = new SimpleDateFormat("yyyMMdd_hhmmss").format(new Date());
                    File photoDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    try {
                        File photoFile = File.createTempFile("photo" + time, ".jpg", photoDir);
                        // Saving of the full path
                        photoPath = photoFile.getAbsolutePath();

                        InputStream is;
                        OutputStream os;

                        try {
                            is = new FileInputStream(imgDecodableString);
                            os = new FileOutputStream(photoFile);
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = is.read(buffer)) > 0) {
                                os.write(buffer, 0, len);
                            }
                            is.close();
                            os.close();
                        }
                        catch(IOException e){
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }



                    // Define the image in ImageView after decoding of the string
                    //imageViewer.setImageBitmap(BitmapFactory.decodeFile(imgDecodableString));
                    try {
                        imageViewer.setImageBitmap(BitmapFactory.decodeFile(FindSheets.drawContours(photoPath)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }
}
