package com.example.feed;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

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
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.android.volley.VolleyLog.TAG;

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<Post> post_list;
    private String token,userid;

    public PostRecyclerAdapter(Context context, List post_list,String token,String userid) {
        this.context = context;
        this.post_list = post_list;
        this.token =token;
        this.userid = userid;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_single_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemView.setTag(post_list.get(position));

        Post pu = post_list.get(position);
        JSONObject creatorObj = pu.getCreator();
        String id = "";
        try {
            String name = creatorObj.getString("name");
            id = creatorObj.getString("_id");
            holder.user.setText(name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
        Log.i("TIME", "onBindViewHolder: "+pu.getCreatedAt());
        String date = sdf.format(pu.getCreatedAt());
        holder.pTitle.setText(pu.getTitle());
        holder.created_at.setText(date);

        if(userid.equals(id)){
            holder.edit.setVisibility(View.VISIBLE);
            holder.delete.setVisibility(View.VISIBLE);
        }else{
            holder.edit.setVisibility(View.GONE);
            holder.delete.setVisibility(View.GONE);
        }

        holder.view.setOnClickListener(v-> {
            Intent viewIntent = new Intent(context,SinglePost.class);
            viewIntent.putExtra("token",token);
            viewIntent.putExtra("postid",pu.get_id());
            context.startActivity(viewIntent);

        });

        holder.edit.setOnClickListener(v-> {
            Intent viewIntent = new Intent(context,NewPost.class);
            viewIntent.putExtra("postid",pu.get_id());
            context.startActivity(viewIntent);

        });

        holder.delete.setOnClickListener(v-> {
            holder.deletePost(pu.get_id(),position);

        });



    }

    @Override
    public int getItemCount() {
        return post_list.size();
    }
    public void clear() {
        post_list.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Post> list) {
        post_list.addAll(list);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView pTitle, user, created_at, view, edit, delete;

        public ViewHolder(View itemView) {
            super(itemView);

            pTitle =  itemView.findViewById(R.id.title);
            user =  itemView.findViewById(R.id.username);
            created_at = itemView.findViewById(R.id.created_date);
            view = itemView.findViewById(R.id.view_btn);
            edit = itemView.findViewById(R.id.edit_btn);
            delete = itemView.findViewById(R.id.delete_btn);

        }



        public void deletePost(String id,int  i) {
            RequestQueue requestQueue = Volley.newRequestQueue(context);

            String URL = "http://103.1.92.237:8080/feed/post/" + id;
            //String URL = "http://103.1.92.237:8080/auth/login";

            JsonObjectRequest jsonOblect = new JsonObjectRequest(Request.Method.DELETE, URL, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i("USER", "onResponse: "+response);
                    try {
                        String name = response.getString("message");
//                        post_list.remove(i);
//                        notifyItemRemoved(i);
//                        notifyItemRangeChanged(i,post_list.size());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                        Toast.makeText(context, "Communication Error!", Toast.LENGTH_SHORT).show();

                    } else if (error instanceof AuthFailureError) {
                        Toast.makeText(context, "Authentication Error!", Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ServerError) {
                        Toast.makeText(context, "Server Side Error!", Toast.LENGTH_SHORT).show();
                    } else if (error instanceof NetworkError) {
                        Toast.makeText(context, "Network Error!", Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ParseError) {
                        Toast.makeText(context, "Parse Error!", Toast.LENGTH_SHORT).show();
                    }
                    String body;
                    if(error.networkResponse != null) {
                        try {
                            body = new String(error.networkResponse.data, "UTF-8");
                            JSONObject data = new JSONObject(body);
                            String b = data.get("message").toString();

                            Log.i("NETWORK", "onErrorResponse: "+b+ "  " + body);
                            Toast.makeText(context, "Error: "+b, Toast.LENGTH_SHORT).show();

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
                    headers.put("Content-Type","application/json");//put your token here
                    headers.put("Authorization","Bearer "+token);
                    return headers;
                }
            };
            requestQueue.add(jsonOblect);

        }
    }

}