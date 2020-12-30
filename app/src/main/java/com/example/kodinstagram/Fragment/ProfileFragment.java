package com.example.kodinstagram.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.kodinstagram.Adapter.MyFotoAdapter;
import com.example.kodinstagram.Adapter.UserAdapter;
import com.example.kodinstagram.EditProfileActivity;
import com.example.kodinstagram.FollowersActivity;
import com.example.kodinstagram.Model.Post;
import com.example.kodinstagram.Model.User;
import com.example.kodinstagram.OptionsActivity;
import com.example.kodinstagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment" ;
    ImageView image_profile , options;
    TextView posts, followers , following , fullname , username , bio;
    Button edit_profile;

    //key 저장
    private List<String> mySaves;

    RecyclerView recyclerView_saves;
    MyFotoAdapter myFotoAdapter_saves;
    //save post 저장
    List<Post> postList_saves;

    RecyclerView recyclerView;
    MyFotoAdapter myFotoAdapter;
    //image저장
    List<Post> postList;

    FirebaseUser firebaseUser;
    DatabaseReference statusReference;
    String profileid;

    ImageButton my_fotos, saved_fotos;
    ProgressDialog progressDialog;


    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        statusReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        //main 쪽에서 getIntent로 받아놓은 profileid
        //액티비티에서 프래그먼트로 데이터넘길떄 SharedPreferences 이용하는듯함.
        SharedPreferences prefs = requireContext().getSharedPreferences("PREFS" , Context.MODE_PRIVATE);
        profileid = prefs.getString("profileid","noon");

        image_profile = view.findViewById(R.id.image_profile);
        options = view.findViewById(R.id.options);
        posts = view.findViewById(R.id.posts);
        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.following);
        fullname = view.findViewById(R.id.fullname);
        username = view.findViewById(R.id.username);
        edit_profile = view.findViewById(R.id.edit_profile);
        my_fotos = view.findViewById(R.id.my_fotos);
        saved_fotos = view.findViewById(R.id.saved_fotos);
        bio = view.findViewById(R.id.bio);


        //recyclerView setting for post with grid
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        //GridLayoutManager
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(),3);
        recyclerView.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        myFotoAdapter = new MyFotoAdapter(getContext() , postList);
        recyclerView.setAdapter(myFotoAdapter);

        //recyclerView setting for save
        recyclerView_saves = view.findViewById(R.id.recycler_view_save);
        recyclerView_saves.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager_saves = new GridLayoutManager(getContext(),3);
        recyclerView_saves.setLayoutManager(linearLayoutManager_saves);
        postList_saves = new ArrayList<>();
        myFotoAdapter_saves = new MyFotoAdapter(getContext() , postList_saves);
        recyclerView_saves.setAdapter(myFotoAdapter_saves);

        recyclerView.setVisibility(View.VISIBLE);
        recyclerView_saves.setVisibility(View.GONE);

        userInfo();
        getFollowers();
        getNrPosts();
        myFotos();
        mysaves();

        //자기자신일때는 edit 프로필이지만
        // 다른사람을 보았을떄는 follow가 보이니까
        if (profileid.equals(firebaseUser.getUid())){
            //if myself?
            edit_profile.setText("Edit Profile");
        }else{
            checkFollow();
            saved_fotos.setVisibility(View.GONE);
        }


        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btn = edit_profile.getText().toString();

                if (btn.equals("Edit Profile")){

                    //go to EditProfile
                    startActivity(new Intent(getContext() , EditProfileActivity.class));

                }else if (btn.equals("follow")){

                    FirebaseDatabase.getInstance().getReference()
                            .child("Follow")
                            .child(firebaseUser.getUid())
                            .child("following")
                            .child(profileid)
                            .setValue(true);

                    FirebaseDatabase.getInstance().getReference()
                            .child("Follow")
                            .child(profileid)
                            .child("followers")
                            .child(firebaseUser.getUid())
                            .setValue(true);

                    addNotification();

                }else if(btn.equals("following")){

                    FirebaseDatabase.getInstance().getReference()
                            .child("Follow")
                            .child(firebaseUser.getUid())
                            .child("following")
                            .child(profileid)
                            .removeValue();
                    FirebaseDatabase.getInstance().getReference()
                            .child("Follow").child(profileid)
                            .child("followers")
                            .child(firebaseUser.getUid())
                            .removeValue();
                }
            }
        });

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext() , OptionsActivity.class);
                startActivity(intent);
            }
        });

        my_fotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                recyclerView.setVisibility(View.VISIBLE);
                recyclerView_saves.setVisibility(View.GONE);

            }
        });

        saved_fotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.GONE);
                recyclerView_saves.setVisibility(View.VISIBLE);
            }
        });

        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext() , FollowersActivity.class);
                intent.putExtra("id" , profileid);
                intent.putExtra("title" , "followers");
                startActivity(intent);
            }
        });

        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext() , FollowersActivity.class);
                intent.putExtra("id" , profileid);
                intent.putExtra("title" , "following");
                startActivity(intent);
            }
        });

        return view;
    }

    private void addNotification(){

        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Notifications")
                .child(profileid);

        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put("userid" , firebaseUser.getUid());
        hashMap.put("text" , "Started Following you!");
        //레퍼런스 노드가 변하면 안되서 그대로 남겨둠
        hashMap.put("postid" , "");
        hashMap.put("ispost" , false);

        reference.push().setValue(hashMap);

    }

    private void userInfo(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(profileid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (getContext() == null){ return; }
                User user = dataSnapshot.getValue(User.class);
                try {
                    assert user != null;
                    Glide.with(requireActivity()).load(user.getImageurl()).override(100,100).into(image_profile);
//                    Picasso.get().load(user.getImageurl()).into(image_profile);
                    username.setText(user.getUsername());
                    fullname.setText(user.getFullname());
                    bio.setText(user.getBio());

                }catch (Exception e){
                    Log.d(TAG , Objects.requireNonNull(e.getMessage()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkFollow(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow")
                .child(firebaseUser.getUid())
                .child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(profileid).exists()){
                    edit_profile.setText("following");
                }else{
                    edit_profile.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowers(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow")
                .child(profileid)
                .child("followers");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followers.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference()
                .child("Follow")
                .child(profileid)
                .child("following");
        //db의 이부분을 참조해서

        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                following.setText(""+dataSnapshot.getChildrenCount());
                //getChildrenCount 해주는작업

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getNrPosts(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    assert post != null;
                    if (post.getPublisher().equals(profileid)){
                        //해당 각각의 사용자의 포스트 개수를 count
                        i++;
                    }
                }
                posts.setText(""+i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //post가 내꺼인지 다른사람꺼인지 구분해서 세팅
    private void myFotos(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    //all snapshot see
                    Post post = snapshot.getValue(Post.class);
                    //post.getPublisher().equals(profileid) 가 중요 ,,
                    assert post != null;
                    if (post.getPublisher().equals(profileid)){
                        postList.add(post);
                    }
                }

                Collections.reverse(postList); // Collections의 내림차순 정렬 최신껄 맨앞으로
                myFotoAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void mysaves(){
        mySaves = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Saves")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    mySaves.add(snapshot.getKey());
                    Log.d(TAG , "키는:"+snapshot.getKey());
                }
                //먼저 save쪽 key를 get함
                //각각의 post에 접근하도록
                readSave();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readSave() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList_saves.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    for (String id : mySaves){
                        assert post != null;
                        //id ==>?? key
                        if (post.getPostid().equals(id)){
                            postList_saves.add(post);
                        }
                    }
                }
                myFotoAdapter_saves.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}