package com.example.kodinstagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.kodinstagram.Adapter.MessageAdapter;
import com.example.kodinstagram.ChatFragment.UsersFragment;
import com.example.kodinstagram.Fragment.APIService;
import com.example.kodinstagram.Model.Chat;
import com.example.kodinstagram.Model.User;
import com.example.kodinstagram.Notifications.Client;
import com.example.kodinstagram.Notifications.Data;
import com.example.kodinstagram.Notifications.MyResponse;
import com.example.kodinstagram.Notifications.Sender;
import com.example.kodinstagram.Notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    private static final String TAG = "MessageActivity";
    CircleImageView profile_image;
    TextView username;
    ImageButton btn_send;
    EditText text_send;

    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

    MessageAdapter messageAdapter;
    List<Chat> mchat;
    RecyclerView recyclerView;

    Intent intent;
    String hisUid; // 상대방 uid

    ValueEventListener seenListener;
    APIService apiService;
    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessageActivity.this, ChatMainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        /* fcm api call base body */
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true); // 리사이클러뷰 끝쪽으로 이동
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);

        /* ***중요*** intent 로 상대 UID를 꼭 받아서 진행한다. 이부분으로써 초기 구현부 시작. */
        intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");
        Log.d(TAG,"HIS UID::" + hisUid);
        assert firebaseUser != null;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG,"MY UID::" + firebaseUser.getUid());

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String msg = text_send.getText().toString();
                if (!msg.equals("")){
                    sendMessage(firebaseUser.getUid() , hisUid , msg);
                }else{
                    Toast.makeText(MessageActivity.this , "You can`t send empty message!",Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Users").child(hisUid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                username.setText(user.getUsername());
                if (user.getImageurl().equals("")){
                    profile_image.setImageResource(R.drawable.placeholer);
                }else{
                    try {
                        Glide.with(getApplicationContext()).load(user.getImageurl()).into(profile_image);
//                        Picasso.get().load(user.getImageurl()).into(profile_image);
                    }catch (Exception e){
                        Log.d(TAG,"" + e.getMessage());
                    }
                }

                readMessages(firebaseUser.getUid() , hisUid , user.getImageurl());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        seenMessage(hisUid);

    }//...............onCreate....................

    private void seenMessage(final String userid){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    assert chat != null;
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)){

                        HashMap<String , Object> hashMap = new HashMap<>();
                        hashMap.put("isseen" , true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /* RealTime db로써 동작되며 , 데이터베이스를 읽고쓰는것에서 구현된다. */
    private void sendMessage(String sender , final String receiver , String message){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put("sender" , sender);
        hashMap.put("receiver" , receiver);
        hashMap.put("message" , message);
        hashMap.put("isseen" , false);

        reference.child("Chats")
                .push()
                .setValue(hashMap);

        /*레퍼런스 하위노드로써 자기자신과,상대 id 레퍼런스 인스턴스 생성 */
        final DatabaseReference chatRef = FirebaseDatabase.getInstance()
                .getReference("Chatlist")
                .child(firebaseUser.getUid())
                .child(hisUid);

        /* addListenerForSingleValueEvent 싱글벨류는 스냅숏을 한번만 읽고 버린다. */
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(hisUid);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //TODO : 레퍼런스를 두개를 생성. 새로운 채팅을 생성할때, 새로운 메세지를 '받는사람'이 CHATS에 리스트에 생성되야하기떄문.
        //TODO : 한마디로 보내는사람이 메세지 리스트생성 chatRef , 받는사람이 새로운 메세지 리스트 생성 chatRef2
        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance()
                .getReference("Chatlist")
                .child(hisUid)
                .child(firebaseUser.getUid());

        chatRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef2.child("id")
                            .setValue(firebaseUser.getUid());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //TODO : CLOUDING MESSAGEING API ...
        //TODO : 알림의 축이 되는 부분 notify 의 로직순서를 잘보고 게시물에도 적용
        final String msg = message;
        reference = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                if (notify){
                    sendNotification(receiver , user.getUsername() , msg);
                }
                notify = false;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    /* 디바이스 to 디바이스 통신은 retrofit으로 만들어서 진행 */
    private void sendNotification(String receiver , final String username , final String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    //아이콘이 원래는 ic_런처
                    Data data = new Data(firebaseUser.getUid(), R.drawable.favicon_new , username+":"+message,
                            "메세지" , hisUid);

                    assert token != null;
                    Sender sender = new Sender(data , token.getToken());

                    apiService
                            .sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse( Call<MyResponse> call ,  Response<MyResponse> response) {
                            if (response.code() == 200){
                                assert response.body() != null;
                                if (response.body().success != 1){
                                    Toast.makeText(MessageActivity.this , "Failed!" , Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readMessages(final String myid , final String hisUid , final String imageurl){
        mchat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);

                    assert chat != null;
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(hisUid)
                            || chat.getReceiver().equals(hisUid) && chat.getSender().equals(myid)) {
                        mchat.add(chat);
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this , mchat , imageurl);
                    recyclerView.setAdapter(messageAdapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /*파라메터 id 저장 함수 */
    private void currentUser(String userid){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS" , MODE_PRIVATE).edit();
        editor.putString("currentuser" , userid);
        editor.apply();
    }

    /**/
    @Override
    protected void onResume() {
        super.onResume();
        currentUser(hisUid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        currentUser("none");
    }

}