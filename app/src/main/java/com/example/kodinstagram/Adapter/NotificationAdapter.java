package com.example.kodinstagram.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kodinstagram.Fragment.PostDetailFragment;
import com.example.kodinstagram.Fragment.ProfileFragment;
import com.example.kodinstagram.Model.Notification;
import com.example.kodinstagram.Model.Post;
import com.example.kodinstagram.Model.User;
import com.example.kodinstagram.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHoler> {

    private static final String TAG = "NotificationAdapter";
    private final Context mContext;
    private final List<Notification> mNotifications;

    public NotificationAdapter(Context mContext, List<Notification> mNotifications) {
        this.mContext = mContext;
        this.mNotifications = mNotifications;
    }

    @NonNull
    @Override
    public ViewHoler onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item , viewGroup , false);
        return new ViewHoler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHoler viewHoler, int position) {

        //Model객체 생성하여 리사이클러뷰 포지션값 얻어오고 진행
        final Notification notification = mNotifications.get(position);
        viewHoler.text.setText(notification.getText());
        getUserInfo(viewHoler.image_profile , viewHoler.username , notification.getUserid());

        /* Notification 이 이미지에 관련된걸수도 있고 , 팔로우 일수도있으니까 제어해준다. */
        if (notification.isIspost()){
            viewHoler.post_image.setVisibility(View.VISIBLE);
            getPostImage(viewHoler.post_image , notification.getPostid());
        }else{
            viewHoler.post_image.setVisibility(View.GONE);
        }

        //각 item click 할떄
        viewHoler.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (notification.isIspost()){
                    /* 포스트에 대한 이벤트 처리 */
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS" , Context.MODE_PRIVATE).edit();
                    editor.putString("postid" , notification.getPostid());
                    editor.apply();

                    ((FragmentActivity)mContext)
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container , new PostDetailFragment())
                            .commit();

                }else{
                    /* 유저에 대한 이벤트 처리  */
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS" , Context.MODE_PRIVATE).edit();
                    editor.putString("profileid" , notification.getUserid());
                    editor.apply();

                    ((FragmentActivity)mContext)
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container , new ProfileFragment())
                            .commit();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNotifications.size();
    }

    public static class ViewHoler extends RecyclerView.ViewHolder{

        public ImageView image_profile , post_image;
        public TextView username , text;

        public ViewHoler(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            username = itemView.findViewById(R.id.username);
            text = itemView.findViewById(R.id.comment);

        }
    }

    //publisherid == getUserId
    /*포스트를 가져오는것과 마찬가지로 파라메터로 publisherid 를 넣고 데이터베이스에 정보요청   */
    private void getUserInfo(final ImageView imageView , final TextView username , String publisherid){

        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(publisherid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                try{
                    assert user != null;
                    Glide.with(mContext)
                            .load(user.getImageurl())
                            .override(80,80)
                            .into(imageView);
                    username.setText(user.getUsername());

                }catch (Exception e ){
                    Log.e(TAG , Objects.requireNonNull(e.getMessage()));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /* postid 를 파라메터로 넣어서 해당 포스트 데이터 모델을 요청해서 글라이드로 이미지 셋팅 */
    private void getPostImage(final ImageView imageView , String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Posts")
                .child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                try{
                    assert post != null;
                    Glide.with(mContext).load(post.getPostimage()).override(80,80).into(imageView);
                }catch (Exception e ){
                    Log.e(TAG , Objects.requireNonNull(e.getMessage()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
