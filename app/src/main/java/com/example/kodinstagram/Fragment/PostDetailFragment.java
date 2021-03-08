package com.example.kodinstagram.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kodinstagram.Adapter.PostAdapter;
import com.example.kodinstagram.ChatMainActivity;
import com.example.kodinstagram.MessageActivity;
import com.example.kodinstagram.Model.Post;
import com.example.kodinstagram.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class PostDetailFragment extends Fragment {

    String postid;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;

    public PostDetailFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_post_detail, container, false);

        //TODO : 이곳저곳 SharedPreferences 로 저장해뒀던 postid 를 get 핵심포인트
        SharedPreferences preferences = getContext().getSharedPreferences("PREFS" , Context.MODE_PRIVATE);
        postid = preferences.getString("postid","none");

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        postList = new ArrayList<>();
        //post쪽 긁어와야하니까 어탭터 생성
        postAdapter = new PostAdapter(getContext() , postList);
        recyclerView.setAdapter(postAdapter);

        readPost();

        return view;
    }

    //postList 에 add해주기위한 db작업
    private void readPost() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Posts")
                .child(postid);
        /*SharedPreferences 저장한 postid를 가져와서 레퍼런스생성 */

        //postid는 하나이니까 list에 하나만 저장후 최종적으로 뿌려줌
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /* 하나밖에 없으니 바로 진행 */
                postList.clear();
                Post post = dataSnapshot.getValue(Post.class);
                //위에있는 db값을 긁어와서 Post모델에 접근해서 get해서 post쪽에 넣어줌
                postList.add(post);
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}