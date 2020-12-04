package com.example.kodinstagram.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kodinstagram.AddStoryActivity;
import com.example.kodinstagram.Model.Story;
import com.example.kodinstagram.Model.User;
import com.example.kodinstagram.R;
import com.example.kodinstagram.StoryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHoler> {

    private static final String TAG = "StoryAdapter";

    private Context mContext;
    private List<Story> mStory;


    public StoryAdapter() {


    }

    public StoryAdapter(Context mContext, List<Story> mStory) {
        this.mContext = mContext;
        this.mStory = mStory;
    }

    @NonNull
    @Override
    public ViewHoler onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        if (i == 0){
            View view = LayoutInflater.from(mContext).inflate(R.layout.add_story_item , viewGroup , false);
            return new StoryAdapter.ViewHoler(view);
        }else{
            View view = LayoutInflater.from(mContext).inflate(R.layout.story_item , viewGroup , false);
            return new StoryAdapter.ViewHoler(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHoler viewHoler, final int i) {

        final Story story = mStory.get(i);

        userInfo(viewHoler , story.getUserid() , i);

        if (viewHoler.getAdapterPosition() != 0){
            seenStory(viewHoler , story.getUserid());
        }
        if (viewHoler.getAdapterPosition() == 0){
            myStory(viewHoler.addstory_text , viewHoler.story_plus , false);
        }

        viewHoler.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewHoler.getAdapterPosition() == 0){
                    myStory(viewHoler.addstory_text , viewHoler.story_plus , true);
                }else{
                    Intent intent = new Intent(mContext , StoryActivity.class);
                    intent.putExtra("userid" , story.getUserid());
                    mContext.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mStory.size();
    }

    public class ViewHoler extends RecyclerView.ViewHolder{

        public ImageView story_photo , story_plus , story_photo_seen;
        public TextView story_username , addstory_text;

        public ViewHoler(@NonNull View itemView) {
            super(itemView);

            story_photo = itemView.findViewById(R.id.story_photo);
            story_plus = itemView.findViewById(R.id.story_plus);
            story_photo_seen = itemView.findViewById(R.id.story_photo_seen);
            story_username = itemView.findViewById(R.id.story_username);
            addstory_text = itemView.findViewById(R.id.addstory_text);


        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0){
            return 0;
        }
        return 1;
    }

    private void userInfo(final ViewHoler viewHoler , final String userid , final int pos){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                try{
                    assert user != null;
                    Picasso.get().load(user.getImageurl()).into(viewHoler.story_photo);
                    if (pos != 0){
                        Picasso.get().load(user.getImageurl()).into(viewHoler.story_photo_seen);
                        viewHoler.story_username.setText(user.getUsername());
                    }

                }catch (Exception e){
                    Log.e(TAG , Objects.requireNonNull(e.getMessage()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void myStory(final TextView textView , final ImageView imageView , final boolean click){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                long timecurrent = System.currentTimeMillis();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Story story = snapshot.getValue(Story.class);
                    if (timecurrent > story.getTimestart() && timecurrent < story.getTimeend()){
                        count++;
                    }
                }
                if (click){
                    if (count > 0){
                        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "View story",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which) {
                                        Intent intent = new Intent(mContext , StoryActivity.class);
                                        intent.putExtra("userid" , FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        mContext.startActivity(intent);
                                        dialogInterface.dismiss();
                                    }
                                });
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which) {
                                        Intent intent = new Intent(mContext , AddStoryActivity.class);
                                        mContext.startActivity(intent);
                                        dialogInterface.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }else{
                        Intent intent = new Intent(mContext , AddStoryActivity.class);
                        mContext.startActivity(intent);
                    }
                }else{
                    if (count > 0){
                        textView.setText("My Story");
                        imageView.setVisibility(View.GONE);
                    }else{
                        textView.setText("Add story");
                        imageView.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void seenStory(final ViewHoler viewHoler , String userid){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if (!snapshot
                            .child("views")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .exists() && System.currentTimeMillis() <
                            snapshot.getValue(Story.class).getTimeend()){
                        i++;
                    }
                }

                if (i > 0){
                    viewHoler.story_photo.setVisibility(View.VISIBLE);
                    viewHoler.story_photo_seen.setVisibility(View.GONE);
                }else{
                    viewHoler.story_photo.setVisibility(View.GONE);
                    viewHoler.story_photo_seen.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}
