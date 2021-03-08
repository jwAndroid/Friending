package com.example.kodinstagram;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kodinstagram.ChatFragment.ChatsFragment;
import com.example.kodinstagram.ChatFragment.UsersFragment;
import com.example.kodinstagram.Model.Chat;
import com.example.kodinstagram.Model.User;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatMainActivity extends AppCompatActivity {

    /* 뷰페이저 생성 + db 쿼리 + 메세지개수 카운트 */

    private static final String TAG = "ChatMainActivity";
    CircleImageView profile_image;
    TextView username;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    ImageView homeIv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        homeIv = findViewById(R.id.homeIv);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        homeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChatMainActivity.this, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                // 디폴트생성자 생성 파라메터에 3개의 값이 들어가야하는데 없이 유저모델을 생성하려고하니까 생기는 에러임
                assert user != null;
                username.setText(user.getUsername());

                if (user.getImageurl().equals("https://firebasestorage.googleapis.com/v0/b/kodinstagram-492ee.appspot.com/o/placeholer.png?alt=media&token=c9c9b116-09cc-4149-bf65-18175c1c8478")){
                    profile_image.setImageResource(R.drawable.placeholer);
                }else{
                    try {
                        //TODO : user.getImageurl() << 이 데이터의 크기가 그대로 프로필이미지에 셋팅됨 그럼 어찌되겠어? 당연히 메모리값 올라가겠지??
                        //OVERRIDE를 써서 줄여주든 해야함 단 , 화질이 저하됨
                        Picasso.get().load(user.getImageurl()).into(profile_image);
                    }catch (Exception e){
                        Log.d(TAG , Objects.requireNonNull(e.getMessage()));
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final TabLayout tab_layout = findViewById(R.id.tab_layout);
        final ViewPager view_pager = findViewById(R.id.view_pager);

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
                int unread = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    assert chat != null;
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isIsseen()){
                        unread++;
                    }
                }
                if (unread == 0){
                    viewPagerAdapter.addFragment(new ChatsFragment() , "Chats");
                }else{
                    viewPagerAdapter.addFragment(new ChatsFragment() , "("+unread+") Chats");
                }

                //VIEW PAGER
                viewPagerAdapter.addFragment(new UsersFragment() , "Users");

                view_pager.setAdapter(viewPagerAdapter);
                tab_layout.setupWithViewPager(view_pager);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }//..................onCreate...................

    class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPagerAdapter(FragmentManager fm){
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment (Fragment fragment , String title){

            fragments.add(fragment);
            titles.add(title);
        }
        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

    }//.............ViewPager.................



}