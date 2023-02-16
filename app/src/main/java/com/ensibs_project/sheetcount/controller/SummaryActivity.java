/**
 * © 2023 Collen Leon and Tremaudant Axel
 *
 * This file contains the class SummaryActivity which print the summary of the count.
 */

package com.ensibs_project.sheetcount.controller;

import static java.lang.Integer.parseInt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ensibs_project.sheetcount.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Class de make the summary of the count
 */
public class SummaryActivity extends AppCompatActivity {

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
        TextView totalCount = findViewById(R.id.totalCounted);
        ListView listView = findViewById(R.id.photoListView);

        // Set the action of the buttons on click
        backButton.setOnClickListener(view -> backToMain());
        finishButton.setOnClickListener(view -> finishCount());

        int totalCounted = 0;
        List<String> list = new ArrayList<>();
        // Set the text in each row of the listView
        for(int i = 0 ; i < MainActivity.getCountedList().size() ; i ++){
            list.add("Photo " + (i + 1) + " : " + MainActivity.getCountedList().get(i));
            totalCounted += parseInt(MainActivity.getCountedList().get(i));
        }

        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<>(this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, list);
        listView.setAdapter(adapter);  // Set the values in the listView

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
}