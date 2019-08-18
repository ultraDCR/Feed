package com.example.feed;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class Signup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        EditText email = findViewById(R.id.email_field);
        EditText password = findViewById(R.id.password_field);
        EditText fullname = findViewById(R.id.full_name);

        Button login = findViewById(R.id.signup_btn);

        login.setOnClickListener(v -> {
            String eaddress = email.getText().toString();
            String pass = password.getText().toString();
            String name = fullname.getText().toString();

            if(!eaddress.isEmpty() && !pass.isEmpty() && !name.isEmpty()){
                Toast.makeText(this, ""+eaddress+" "+pass, Toast.LENGTH_SHORT).show();
                signupSend(eaddress,pass,name);
            }else{
                Toast.makeText(this, "All field required.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void signupSend(String uname, String pass,String name) {
        RequestQueue requestQueue = Volley.newRequestQueue(Signup.this);

        try {
            String URL = "http://103.1.92.237:8080/auth/signup";
            //String URL = "http://103.1.92.237:8080/auth/login";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", uname);
            jsonBody.put("password", pass);
            jsonBody.put("name",name);


            JsonObjectRequest jsonOblect = new JsonObjectRequest(Request.Method.PUT, URL, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i("LOGIN", "onResponse: "+response);
                        Toast.makeText(getApplicationContext(), "Response: ", Toast.LENGTH_SHORT).show();

                        Intent main = new Intent(Signup.this,Login.class);
                        startActivity(main);
                        finish();

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

                            Log.i("NETWORKSignup", "onErrorResponse: "+b+ "  " + body);
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
