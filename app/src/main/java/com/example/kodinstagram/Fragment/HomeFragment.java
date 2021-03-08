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

    /*posting 리스트를 위한 변수*/
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postLists;

    /*HomeFragment 상단부분 */
    //Story
    private RecyclerView recyclerView_story;
    private StoryAdapter storyAdapter;
    private List<Story> storyLists;

    /**/
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

        //..............Post 리사이클러뷰 세로방향.................
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        postLists = new ArrayList<>();
        postAdapter = new PostAdapter(getContext() , postLists);
        recyclerView.setAdapter(postAdapter);

        //..............Story 리사이클러뷰 가로방향 .................
        recyclerView_story = view.findViewById(R.id.recycler_view_story);
        recyclerView_story.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getContext() ,
                LinearLayoutManager.HORIZONTAL , false);
        recyclerView_story.setLayoutManager(linearLayoutManager1);
        storyLists = new ArrayList<>();
        storyAdapter = new StoryAdapter(getContext() , storyLists);
        recyclerView_story.setAdapter(storyAdapter);

        chatList_Iv = view.findViewById(R.id.chatList_Iv);
        /*채팅 */
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

    /* */
    private void checkFollowing(){

        followingList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()) /*현재 자기자신 uid 하위노드 */
                .child("following");

                /*이렇게 레퍼런스 인스턴스 생성 */

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    /*해당 레퍼런스 스냅샷을 모두 가져와서 */
                    followingList.add(snapshot.getKey());
                    /*list를 add 해준후에 */
                }
                followingList.add(uid); /*자기 자신의 uid을 포함하여(자기 자신의 글도 보이게하기위해서 ) */

                readPosts(); //후에 post 진행
                readStory();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    /*PostActivity에서 db에 setValue()해서 넣어준 데이터를 하나하나 읽어옴 */
    private void readPosts(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        /*기본 레퍼런스 인스턴스 생성*/

        /* 리스너는 데이터의 초기 상태가 확인될 때 한 번 트리거된 후 데이터가 변경될 때마다 다시 트리거 */
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /* 경로에 있던 콘텐츠의 정적 스냅샷을 읽음 */
                postLists.clear(); //리스트 먼저 clear()하고 진행해야한다.

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class); //하위 노드의 모든부분을 post 자바객체로써 저장함.
                    /* 데이터베이스에서 지정된 위치에 있던 데이터를 포함하는 DataSnapshot을 수신합니다.
                     * 스냅샷에 대해 getValue()를 호출하면 데이터의 자바 객체 표현이 반환 */

                    for (String id : followingList){
                        if (post.getPublisher().equals(id)){
                            /* 먼저 followingList를 저장후에 , 자기가 팔로우한 사람만
                            * 즉 post.getPublisher().equals(id) 때에만 add해주는경우*/
                            postLists.add(post);
                        }
                    }
                }
                postAdapter.notifyDataSetChanged(); //데이터변경시 수신받기위해
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        /* 이렇게 리스너를 이용하여 데이터를 읽고 쓰는 방식이 대부분이다. */

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