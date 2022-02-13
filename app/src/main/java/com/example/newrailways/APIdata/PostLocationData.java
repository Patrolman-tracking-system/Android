package com.example.newrailways.APIdata;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PostLocationData {
    public PostLocationData(Context context,String userID, String lat, String long1, String speed, String timeStamp){
        postDataUsingVolley(context,userID,lat,long1,speed,timeStamp);
    }
    private void postDataUsingVolley(Context context,String userID, String lat, String long1, String speed, String timeStamp) {
        String url = "https://deviation-check-service.herokuapp.com/fetchdata";
//        RequestQueue queue = Volley.newRequestQueue(context);
//        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                NetworkResponse response = error.networkResponse;
//                if (error instanceof ServerError && response != null) {
//                    try {
//                        String res = new String(response.data,
//                                HttpHeaderParser.parseCharset(response.headers, "utf-8"));
//                        // Now you can use any deserializer to make sense of data
//                        JSONObject obj = new JSONObject(res);
//                    } catch (UnsupportedEncodingException e1) {
//                        // Couldn't properly decode data to string
//                        e1.printStackTrace();
//                    } catch (JSONException e2) {
//                        // returned data is not JSONObject?
//                        e2.printStackTrace();
//                    }
//                }
//
//            }
//        }) {
//
//            protected Map<String, String> getParams() {
//                Log.d("TAG", "getParams: MYDATA4");
//                Map<String, String> MyData = new HashMap<String, String>();
//                MyData.put("userID", userID);
//                MyData.put("latitude", lat);
//                MyData.put("longitude", long1);
//                MyData.put("speed", speed);
//                MyData.put("timeStamp", timeStamp);
//                Log.d("TAG", "getParams: MYDATA: "+lat+" "+long1+" "+speed);
//                Log.d("TAG", "getParams: MYDATA"+ Arrays.asList(MyData));
//                Log.d("TAG", "DATA ADDED");
//
//                return MyData;
//            }
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                HashMap<String, String> headers = new HashMap<String, String>();
//                headers.put("Content-Type", "application/json; charset=utf-8");
//                return headers;
//            }
//
//        };
//
//        queue.add(MyStringRequest);
        JSONObject params = new JSONObject();
        try {
            params.put("userID", userID);
            params.put("latitude", lat);
            params.put("longitude", long1);
            params.put("speed", speed);
            params.put("timeStamp", timeStamp);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Response", ""+response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", error.getMessage());

            }
        }) ;
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(request);
    }


}
