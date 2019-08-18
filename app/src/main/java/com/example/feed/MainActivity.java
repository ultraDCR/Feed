package com.example.feed;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.feed.model.Post;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button login = findViewById(R.id.login_button);
        Button signup = findViewById(R.id.create_acc_btn);

        login.setOnClickListener(v->{
            startActivity(new Intent(MainActivity.this,Login.class));
        });

        signup.setOnClickListener(v->{
            startActivity(new Intent(MainActivity.this,Signup.class));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedPreferences;
        sharedPreferences = getSharedPreferences("my",MODE_PRIVATE);
        Log.i("DATA", "loginSend: "+sharedPreferences.getString("token",""));
        String tok = sharedPreferences.getString("token","");

        if(!tok.isEmpty()){
            checkauth(tok);
        }

    }

    private void checkauth(String token) {
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        try {
            String URL = "http://103.1.92.237:8080/feed/checkauth";


            JsonObjectRequest jsonOblect = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    try {
                        String id = response.getString("userId");
                        Intent main = new Intent(MainActivity.this, Home.class);
                        main.putExtra("token", token);
                        main.putExtra("userid", id);
                        startActivity(main);
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                        Toast.makeText(getApplicationContext(), "Communication Error!", Toast.LENGTH_SHORT).show();

                    } else if (error instanceof AuthFailureError) {
                        Toast.makeText(getApplicationContext(), "Authentication Error!", Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ServerError) {
                        Toast.makeText(getApplicationContext(), "Server Side Error!", Toast.LENGTH_SHORT).show();
                    } else if (error instanceof NetworkError) {
                        Toast.makeText(getApplicationContext(), "Network Error!", Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ParseError) {
                        Toast.makeText(getApplicationContext(), "Parse Error!", Toast.LENGTH_SHORT).show();
                    }
                    String body;
                    if(error.networkResponse != null) {
                        try {
                            body = new String(error.networkResponse.data, "UTF-8");
                            JSONObject data = new JSONObject(body);
                            String b = data.get("message").toString();

                            Log.i("AUTHCHECK", "onErrorResponse: " + b);
                            Toast.makeText(getApplicationContext(), "Error: "+b, Toast.LENGTH_SHORT).show();

                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    final Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");//put your token here
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };
            requestQueue.add(jsonOblect);

        } catch (Exception e) {
            Log.i("AUTHCHECK11", "onErrorResponse: " +e);
        }
    }
}
