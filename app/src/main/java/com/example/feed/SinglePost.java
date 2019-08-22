package com.example.feed;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SinglePost extends AppCompatActivity {
    TextView title, creator, c_date, content;
    ImageView pic;
    String token;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_post);

        requestQueue = VolleySingleton.getInstance(this).getmRequestQueue();

        Intent intent = getIntent();
        token = intent.getStringExtra("token");
        String postid = intent.getStringExtra("postid");

        title = findViewById(R.id.title_single);
        creator = findViewById(R.id.username);
        c_date = findViewById(R.id.created_date);
        content = findViewById(R.id.content);
        pic = findViewById(R.id.image_post);

        getdate(postid);

    }

    private void getdate(String postid) {
            String URL = "http://103.1.92.237:8080/feed/post/"+postid;
            //String URL = "http://103.1.92.237:8080/auth/login";

            JsonObjectRequest jsonOblect = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    try {
                        JSONObject res = response.getJSONObject("post");
                        Log.i("SinglePost", "onResponse: "+response+"    "+res);

                        String s_title = res.getString("title");
                        String s_content = res.getString("content");
                        String s_image = res.getString("imageUrl");
                        JSONObject creatorObj =res.getJSONObject("creator");
                        String id = "";
                        try {
                            String name = creatorObj.getString("name");
                            id = creatorObj.getString("_id");
                            creator.setText(name);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String s_createdat = res.getString("createdAt");
                        Log.i("SinglePost1", "onResponse: "+s_title+"  "+s_content+"  "+s_image);
                        title.setText(s_title);
                        content.setText(s_content);
                        Picasso.with(SinglePost.this).load("http://103.1.92.237:8080/"+s_image).into(pic);

                        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(s_createdat);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
                        String created = sdf.format(date);
                        c_date.setText(created);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
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

            })
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    final Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type","application/json");//put your token here
                    headers.put("Authorization","Bearer "+token);
                    return headers;
                }
            };
            requestQueue.add(jsonOblect);
    }
}
