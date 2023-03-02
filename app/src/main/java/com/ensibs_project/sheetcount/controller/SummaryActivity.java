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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ensibs_project.sheetcount.BuildConfig;
import com.ensibs_project.sheetcount.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Class de make the summary of the count
 */
public class SummaryActivity extends AppCompatActivity {
    private ListView listView;
    private List<String> list;
    public static final String BUNDLE_STATE_COUNT = "BUNDLE_STATE_COUNT";

    /**
     * Methode called at the creation of the activity
     * @param savedInstanceState Allow to get data back after destroy
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        // Get the ids
        Button backButton = findViewById(R.id.backBtn);
        Button finishButton = findViewById(R.id.finishBtn);
        Button aboutButton = findViewById(R.id.about);
        TextView totalCount = findViewById(R.id.totalCounted);
        listView = findViewById(R.id.photoListView);

        // Set the action of the buttons on click
        backButton.setOnClickListener(view -> backToMain());
        finishButton.setOnClickListener(view -> finishCount());
        aboutButton.setOnClickListener(view -> about());

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
        intent.putStringArrayListExtra(BUNDLE_STATE_COUNT, MainActivity.getCountedList());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    /**
     * Called when the activity is destroy
     */
    @Override
    protected void onDestroy(){
        super.onDestroy();
        Intent mainActivityIntent = new Intent(SummaryActivity.this, MainActivity.class);
        startActivity(mainActivityIntent);
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
}