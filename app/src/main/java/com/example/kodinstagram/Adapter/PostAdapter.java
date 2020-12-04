package com.example.kodinstagram.Adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.kodinstagram.CommentsActivity;
import com.example.kodinstagram.FollowersActivity;
import com.example.kodinstagram.Fragment.PostDetailFragment;
import com.example.kodinstagram.Fragment.ProfileFragment;
import com.example.kodinstagram.Model.Post;
import com.example.kodinstagram.Model.User;
import com.example.kodinstagram.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHoler> {

    private static final String TAG = "PostAdapter" ;
    public Context mContext;
    public List<Post> mPost;
    private FirebaseUser firebaseUser;

    AnimatedVectorDrawableCompat avd;
    AnimatedVectorDrawable avd2;

    //TODO : DOUBLE CLICK
    int i = 0;

    public PostAdapter() { }
    public PostAdapter(Context mContext, List<Post> mPost) {
        this.mContext = mContext;
        this.mPost = mPost;
    }

    @NonNull
    @Override
    public ViewHoler onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item , viewGroup , false);
        return new PostAdapter.ViewHoler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHoler viewHoler, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final Post post = mPost.get(position);
        try {
            Glide.with(mContext).load(post.getPostimage())
                    .override(800,800)
                    .apply(new RequestOptions().placeholder(R.drawable.placeholder2))
                    .into(viewHoler.post_image);
        }catch (Exception e){
            Log.d(TAG , Objects.requireNonNull(e.getMessage()));
        }

        try{
            if (post.getDescription().equals("")){
                viewHoler.description.setVisibility(View.GONE);

            }else{
                viewHoler.description.setVisibility(View.VISIBLE);
                viewHoler.description.setText(post.getDescription());
            }


        }catch (Exception e){
            Log.d(TAG , e.getMessage());
        }


        //home setting
        try{

            publisherInfo(viewHoler.image_profile , viewHoler.username , viewHoler.publisher , post.getPublisher());
            isLiked(post.getPostid() , viewHoler.like);
            nrLikes(viewHoler.likes , post.getPostid());
            getComments(post.getPostid() , viewHoler.comments);
            isSaved(post.getPostid() , viewHoler.save);

        }catch (Exception e){
            Log.d(TAG ,  e.getMessage());
        }


        viewHoler.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS" , Context.MODE_PRIVATE).edit();
                //getPublisher id 저장
                editor.putString("profileid",post.getPublisher());
                editor.apply();

                //profile frgment to move
                //((FragmentActivity)mContext) 프래그먼트식으로 돌려야함 ..
                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                ,new ProfileFragment()).commit();
            }
        });

        viewHoler.publisher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS" , Context.MODE_PRIVATE).edit();

                editor.putString("profileid",post.getPublisher());
                editor.apply();

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                        ,new ProfileFragment()).commit();
            }
        });

        viewHoler.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS" , Context.MODE_PRIVATE).edit();
                editor.putString("profileid",post.getPublisher());
                editor.apply();

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                        ,new ProfileFragment()).commit();
            }
        });

        //.... 여기까지 username , publisher , image_profile 모두 동일하게 SharedPreferences 저장 -- > fragment 로 move
        //post_image를 눌렀을때 PostDetailFragment로 그리고 postid 를 저장
        
//        viewHoler.post_image.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS" , Context.MODE_PRIVATE).edit();
//                editor.putString("postid",post.getPostid());
//                editor.apply();
//
//                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
//                        ,new PostDetailFragment()).commit();
//            }
//        });

        //TODO : Post_item 쪽 post_image 에서 imageView 하나 장착해놓고
        //TODO : 스레드가아닌 애니메이션을 넣은거같다 . 우선 LIKE 버튼처리다시공부하고 적용할것.
        final Drawable drawable = viewHoler.heart.getDrawable();
        viewHoler.post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                i++;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (i == 1){
                            //TODO : ONE Click POST DETAIL FRAGMENT ( DELETE )

                        }else if (i == 2){
                            //TODO : Two Click POST LIKE BUTTON ANIMATION
                            viewHoler.heart.setAlpha(0.70f);
                            if (drawable instanceof AnimatedVectorDrawableCompat){
                                avd = (AnimatedVectorDrawableCompat) drawable;
                                avd.start();

                            }else if(drawable instanceof AnimatedVectorDrawable){
                                avd2 = (AnimatedVectorDrawable) drawable;
                                avd2.start();
                            }

                            if (viewHoler.like.getTag().equals("like")){

                                FirebaseDatabase.getInstance().getReference()
                                        .child("Likes")
                                        .child(post.getPostid())
                                        .child(firebaseUser.getUid())
                                        .setValue(true);
                                //like를 눌렀을떄 notification add! -- > 게시
                                addNotification(post.getPublisher() , post.getPostid());

                            }else{

                                FirebaseDatabase.getInstance().getReference()
                                        .child("Likes")
                                        .child(post.getPostid())
                                        .child(firebaseUser.getUid())
                                        .removeValue();
                            }
                        }
                        i = 0;
                    }
                }, 500);

            }
        });

        //save.setOnClickListener DB noid crate
        viewHoler.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewHoler.save.getTag().equals("save")){

                    FirebaseDatabase.getInstance().getReference()
                            .child("Saves")
                            .child(firebaseUser.getUid())
                            .child(post.getPostid())
                            .setValue(true);
                }else{
                    FirebaseDatabase.getInstance().getReference()
                            .child("Saves")
                            .child(firebaseUser.getUid())
                            .child(post.getPostid())
                            .removeValue();
                }
            }
        });

        viewHoler.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (viewHoler.like.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference()
                            .child("Likes")
                            .child(post.getPostid())
                            .child(firebaseUser.getUid())
                            .setValue(true);
                    //like를 눌렀을떄 notification add! -- > 게시
                    addNotification(post.getPublisher() , post.getPostid());

                }else{
                    FirebaseDatabase.getInstance().getReference()
                            .child("Likes")
                            .child(post.getPostid())
                            .child(firebaseUser.getUid())
                            .removeValue();

                }
            }
        });

        //위에 아이콘을눌러도 넘어가고 ..
        viewHoler.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext , CommentsActivity.class);
                intent.putExtra("postid" , post.getPostid());
                intent.putExtra("publisherid" , post.getPublisher());
                mContext.startActivity(intent);
            }
        });

        //인스타처럼 밑에 댓글을 눌러도되고 ..
        viewHoler.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext , CommentsActivity.class);
                intent.putExtra("postid" , post.getPostid());
                intent.putExtra("publisherid" , post.getPublisher());
                mContext.startActivity(intent);
            }
        });

        viewHoler.likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext , FollowersActivity.class);
                intent.putExtra("id" , post.getPostid());
                intent.putExtra("title" , "likes");
                mContext.startActivity(intent);
            }
        });

        viewHoler.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(mContext , view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){

                            case R.id.edit :
                                editPost(post.getPostid());
                                return true;

                            case R.id.delete :
                                FirebaseDatabase.getInstance().getReference("Posts")
                                        .child(post.getPostid())
                                        .removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Toast.makeText(mContext , "Deleted!" , Toast.LENGTH_SHORT).show();


                                                }
                                            }
                                        });
                                return true;

                            case R.id.report :
                                Toast.makeText(mContext , "Report cliked!" , Toast.LENGTH_SHORT).show();
                                return true;

                            default:
                                return false;
                        }
                    }
                });
                popupMenu.inflate(R.menu.post_menu);
                if (!post.getPublisher().equals(firebaseUser.getUid())){
                    //자기자신이 아니라면 당연 more의 edit,delete만!! VIEW VISIBLE -- > GONE
                    //그래서 popupMenu.setVisible(false); 이 아니고 .getMenu().findItem(R.id.edit) 까지
                    popupMenu.getMenu().findItem(R.id.edit).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
                }
                popupMenu.show();
            }
        });
    }//........onBindViewHoler.........

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    public class ViewHoler extends RecyclerView.ViewHolder{

        public ImageView image_profile , post_image , like , comment , save , more , heart;
        public TextView username , likes , publisher , description , comments;


        public ViewHoler(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            save = itemView.findViewById(R.id.save);
            comments = itemView.findViewById(R.id.comments);
            username = itemView.findViewById(R.id.username);
            likes = itemView.findViewById(R.id.likes);
            publisher = itemView.findViewById(R.id.publisher);
            description = itemView.findViewById(R.id.description);
            more = itemView.findViewById(R.id.more);
            heart = itemView.findViewById(R.id.heart_image);


        }
    }//..ViewHoler..

    private void getComments(String postid , final TextView comments){

        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Comments")
                .child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                comments.setText("All " + dataSnapshot.getChildrenCount() + " Comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void isLiked(String postid , final ImageView imageView){

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes")
                .child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(firebaseUser.getUid()).exists()){
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                }else{
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void addNotification(String userid , String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);
        //data insert 는 hash로 ... reference는 말그대로 참조만 하겠다는뜻 ..

        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put("userid" , firebaseUser.getUid());
        hashMap.put("text" , "Liked your post!");
        hashMap.put("postid" , postid);
        hashMap.put("ispost" , true);

        reference.push().setValue(hashMap);

    }

    private void nrLikes(final TextView likes , String postid){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes")
                .child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                likes.setText(dataSnapshot.getChildrenCount()+" Likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void publisherInfo(final ImageView image_profile , final TextView username , final TextView publisher , String userid){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                try {
                    assert user != null;
                    Picasso.get().load(user.getImageurl()).into(image_profile);

                }catch (Exception e){
                    Log.d(TAG , Objects.requireNonNull(e.getMessage()));
                }
                username.setText(user.getUsername());
                publisher.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void isSaved(final String postid, final ImageView  imageView){

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        assert firebaseUser != null;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Saves")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postid).exists()){
                    imageView.setImageResource(R.drawable.ic_save_black);
                    imageView.setTag("saved");
                }else{
                    imageView.setImageResource(R.drawable.ic_baseline_bookmark_border_24);
                    imageView.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void editPost(final String postid){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("Edit Post");
        final EditText editText = new EditText(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        editText.setLayoutParams(layoutParams);
        alertDialog.setView(editText);

        getText(postid , editText);

        alertDialog.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {

                HashMap<String , Object> hashMap = new HashMap<>();
                hashMap.put("description" , editText.getText().toString());

                FirebaseDatabase.getInstance().getReference("Posts")
                        .child(postid)
                        .updateChildren(hashMap);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    private void getText(String postid, final EditText editText){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts")
                .child(postid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                editText.setText(Objects.requireNonNull(dataSnapshot.getValue(Post.class)).getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
