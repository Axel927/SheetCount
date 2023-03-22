/**
 * This file contains the class SummaryActivity which print the summary of the count.
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.ensibs_project.sheetcount.BuildConfig;
import com.ensibs_project.sheetcount.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Class de make the summary of the count
 */
public class SummaryActivity extends AppCompatActivity {
    private ListView listView;
    private List<String> list;
    private Button exportButton;
    private TextView settingsTV;
    private Button finishButton;
    private Button backButton;
    private ImageButton settingsBtn;
    private TableLayout settingsTable;
    private EditText csvName;
    private EditText csvPath;
    private EditText emailTo;
    private EditText emailObject;
    private EditText emailContent;
    public static final String BUNDLE_STATE_COUNT = "BUNDLE_STATE_COUNT";
    private static final int DIRECTORY_CODE = 1;

    /**
     * Methode called at the creation of the activity
     * @param savedInstanceState Allow to get data back after destroy
     */
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        // Get the ids
        backButton = findViewById(R.id.backBtn);
        finishButton = findViewById(R.id.finishBtn);
        Button aboutButton = findViewById(R.id.about);
        TextView totalCount = findViewById(R.id.totalCounted);
        listView = findViewById(R.id.photoListView);
        exportButton = findViewById(R.id.exportBtn);
        settingsBtn = findViewById(R.id.settingsBtn);
        settingsTable = findViewById(R.id.settingsTable);
        csvName = findViewById(R.id.csvName);
        Button pathBtn = findViewById(R.id.pathBtn);
        csvPath = findViewById(R.id.editTextPath);
        emailTo = findViewById(R.id.editTextEmail);
        emailObject = findViewById(R.id.objectMail);
        emailContent = findViewById(R.id.mailContent);
        Button resetBtn = findViewById(R.id.resetBtn);
        Button finishSettings = findViewById(R.id.endSettingsBtn);
        settingsTV = findViewById(R.id.settingsTextView);

        settingsBtn.setOnClickListener(view -> settings());
        resetBtn.setOnClickListener(view -> reset());
        finishSettings.setOnClickListener(view -> closeSettings());
        pathBtn.setOnClickListener(view -> setPath());
        setSettingsVisibility(false);

        // Set the action of the buttons on click
        backButton.setOnClickListener(view -> backToMain());
        finishButton.setOnClickListener(view -> finishCount());
        aboutButton.setOnClickListener(view -> about());
        exportButton.setOnClickListener(view -> writeToCSV());

        int totalCounted = 0;
        list = new ArrayList<>();
        // Set the text in each row of the listView
        for(int i = 0 ; i < MainActivity.getCountedList().size() ; i ++){
            list.add(MainActivity.getCountedList().get(i));
            totalCounted += parseInt(MainActivity.getCountedList().get(i).split(getString(R.string.photo_name_separator))[1]);
        }

        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<>(this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, list);
        listView.setAdapter(adapter);  // Set the values in the listView
        listView.setOnItemClickListener((parent, view, position, id) -> countNamed(position));
        listView.setOnItemLongClickListener((parent, view, position, id) -> setName(position));

        totalCount.setText(String.valueOf(totalCounted));  // Print the total counted
        readSettings();
    }

    /**
     * Get the path of the csv file
     * @param requestCode return code
     * @param resultCode result code (must be 'ok')
     * @param data the data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == DIRECTORY_CODE){
            try {
                if (data != null) {
                    String[] pathSections = data.getData().getPath().split(":");
                    String path  = Environment.getExternalStorageDirectory().getPath() + "/" + pathSections[pathSections.length-1];
                    csvPath.setText(path);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Restart the app
     */
    private void finishCount(){
        // Create a popup before restarting the assure the will of the user
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.finishTitle)).setMessage(R.string.finishText)
                .setPositiveButton(R.string.finishPositiveButton, (dialogInterface, i) -> {
                    closeContextMenu();
                    Runtime.getRuntime().exit(0);
                }).setNegativeButton(R.string.finishNegativeButton, (dialogInterface, i) -> closeContextMenu()).create().show();
    }

    /**
     * Go back to main
     */
    private void backToMain(){
        Intent intent = new Intent();
        // Send back the list of count
        intent.putStringArrayListExtra(BUNDLE_STATE_COUNT, new ArrayList<>(list));
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    /**
     * Function called when the about button is pressed
     */
    private void about(){
        // Create a popup to show the about information
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.about_title)).setMessage(getString(R.string.about_text, BuildConfig.VERSION_CODE + "." + BuildConfig.VERSION_NAME,
                        Calendar.getInstance().get(Calendar.YEAR)))
                .setPositiveButton("OK", (dialogInterface, i) -> closeContextMenu()).create().show();
    }

    /**
     * When an item of the list is clicked, a popup appear with the number of sheet counted with the same name
     * @param position Give the position of the item in the list
     */
    private void countNamed(int position){
        // Get the wanted name
        String name = listView.getItemAtPosition(position).toString().split(getString(R.string.photo_name_separator))[0];
        int count = 0;

        for(int i = 0 ; i < listView.getCount() ; i ++) // Find where there is the same name and add the number of sheet
            if(listView.getItemAtPosition(i).toString().split(getString(R.string.photo_name_separator))[0].equals(name))
                count += Integer.parseInt(listView.getItemAtPosition(i).toString().split(getString(R.string.photo_name_separator))[1]);

        // Create a popup to the the number of sheet
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.name_count_title)).setMessage(getString(R.string.name_count_text, count, name))
                .setPositiveButton("OK", (dialogInterface, i) -> closeContextMenu()).create().show();
    }

    /**
     * When an item of the list is long clicked, a popup appear to set the name of the image
     * @param position Give the position of the item in the list
     * @return Always true
     */
    private boolean setName(int position){
        String name = listView.getItemAtPosition(position).toString().split(getString(R.string.photo_name_separator))[0];
        String count = listView.getItemAtPosition(position).toString().split(getString(R.string.photo_name_separator))[1];

        // Show a popup to set the name of the image
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.photo_name_title);
        dialog.setMessage(R.string.photo_name_text);

        EditText photoName = new EditText(this);
        photoName.setText(name);
        photoName.requestFocus();
        photoName.selectAll();
        dialog.setView(photoName);

        // Show the keyboard
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        dialog.setPositiveButton(R.string.photo_name_positive_btn, (dialog1, which) -> {
            list.set(position, photoName.getText().toString().replace(getString(R.string.photo_name_separator), " - ") +
                    getString(R.string.photo_name_separator) + count);
            ArrayAdapter<String> adapter;
            adapter = new ArrayAdapter<>(this,
                    androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, list);
            listView.setAdapter(adapter);  // Set the values in the listView
            closeContextMenu();
        });

        dialog.setNegativeButton(R.string.photo_name_negative_btn, (dialog12, which) -> closeContextMenu());

        dialog.show();
        return true;
    }

    /**
     * Write count to csv file and send it by email.
     */
    @SuppressLint("SimpleDateFormat")
    private void writeToCSV(){
        // Load the data
        String data = getString(R.string.column_name);
        for(int i = 0 ; i < listView.getCount() ; i ++) {
            data = data.concat(listView.getItemAtPosition(i).toString().split(getString(R.string.photo_name_separator))[0]);
            data = data.concat(";");
            data = data.concat(listView.getItemAtPosition(i).toString().split(getString(R.string.photo_name_separator))[1]);
            data = data.concat("\n");
        }

        String time = new SimpleDateFormat("mm:hh:dd-MM-yyy").format(new Date());
        // Create file
        File csvFile = new File(csvPath.getText() + "/" + csvName.getText().toString().replace("{date}", time) + ".csv");
        try{
            FileWriter fw = new FileWriter(csvFile);
            fw.write(data);
            fw.close();
        }
        catch (IOException e){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.export_error_title)).setMessage(getResources().getString(R.string.export_error_text) + e)
                    .setPositiveButton("OK", (dialogInterface, i) -> closeContextMenu()).create().show();
        }

        // Send email
        try {
            time = new SimpleDateFormat("dd/MM/yyy").format(new Date());
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailTo.getText().toString()});
            intent.putExtra(Intent.EXTRA_SUBJECT, emailObject.getText().toString().replace("{date}", time));
            intent.putExtra(Intent.EXTRA_TEXT, emailContent.getText().toString().replace("{date}", time));

            Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", csvFile);
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.setType("*/*");
            this.startActivity(Intent.createChooser(intent, "email"));
        } catch (android.content.ActivityNotFoundException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.email_error_title)).setMessage(getResources().getString(R.string.email_error_text) + e)
                    .setPositiveButton("OK", (dialogInterface, i) -> closeContextMenu()).create().show();
        }
    }

    /**
     * Called when settings button clicked
     */
    private void settings(){
        setSettingsVisibility(true);
    }

    /**
     * Change the visibility of the settings
     * @param visible true make settings visible
     */
    private void setSettingsVisibility(boolean visible){
        settingsBtn.setEnabled(!visible);
        if(visible) {
            settingsTV.setVisibility(View.VISIBLE);
            settingsTable.setVisibility(View.VISIBLE);
            exportButton.setVisibility(View.GONE);
            backButton.setVisibility(View.GONE);
            finishButton.setVisibility(View.GONE);
            listView.setVisibility(View.GONE);
        }
        else {
            settingsTV.setVisibility(View.GONE);
            settingsTable.setVisibility(View.GONE);
            exportButton.setVisibility(View.VISIBLE);
            backButton.setVisibility(View.VISIBLE);
            finishButton.setVisibility(View.VISIBLE);
            listView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Reset the settings values to default values
     */
    private void reset(){
        csvName.setText(getString(R.string.csv_file_name));
        csvPath.setText(getString(R.string.csv_file_path));
        emailTo.setText(getString(R.string.email_address));
        emailObject.setText(getString(R.string.email_object));
        emailContent.setText(getString(R.string.email_text_content));
    }

    /**
     * Close settings and save the settings
     */
    private void closeSettings(){
        File path = new File(csvPath.getText().toString());
        if(path.exists() && path.isDirectory()){  // Verify if the directory of the csv file exists
            setSettingsVisibility(false);
            saveSettings();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.error_path_title)).setMessage(getResources().getString(R.string.error_path_text))
                    .setPositiveButton("OK", (dialogInterface, i) -> closeContextMenu()).create().show();
        }
    }

    /**
     * Load an activity to choose the directory where the csv file will be saved
     */
    @SuppressWarnings("deprecation")
    private void setPath(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("location", csvPath.getText());
        startActivityForResult(Intent.createChooser(intent, "Choose directory"), DIRECTORY_CODE);
    }

    /**
     * Load the settings from the save
     */
    private void readSettings(){
        File saveDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // Create/load a file
        File saveFile = new File(saveDir.toString() + "/settings.sc");
        if(saveFile.exists()) {  // Verify the existence of the file
            try {
                FileReader fr = new FileReader(saveFile);
                BufferedReader br = new BufferedReader(fr);
                StringBuilder sb = new StringBuilder();
                String data;

                while ((data = br.readLine()) != null) {  // Read the data
                    sb.append(data);
                    sb.append("\n");
                }
                fr.close();

                // Dispatch the data
                String[] lines = sb.toString().split("\\$");
                csvName.setText(lines[0]);
                csvPath.setText(lines[1]);
                emailTo.setText(lines[2]);
                emailObject.setText(lines[3]);
                emailContent.setText(lines[4]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Save the settings into a file
     */
    private void saveSettings(){
        File saveDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // Create/load a file
        File saveFile = new File(saveDir.toString() + "/settings.sc");
        String data = csvName.getText() + "$" +
                csvPath.getText() + "$" +
                emailTo.getText() + "$" +
                emailObject.getText() + "$" +
                emailContent.getText() + "$";
        try{
            FileWriter fw = new FileWriter(saveFile);
            fw.write(data); // Write the data
            fw.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}