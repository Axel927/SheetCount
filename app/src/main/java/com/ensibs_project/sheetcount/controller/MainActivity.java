/**
 * © 2023 Collen Leon and Tremaudant Axel
 *
 * This file contains the class MainActivity which print the image and deal with everything to count the sheets.
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
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
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

/**
 * Class for the activity to count the sheets
 */
public class MainActivity extends AppCompatActivity {
    private ImageView imageViewer;
    private Button pictureButton;
    private String photoPath;
    private Button galleryButton;
    private static final int BACK_TAKE_PICTURE = 1;
    private static final int BACK_CHOOSE_PICTURE = 2;
    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1.0f;

    /**
     * Set the items
     * @param savedInstanceStata description
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceStata){
        super.onCreate(savedInstanceStata);
        setContentView(R.layout.activity_main);

        pictureButton = findViewById(R.id.takePictBtn);
        galleryButton = findViewById(R.id.galleryBtn);

        imageViewer = findViewById(R.id.imageView);

        pictureButton.setOnClickListener(view -> takePicture());
        galleryButton.setOnClickListener(view -> choosePicture());

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        scaleGestureDetector.onTouchEvent(motionEvent);
        return true;
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
            Uri photoUri = FileProvider.getUriForFile(MainActivity.this, MainActivity.this.getApplicationContext().getPackageName()+
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
                        image = BitmapFactory.decodeFile(FindSheets.drawContours(photoPath));
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

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        /**
         * Class to change the scale of the image.
         */
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            scaleFactor *= scaleGestureDetector.getScaleFactor();
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 10.0f));
            imageViewer.setScaleX(scaleFactor);
            imageViewer.setScaleY(scaleFactor);
            return true;
        }
    }
}