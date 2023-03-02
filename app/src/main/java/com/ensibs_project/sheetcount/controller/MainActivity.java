/**
 * This file contains the class MainActivity which print the image and deal with everything to count the sheets.
 * Copyright © 2023  Collen Leon and Tremaudant Axel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 */

package com.ensibs_project.sheetcount.controller;

import static java.lang.Integer.parseInt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.ensibs_project.sheetcount.R;
import com.ensibs_project.sheetcount.model.FindSheets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Class for the activity to count the sheets
 */
public class MainActivity extends AppCompatActivity {
    private WebView imageViewer;
    private String photoPath;
    private TextView valueCountedText;
    private EditText addedText;
    private TextView totalText;
    private static final int BACK_TAKE_PICTURE = 1;
    private static final int BACK_CHOOSE_PICTURE = 2;
    private static final int SUMMARY_ACTIVITY_REQUEST_CODE = 3;
    private static final String IMAGE_VIEWED = "file:///android_res/drawable/launch_image.jpg";
    private static final String BUNDLE_STATE_COUNT = "BUNDLE_STATE_COUNT";
    private static ArrayList<String> countedList = new ArrayList<>();
    private FindSheets findSheets;

    /**
     * Called at the start of the app after the splashActivity
     * @param savedInstanceState Allow to get data back after destroy
     */
    @SuppressLint({"ClickableViewAccessibility", "MissingInflatedId"})
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findSheets = new FindSheets();
        File photoDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // Create/load a file
        File photoFile = new File(photoDir.toString() + "/sheetCountPlotImage.jpg");
        // Saving of the full path
        photoPath = photoFile.getAbsolutePath();

        // Get the ids
        Button pictureButton = findViewById(R.id.takePictBtn);
        Button galleryButton = findViewById(R.id.galleryBtn);
        imageViewer = findViewById(R.id.imageView);
        valueCountedText = findViewById(R.id.valueCountedTextView);
        Button informationButton = findViewById(R.id.infoBtn);
        addedText = findViewById(R.id.addedEditTextNumberDecimal);
        Button addSheetButton = findViewById(R.id.addSheetBtn);
        Button rmSheetButton = findViewById(R.id.rmSheetBtn);
        totalText = findViewById(R.id.valueTotalTextView);
        Button nextButton = findViewById(R.id.nextBtn);
        Button summaryButton = findViewById(R.id.summaryBtn);

        // Set the buttons their action on click
        pictureButton.setOnClickListener(view -> takePicture());
        galleryButton.setOnClickListener(view -> choosePicture());
        addSheetButton.setOnClickListener(view -> addSheet());
        rmSheetButton.setOnClickListener(view -> removeSheet());
        addedText.setOnEditorActionListener((textView, i, keyEvent) -> modifySheet());
        informationButton.setOnClickListener(view -> info());
        nextButton.setOnClickListener(view -> nextCount(false));
        summaryButton.setOnClickListener(view -> {  // Go to the summary activity
            // Add the value if count != 0
            nextCount(true);
        });

        // Define the properties of the imageViewer
        imageViewer.getSettings().setBuiltInZoomControls(true);
        imageViewer.getSettings().setUseWideViewPort(true);
        imageViewer.getSettings().setDisplayZoomControls(false);
        imageViewer.setBackgroundColor(getResources().getColor(R.color.activity_background));
        imageViewer.getSettings().setAllowFileAccess(true);
        imageViewer.loadUrl(IMAGE_VIEWED);
        imageViewer.setInitialScale(1);

        // To set the permissions
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||  //Check if permissions to photos and the gallery have been granted
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA}, 1); //If not granted request them
        }
    }

    /**
     * Function called to choose a picture in the gallery.
     */
    @SuppressWarnings("deprecation")
    private void choosePicture(){
        addedText.setText("0");
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
     * Get the counted list
     * @return countedList
     */
    public static ArrayList<String> getCountedList(){
        return countedList;
    }

    /**
     * Function to open the camera and take a picture
     */
    @SuppressWarnings("deprecation")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void takePicture(){
        addedText.setText("0");
        // Creation of a window to take a picture
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Creation of the URI
        Uri photoUri = FileProvider.getUriForFile(MainActivity.this,
                MainActivity.this.getApplicationContext().getPackageName() + ".provider", new File(photoPath));

        // Transfer URI to the intent to save the picture in a temp file
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

        // Open the activity
        startActivityForResult(intent, BACK_TAKE_PICTURE);
    }

    /**
     * Get the image and print it
     * @param requestCode return code
     * @param resultCode result code (must be 'ok')
     * @param data the data
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case SUMMARY_ACTIVITY_REQUEST_CODE:  // Back from the summary activity
                    countedList = data.getStringArrayListExtra(SummaryActivity.BUNDLE_STATE_COUNT);  // Get the values of countedList
                    break;

                case BACK_TAKE_PICTURE:  // When a picture has just been taken
                    try {
                        imageViewer.loadUrl("file://" + findSheets.processImage(photoPath));  // Print the image and count the sheets
                        imageViewer.setInitialScale(1);
                        valueCountedText.setText(String.valueOf(findSheets.getCount()));  // Print the number of sheets counted
                        refreshCount();

                    } catch (Exception e) {
                        e.printStackTrace();
                        // Print a popup if an error occurred
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle(getString(R.string.errorImportTitle)).setMessage(getResources().getString(R.string.errorImportText) + e)
                                .setPositiveButton("OK", (dialogInterface, i) -> closeContextMenu()).create().show();
                    }
                    break;

                case BACK_CHOOSE_PICTURE: // When we just have selected a picture
                    backChoosePicture(data);
                    break;
            }
        }
        else countedList = new ArrayList<>();  // If the count is finished
    }

    /**
     * Function to get a copy of a photo from the gallery and plot it on the screen
     * @param data Data about the location of the image
     */
    private void backChoosePicture(Intent data){
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

        try {
            InputStream is;
            OutputStream os;

            is = new FileInputStream(imgDecodableString);
            os = new FileOutputStream(photoPath);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }
            is.close();
            os.close();

            // Define the image in ImageView and count the sheets
            imageViewer.loadUrl("file://" + findSheets.processImage(photoPath));
            imageViewer.setInitialScale(1);
            valueCountedText.setText(String.valueOf(findSheets.getCount()));
            refreshCount();
        } catch(Exception e){
            e.printStackTrace();
            // Print a popup if an error occurred
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.errorImportTitle)).setMessage(getResources().getString(R.string.errorImportText) + e)
                    .setPositiveButton("OK", (dialogInterface, i) -> closeContextMenu()).create().show();
        }
    }

    /**
     * Function to add sheets
     */
    @SuppressLint("SetTextI18n")
    protected void addSheet(){
        addedText.setText(Integer.toString(parseInt(addedText.getText().toString()) + 1));
        refreshCount();
    }

    /**
     * Function to remove sheets
     */
    @SuppressLint("SetTextI18n")
    protected void removeSheet(){
        addedText.setText(Integer.toString(parseInt(addedText.getText().toString()) - 1));
        refreshCount();
    }

    /**
     * Modify the number of sheets from the textEdit
     * @return always true
     */
    protected boolean modifySheet(){
        refreshCount();
        return true;
    }

    /**
     * Function to set the total counted
     */
    @SuppressLint("SetTextI18n")
    protected void refreshCount(){
        totalText.setText(Integer.toString(parseInt(valueCountedText.getText().toString()) +
                parseInt(addedText.getText().toString())));
    }

    /**
     * Create a popup to give information
     */
    protected void info(){
        // Print a popup
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.infoTitle)).setMessage(R.string.infoText)
                .setPositiveButton("OK", (dialogInterface, i) -> closeContextMenu()).create().show();
    }

    /**
     * Save the count and go to next photo
     * @param summary true if we want to get the summary else false
     */
    @SuppressWarnings("deprecation")
    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    protected void nextCount(boolean summary){
        if(!totalText.getText().toString().equals("0")) {
            // Show a popup to set the name of the image
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(R.string.photo_name_title);
            dialog.setMessage(R.string.photo_name_text);

            EditText photoName = new EditText(this);
            photoName.setText(getString(R.string.photo_default_name) + (countedList.size() + 1));
            photoName.selectAll();
            dialog.setView(photoName);

            // Show the keyboard
            final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

            dialog.setPositiveButton(R.string.photo_name_positive_btn, (dialog1, which) -> {
                countedList.add(photoName.getText().toString().replace(getString(R.string.photo_name_separator), " - ") +
                        getString(R.string.photo_name_separator) + totalText.getText().toString());  // Add the value
                closeContextMenu();

                // Reset image
                imageViewer.loadUrl(IMAGE_VIEWED);
                imageViewer.setInitialScale(1);
                // Reset texts
                valueCountedText.setText("0");
                addedText.setText("0");
                totalText.setText("0");

                if(summary){
                    Intent summaryActivityIntent = new Intent(MainActivity.this, SummaryActivity.class);
                    startActivityForResult(summaryActivityIntent, SUMMARY_ACTIVITY_REQUEST_CODE);
                }
            });

            dialog.setNegativeButton(R.string.photo_name_negative_btn, (dialog12, which) -> closeContextMenu());

            dialog.show();
        }
        else {
            // Reset image
            imageViewer.loadUrl(IMAGE_VIEWED);
            imageViewer.setInitialScale(1);
            // Reset texts
            valueCountedText.setText("0");
            addedText.setText("0");
            totalText.setText("0");

            if(summary){
                Intent summaryActivityIntent = new Intent(MainActivity.this, SummaryActivity.class);
                startActivityForResult(summaryActivityIntent, SUMMARY_ACTIVITY_REQUEST_CODE);
            }
        }
    }

    /**
     * Called before the activity is destroyed
     * @param outState Variable to contain the values
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState){
        outState.putStringArrayList(BUNDLE_STATE_COUNT, countedList);
        super.onSaveInstanceState(outState);
    }

    /**
     * Called after the activity has been created
     * @param savedInstanceSate Variable to get the values back
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceSate){
        super.onRestoreInstanceState(savedInstanceSate);
        countedList = savedInstanceSate.getStringArrayList(BUNDLE_STATE_COUNT);
    }
}