package com.example.newrailways.APIdata;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.ContentHandler;

public class GetUsers {
    RequestQueue requestQueue;
    public GetUsers(Context context){
        requestQueue = Volley.newRequestQueue(context);
        getData();
        Log.d("TAG", "getData: 3");
    }
    void getData(){
        Log.d("TAG", "getData: 1");
//        String userURL="https://localhost:9000/profile?id=620646f20ca158c8d4e618c3";
        String userURL="https://api.sampleapis.com/wines/reds";
        StringRequest myRequest = new StringRequest(Request.Method.GET, userURL,
                response -> {
                    try{
                        Log.d("TAG", "getData: 2");
                        //Create a JSON object containing information from the API.
                        JSONObject myJsonObject = new JSONObject(response);
//                        Log.d("TAG", "getData: Name: "+myJsonObject.getString("name"));
//                        Log.d("TAG", "getData: DOB: "+myJsonObject.getString("dob"));
                        Log.d("TAG", "getData: Name: "+myJsonObject.getString("winery"));
                        Log.d("TAG", "getData: DOB: "+myJsonObject.getString("wine"));
                    } catch (JSONException e) {
                        Log.d("TAG", "getData: ERROR "+e);
                        e.printStackTrace();

                    }
                },
                volleyError -> Log.d("TAG", "getData: ERROR: "+volleyError.getMessage())
        );
        requestQueue.add(myRequest);
    }
}


