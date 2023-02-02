/**
 * © 2023 Collen Leon and Tremaudant Axel
 *
 * This file contains the class MainActivity which launch the app.
 */

package com.ensibs_project.sheetcount.controller;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.ensibs_project.sheetcount.R;

/**
 * Class for the activity at the start of the app
 */
public class MainActivity extends AppCompatActivity {
    private Button startButton;
    private static final int COUNTING_ACTIVITY_CODE = 41;
    /**
     * Called at the creation of the activity
     * @param savedInstanceState description
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.startButton);
        startButton.setEnabled(true);

        startButton.setOnClickListener(view -> {
            Intent countingActivityIntent = new Intent(MainActivity.this, CountingActivity.class);
            startActivityForResult(countingActivityIntent, COUNTING_ACTIVITY_CODE);
        });
    }


}