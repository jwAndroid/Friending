package com.example.kodinstagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.kodinstagram.Adapter.CommentAdapter;
import com.example.kodinstagram.Model.Comment;
import com.example.kodinstagram.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CommentsActivity extends AppCompatActivity {

    private static final String TAG = "CommentsActivity";

    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    EditText addcomment;
    ImageView image_profile;
    TextView post;

    String postid;
    String publisherid;

    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        //toolbar setting
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //comments details data getIntent!
        Intent intent = getIntent();
        postid = intent.getStringExtra("postid");
        publisherid = intent.getStringExtra("publisherid");

        //리사이클러뷰 생성
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        commentList = new ArrayList<>();
        /* 댓글은 POST객체 하나에 작성되야하니까 , 어댑터에 따로 postid를 넣어주어서 이 id를 가지고 조건작성을 해주기위해서 진행 */
        commentAdapter = new CommentAdapter(this , commentList , postid);
        recyclerView.setAdapter(commentAdapter);

        addcomment = findViewById(R.id.add_comment);
        image_profile = findViewById(R.id.image_profile);
        post = findViewById(R.id.post);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TextUtils.isEmpty(addcomment)
                if (addcomment.getText().toString().equals("")){
                    Toast.makeText(CommentsActivity.this , "댓글을 작성해주세요!" , Toast.LENGTH_SHORT).show();
                }else{
                    addComment();
                }
            }
        });

        getImage();
        readComments();

    }//.......onCreate..............

    /*addComment()  , readComments() 두개 함수로 구현되어지고 , 중요한건 postid로써 구분해준다.  */

    private void addComment() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Comments")
                .child(postid);

        String commentid = reference.push().getKey();

        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put("comment" , addcomment.getText().toString());
        hashMap.put("publisher" , firebaseUser.getUid()); // null?
        hashMap.put("commentid" , commentid);
        assert commentid != null;
        reference.child(commentid).setValue(hashMap);

        addNotification();
        addcomment.setText("");

    }

    /*코맨트를 달때마다 db쓰기 작성 : 알림text 처리 */
    private void addNotification(){
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Notifications")
                .child(publisherid);

        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put("userid" , firebaseUser.getUid());
        hashMap.put("text" , "Commented : " + " " + addcomment.getText().toString());
        hashMap.put("postid" , postid);
        hashMap.put("ispost" , true);

        reference.push().setValue(hashMap);
        //데이터베이스의 데이터 목록에 추가합니다. 목록에 새 노드를 푸시할 때마다 데이터베이스에서 고유 키를 생성

    }

    /* 하단 코맨트 작성 EDITTEXT 왼쪽부분 IMAGEVIEW에 자기자신 이미지 가져오기 */
    private void getImage(){
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(firebaseUser.getUid());
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
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    /* db 읽기 */
    private void readComments(){
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Comments")
                .child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();

                //조건에맞는 comment모델쪽 DB에 접근해서 commentList에 add해주는 작업
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Comment comment = snapshot.getValue(Comment.class);
                    commentList.add(comment);
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}