/**
 * © 2023 Nom Prenom
 * email
 *
 * This file contains the class CountingActivity which print the image.
 */

package com.ensibs_project.sheetcount.controller;

import android.annotation.SuppressLint;
import android.content.Intent;
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

/**
 * Class for the activity to count the sheets
 */
public class CountingActivity extends AppCompatActivity {
    private ImageView imageViewer;
    private Button pictureButton;
    private String photoPath;
    private static final int BACK_TAKE_PICTURE = 1;

    /**
     * Set the items
     * @param savedInstanceStata description
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceStata){
        super.onCreate(savedInstanceStata);
        setContentView(R.layout.activity_counting);

        pictureButton = findViewById(R.id.button);
        pictureButton.setEnabled(true);
        imageViewer = findViewById(R.id.imageView);

        pictureButton.setOnClickListener(view -> takePicture());
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        // Verify the right return code and the state of the return to be ok
        if(requestCode == BACK_TAKE_PICTURE && resultCode == RESULT_OK){
            // Getting of the image
            Bitmap image = BitmapFactory.decodeFile(photoPath);

            // Print the image
            imageViewer.setImageBitmap(image);
        }
    }
}
