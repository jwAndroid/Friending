package com.example.kodinstagram.Fragment;

import android.annotation.SuppressLint;
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
        //액티비티에서 프래그먼트로 데이터넘길떄 SharedPreferences 이용
        /*현재 프래그먼트에서 가장 중요한부분 */
        SharedPreferences prefs = requireContext().getSharedPreferences("PREFS" , Context.MODE_PRIVATE);
        profileid = prefs.getString("profileid","noon");
        Log.d(TAG, "profileID ::"+profileid);

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
                    /*자기자신이라면 단순히 EditProfileActivity 로 startActivity */
                    startActivity(new Intent(getContext() , EditProfileActivity.class));

                }else if (btn.equals("follow")){
                    //팔로잉을 누를떄
                    FirebaseDatabase.getInstance().getReference()
                            .child("Follow")
                            .child(firebaseUser.getUid()) //자기자신
                            .child("following") //following
                            .child(profileid) //상대방
                            .setValue(true);

                    FirebaseDatabase.getInstance().getReference()
                            .child("Follow")
                            .child(profileid)
                            .child("followers") //followers
                            .child(firebaseUser.getUid())
                            .setValue(true);

                    //노티피케이션 데이터쓰기 까지 진행
                    addNotification();

                }else if(btn.equals("following")){
                    //팔로잉을 해제할때

                    FirebaseDatabase.getInstance().getReference()
                            .child("Follow")
                            .child(firebaseUser.getUid())
                            .child("following")
                            .child(profileid)
                            .removeValue(); //rm
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

    /*notification fragment를 위한 데이터쓰기 */
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

    /*중요한 profileid 를 가지고서 해당하는 부분으로가서 user를 가져와서 view에 셋팅해주는부분. */
    private void userInfo(){
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(profileid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (getContext() == null){
                    return;
                }

                User user = dataSnapshot.getValue(User.class);
                try {
                    assert user != null;
                    Glide.with(requireActivity())
                            .load(user.getImageurl())
                            .override(100,100)
                            .into(image_profile);

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

    /*팔로우가 되어있는지 아닌지 setText를 바꿔주는 부분 */
    private void checkFollow(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow")
                .child(firebaseUser.getUid())
                .child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(profileid).exists()){
                    /* 해당 레퍼런스 하위 노드에 위 쉐어드로 가져온 profileid가 존재한다면
                    *  즉 , 팔로우가 되어있는지 아닌지 한다면 */
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

    /*dataSnapshot getChildrenCount*/
    private void getFollowers(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow")
                .child(profileid)
                .child("followers");

        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followers.setText(""+dataSnapshot.getChildrenCount());
                /*단순히 스냅숏 count로써 개수 가져와서 진행. */
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
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                following.setText(""+dataSnapshot.getChildrenCount());
                //getChildrenCount 해줌

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getNrPosts(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        /*모든 Posts쪽에서 */
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class); //자바객체로 가져온후에
                    assert post != null;
                    if (post.getPublisher().equals(profileid)){ //이렇게 진행 . profileid는 현재유저의 uid
                        //해당 각각의 사용자의 포스트 개수를 count
                        i++;
                    }
                }
                posts.setText(""+i); //반복문이 벗어난다면 초기화된 int i를 set view
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
                    if (post.getPublisher().equals(profileid)){ //자기 자신이라면
                        postList.add(post); // 해당 포스트model객체를 리스트에 담아서 리사이클러뷰 어댑터에 껴준것
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
                    //리스트에 스냅숏고유 key 저장
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
                        if (post.getPostid().equals(id)){ // mysaves () 에서 저장한 값을 가지고서 진행
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