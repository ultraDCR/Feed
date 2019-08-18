package com.example.feed;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
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
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewPost extends AppCompatActivity {
    public Button post;
    public EditText title, content;
    public CircleImageView pic;
    int PICK_IMAGE_REQUEST = 111;
    String URL = "http://103.1.92.237:8080/feed/post";
    Bitmap bitmap;
    ProgressDialog progressDialog;
    Uri filePath;
    String file,image;
    String token,userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        title = findViewById(R.id.title);
        content = findViewById(R.id.content);
        pic = findViewById(R.id.circleImageView);
        post = findViewById(R.id.post_btn);

        SharedPreferences sharedPreferences;
        sharedPreferences = getSharedPreferences("my",MODE_PRIVATE);
        token = sharedPreferences.getString("token","token");
        userId = sharedPreferences.getString("userid","userId");

        Intent intent = getIntent();
        String postid = intent.getStringExtra("postid");
        if(postid!= null) {
            getdate(postid);
        }

        pic.setOnClickListener(v -> {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                if(ContextCompat.checkSelfPermission(NewPost.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                    Toast.makeText(NewPost.this, "Permission Denied", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(NewPost.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                } else {

                    BringImagePicker();

                }

            } else {

                BringImagePicker();

            }

        });



        Ion.getDefault(this).getConscryptMiddleware().enable(false);
        post.setOnClickListener(v -> {
            //progressDialog = new ProgressDialog(NewPost.this);

            String nTitle = title.getText().toString();
            String nContent = content.getText().toString();
            Log.d("HELLO@", "onActivityResult: " + file+" "+nTitle+" " +nContent);
            if(file == null) {
                if (!nTitle.isEmpty() && !nContent.isEmpty() && image!=null) {
                    updatePost(nTitle, nContent, image,postid);
                }else{
                    Toast.makeText(NewPost.this, "All file required.", Toast.LENGTH_SHORT).show();
                }
            }else{
                if (!nTitle.isEmpty() && !nContent.isEmpty()) {
                    uploadPost(nTitle, nContent, file);
                }else{
                    Toast.makeText(NewPost.this, "All file required.", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    private void updatePost(String nTitle, String nContent, String image, String postid) {
        RequestQueue requestQueue = Volley.newRequestQueue(NewPost.this);
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("title", nTitle);
            jsonBody.put("content", nContent);
            jsonBody.put("image",image);
            String URL1 = URL+"/"+postid;
            JsonObjectRequest jsonOblect = new JsonObjectRequest(Request.Method.PUT, URL1, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    try {
                        JSONObject res = response.getJSONObject("post");
                        Log.i("SinglePost", "onResponse: "+response+"    "+res);
                        Intent main =new Intent(NewPost.this,Home.class);
                        main.putExtra("token",token);
                        main.putExtra("userid",userId);
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void BringImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }


    private void uploadPost(String nTitle, String nContent, String file) {
        Ion.with(NewPost.this)
                .load(URL)
                .progressDialog(progressDialog)
                .setHeader("Authorization","Bearer "+token)
                .setMultipartParameter("title", nTitle)
                .setMultipartParameter("content", nContent)
                .setMultipartFile("image", "image/jpg", new File(file))
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        //do stuff with result
                        Log.d("HELLO", "onCompleted: " + e);
                        Log.d("HELLO1", "onCompleted: " + result);
                        Intent main =new Intent(NewPost.this,Home.class);
                        main.putExtra("token",token);
                        main.putExtra("userid",userId);
                        startActivity(main);
                        finish();

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            file = getPath(filePath);
            Log.d("MAINA", "onActivityResult: " + filePath);
            try {
                //getting image from gallery
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);

                //Setting image to ImageView
                pic.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getPath(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    private void getdate(String postid) {
        RequestQueue requestQueue = Volley.newRequestQueue(NewPost.this);
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
                    image = res.getString("imageUrl");
                    title.setText(s_title);
                    content.setText(s_content);

                    Picasso.with(NewPost.this).load("http://103.1.92.237:8080/"+image).into(pic);

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
