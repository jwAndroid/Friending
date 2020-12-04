package com.example.kodinstagram.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kodinstagram.Fragment.PostDetailFragment;
import com.example.kodinstagram.Model.Post;
import com.example.kodinstagram.R;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

public class MyFotoAdapter extends RecyclerView.Adapter<MyFotoAdapter.ViewHoler> {

    private static final String TAG = "MyFotoAdapter";
    private Context mContext;
    private List<Post> mPost;

    public MyFotoAdapter() {
    }

    public MyFotoAdapter(Context mContext, List<Post> mPost) {
        this.mContext = mContext;
        this.mPost = mPost;
    }

    @NonNull
    @Override
    public ViewHoler onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.fotos_item ,viewGroup , false );
        return new MyFotoAdapter.ViewHoler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHoler viewHoler, int position) {

        final Post post = mPost.get(position);

        try {

            Picasso.get().load(post.getPostimage())
                    .resize(350,350)
                    .centerInside()
                    .into(viewHoler.post_image);

        }catch (Exception e){
            Log.d(TAG , Objects.requireNonNull(e.getMessage()));
        }

        viewHoler.post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS" , Context.MODE_PRIVATE).edit();
                editor.putString("postid",post.getPostid());
                editor.apply();

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                        ,new PostDetailFragment()).commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    public class ViewHoler extends RecyclerView.ViewHolder{

        public ImageView post_image;

        public ViewHoler(@NonNull View itemView) {
            super(itemView);

            post_image = itemView.findViewById(R.id.post_image);

        }
    }


}
