package com.example.feed;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.feed.model.Post;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Home extends AppCompatActivity {

    private List<Post> post_list;
    RecyclerView recyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager layoutManager;
    RequestQueue rq;
    String token,userid;
    String request_url = "http://103.1.92.237:8080/feed/posts";
    Button new_post;
    ImageView logout;
    SwipeRefreshLayout swipeRefreshLayout;
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://103.1.92.237:8080");
            Log.i("SocketRes", "instance initializer: Iam In");
        } catch (URISyntaxException e) {
            Log.i("SocketError", "instance initializer: "+e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent intent = getIntent();
        token = intent.getStringExtra("token");
        userid = intent.getStringExtra("userid");

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        mSocket.on("posts", onNewMessage);
        mSocket.connect();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                swipeRefreshLayout.setRefreshing(true);
                // once the network request has completed successfully.
                sendRequest();
            }
        });
        // Configure the refreshing colors
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);



        Log.i("Token", "onCreate: "+token);
        logout = findViewById(R.id.logout_btn);
        logout.setOnClickListener(v ->{
            SharedPreferences sharedPreferences = getSharedPreferences("my",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("token", "");
            editor.putString("userid","");
            editor.commit();

            Intent main =new Intent(Home.this,MainActivity.class);
            startActivity(main);
            finish();

        });
        new_post = findViewById(R.id.new_post_btn);
        new_post.setOnClickListener(v -> {
            startActivity(new Intent(Home.this,NewPost.class));
        });

        recyclerView = findViewById(R.id.feed_rview);
        rq = Volley.newRequestQueue(this);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        post_list = new ArrayList<>();
        sendRequest();

    }


    public void sendRequest(){
        post_list.clear();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, request_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                JSONArray res = null;
                try {
                    res = response.getJSONArray("posts");
                    Log.i("Volley Error1: ", String.valueOf(res));
                    for(int i = 0; i < res.length(); i++){
                    Post post = new Post();
                    try {
                        JSONObject jsonObject = res.getJSONObject(i);
                        Log.i("Volley Error2: ", String.valueOf(response));

                        post.setTitle(jsonObject.getString("title"));
                        post.setContent(jsonObject.getString("content"));
                        post.setCreator(jsonObject.getJSONObject("creator"));
                        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(jsonObject.getString("createdAt"));
                        Log.i("TIME1", "onResponse: "+date);
                        post.setCreatedAt(date);
                        post.setImageUrl(jsonObject.getString("imageUrl"));
                        post.set_id(jsonObject.getString("_id"));
                        swipeRefreshLayout.setRefreshing(false);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                        post_list.add(post);
                }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mAdapter = new PostRecyclerAdapter(Home.this, post_list,token,userid);
                recyclerView.setAdapter(mAdapter);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("Volley Error: ", String.valueOf(error));
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

        rq.add(jsonObjectRequest);

    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Home.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.i("Socket.io", "run: "+data);
                    String action;
                    try {
                         action = data.getString("action");
                         if(!action.isEmpty()){
                             sendRequest();
                         }
                    } catch (JSONException e) {
                        return;
                    }

                    // add the message to view
                }
            });
        }
    };

}

