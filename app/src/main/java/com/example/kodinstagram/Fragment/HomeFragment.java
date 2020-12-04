package com.example.kodinstagram.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.kodinstagram.Adapter.PostAdapter;
import com.example.kodinstagram.Adapter.StoryAdapter;
import com.example.kodinstagram.Adapter.UserAdapter;
import com.example.kodinstagram.ChatMainActivity;
import com.example.kodinstagram.Model.Post;
import com.example.kodinstagram.Model.Story;
import com.example.kodinstagram.Model.User;
import com.example.kodinstagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class HomeFragment extends Fragment {


    private static final String TAG = "HomeFragment";
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postLists;

    //Story
    private RecyclerView recyclerView_story;
    private StoryAdapter storyAdapter;
    private List<Story> storyLists;

    private List<String> followingList;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    ProgressBar progressBar;
    ImageView chatList_Iv;

    public HomeFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        progressBar = view.findViewById(R.id.progress_circular);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        //..............Post.................
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        postLists = new ArrayList<>();
        postAdapter = new PostAdapter(getContext() , postLists);
        recyclerView.setAdapter(postAdapter);

        //..............Story.................
        recyclerView_story = view.findViewById(R.id.recycler_view_story);
        recyclerView_story.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getContext() ,
                LinearLayoutManager.HORIZONTAL , false);
        recyclerView_story.setLayoutManager(linearLayoutManager1);
        storyLists = new ArrayList<>();
        storyAdapter = new StoryAdapter(getContext() , storyLists);
        recyclerView_story.setAdapter(storyAdapter);

        chatList_Iv = view.findViewById(R.id.chatList_Iv);
        chatList_Iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(getContext() , ChatListActivity.class);
                //TODO : 원래는 CHAT APP 에서의 CHATS 프래그먼트쪽에있는 부분을 ACTIVITY로 바로 넘어가게끔 구현했으며 , 따로 PROFILE ,USER를 만들지 않았다
                //TODO : ChatMainActivity Chat클래스의 메인함수가 되고 이를 축으로 viewPager 구현
                //TODO : 한마디로 ChatMainActivity 으로 이동함으로써 그대로 CHAT APP을 장착
                Intent intent = new Intent(getContext() , ChatMainActivity.class);
                startActivity(intent);
            }
        });

        checkFollowing();
        return view;
    }

    private void checkFollowing(){

        followingList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    followingList.add(snapshot.getKey());
                }
                followingList.add(uid);
                readPosts();
                readStory();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readPosts(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postLists.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);

                    for (String id : followingList){
                        if (post.getPublisher().equals(id)){
                            postLists.add(post);

                        }
                    }
                }
                postAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readStory(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story");
        final String user = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long timecurrent = System.currentTimeMillis();
                storyLists.clear();
                storyLists.add(new Story("" , 0 , 0 , "" , user));

                    for (String id : followingList){
                        int countStory = 0;
                        Story story = null;

                        for (DataSnapshot snapshot : dataSnapshot.child(id).getChildren()){
                            story = snapshot.getValue(Story.class);

                            if (timecurrent > story.getTimestart() && timecurrent < story.getTimeend()){
                                countStory++;
                            }
                        }

                        if (countStory>0){
                            storyLists.add(story);
                        }
                    }
                storyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}