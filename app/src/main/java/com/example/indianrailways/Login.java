package com.example.indianrailways;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class Login extends AppCompatActivity {
    TextInputEditText iD, pass;
    TextInputLayout idEL, passEL;
    CardView login;
    public static final String MyPREFERENCES = "Login";
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        iD = findViewById(R.id.ID1);
        pass = findViewById(R.id.Pass1);
        idEL = findViewById(R.id.ID);
        passEL = findViewById(R.id.Pass);
        login = findViewById(R.id.tkImg);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        String name = sharedpreferences.getString("Name", null);
        if (name != null) {
            startActivity(new Intent(Login.this, MainPage.class));
        }
        login.setOnClickListener(new LoginSuccess());
    }

    private class LoginSuccess implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if(Objects.requireNonNull(iD.getText()).toString().equals("awb") && Objects.requireNonNull(pass.getText()).toString().equals("awb")) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString("Name", iD.getText().toString());
                editor.putString("Password", pass.getText().toString());
                editor.apply();
                Toast.makeText(Login.this, "Successfully Logged-in", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Login.this, MainPage.class));
            }
            if(!iD.getText().toString().equals("awb")){
                Log.d("TAG", "onClick: ");
                idEL.setError("Invalid User ID");
            }
            if(iD.getText().toString().equals("awb")){
                idEL.setError(null);
                idEL.clearFocus();
                passEL.requestFocus();
                pass.setCursorVisible(true);
            }
            if(!Objects.requireNonNull(pass.getText()).toString().equals("awb")){
                passEL.setError("Invalid Password");
            }
            if(pass.getText().toString().equals("awb")){
                passEL.setError(null);
            }
        }
    }
}