/**
 * © 2023 Collen Leon and Tremaudant Axel
 *
 * This file contains the class SummaryActivity which print the summary of the count.
 */

package com.ensibs_project.sheetcount.controller;

import static java.lang.Integer.parseInt;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import com.ensibs_project.sheetcount.controller.MainActivity;
import android.widget.TextView;

import com.ensibs_project.sheetcount.R;

import java.util.ArrayList;
import java.util.List;

public class SummaryActivity extends AppCompatActivity {
    private Button backButton, finishButton;
    private TextView totalCount;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        backButton = findViewById(R.id.backBtn);
        finishButton = findViewById(R.id.finishBtn);
        totalCount = findViewById(R.id.totalCounted);
        listView = findViewById(R.id.photoListView);
        backButton.setOnClickListener(view -> backToMain());
        finishButton.setOnClickListener(view -> finishCount());

        int totalCounted = 0;
        List<String> list = new ArrayList<>();
        try{
            for(int i = 0 ; i < MainActivity.getCountedList().size() ; i ++){
                list.add("Photo " + (i + 1) + " : " + MainActivity.getCountedList().get(i));
                totalCounted += parseInt(MainActivity.getCountedList().get(i));
            }

            ArrayAdapter<String> adapter;
            adapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                    list);
            listView.setAdapter(adapter);

            totalCount.setText(String.valueOf(totalCounted));
        }
        catch (NullPointerException e){
            Log.d("axel", "onCreate: error pointer");
        }

    }

    /**
     * Restart the app
     */
    private void finishCount(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.finishTitle)).setMessage(R.string.finishText)
                .setPositiveButton(R.string.finishPositiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        closeContextMenu();
                        Runtime.getRuntime().exit(0);
                    }
                }).setNegativeButton(R.string.finishNegativeButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        closeContextMenu();
                    }
                }).create().show();
    }

    /**
     * Go back to main
     */
    private void backToMain(){
        Intent intent = new Intent(SummaryActivity.this, MainActivity.class);
        startActivity(intent);
    }
}