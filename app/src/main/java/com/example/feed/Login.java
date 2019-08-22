package com.example.feed;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class Login extends AppCompatActivity {
private  RequestQueue requestQueue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        requestQueue = VolleySingleton.getInstance(this).getmRequestQueue();

        EditText email = findViewById(R.id.email_field_login);
        EditText password = findViewById(R.id.password_login);

        Button login = findViewById(R.id.login_btn);

        login.setOnClickListener(v -> {
            String eaddress = email.getText().toString();
            String pass = password.getText().toString();

            if(!eaddress.isEmpty() || !pass.isEmpty()){
                loginSend(eaddress,pass);
            }else{
                Toast.makeText(this, "All field required.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loginSend(String uname, String pass) {
        try {
            String URL = "http://103.1.92.237:8080/auth/login";
            //String URL = "http://103.1.92.237:8080/auth/login";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", uname);
            jsonBody.put("password", pass);


            JsonObjectRequest jsonOblect = new JsonObjectRequest(Request.Method.POST, URL, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i("LOGIN", "onResponse: "+response);

                    try {
                        String token = response.getString("token");
                        String id = response.getString("userId");
                        Log.i("LOGIN", "onResponse: "+response);
                        SharedPreferences sharedPreferences = getSharedPreferences("my",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("token", token);
                        editor.putString("userid",id);
                        editor.commit();
                        Intent main = new Intent(Login.this,Home.class);
                        main.putExtra("token",token);
                        main.putExtra("userid",id);
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

                            Log.i("NETWORK", "onErrorResponse: "+b+ "  " + body);
                            Toast.makeText(getApplicationContext(), "Error: "+b, Toast.LENGTH_SHORT).show();

                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }

            });
//            {
//                @Override
//                public Map<String, String> getHeaders() throws AuthFailureError {
//                    final Map<String, String> headers = new HashMap<>();
//                    headers.put("Content-Type","applica");//put your token here
//                    return headers;
//                }
//            };
            requestQueue.add(jsonOblect);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "No server running" + e, Toast.LENGTH_SHORT).show();
        }

    }
}
