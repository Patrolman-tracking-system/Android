package com.example.indianrailways;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

public class MainPage extends AppCompatActivity {
    CardView report,track;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        report=findViewById(R.id.reportIncident);
        track=findViewById(R.id.track);
        track.setOnClickListener(new Track());
        report.setOnClickListener(new Report());
    }

    private class Report implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(MainPage.this, com.example.indianrailways.ReportIncident.class));
        }
    }

    private class Track implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(MainPage.this, com.example.indianrailways.Tracking.class));
        }
    }
}