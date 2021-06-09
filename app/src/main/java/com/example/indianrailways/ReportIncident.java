package com.example.indianrailways;


import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ReportIncident extends AppCompatActivity {
    CardView tkPicture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_incident);
        tkPicture=findViewById(R.id.tkPicture);
        tkPicture.setOnClickListener(new TakePicture());
    }

    private class TakePicture implements View.OnClickListener {
        @Override
        public void onClick(View v) {
//            startActivity(new Intent(ReportIncident.this,TakePicture.class));
        }
    }
}