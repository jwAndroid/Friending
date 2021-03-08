package com.example.kodinstagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.kodinstagram.Fragment.HomeFragment;
import com.example.kodinstagram.Fragment.NotificationFragment;
import com.example.kodinstagram.Fragment.ProfileFragment;
import com.example.kodinstagram.Fragment.SearchFragment;
import com.example.kodinstagram.Model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    Fragment selectedFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        //CommentAdapter 에서 퍼블리셔아이디를 쏨
        Bundle intent = getIntent().getExtras();
        if (intent != null){
            String publisher = intent.getString("publisherid");

            //get에서 받고나서 프래그먼트떄문에 프리페어런스로 꼭 저장
            SharedPreferences.Editor editor = getSharedPreferences("PREFS" , MODE_PRIVATE).edit();
            editor.putString("profileid" , publisher);
            editor.apply();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .commit();

        }else{

            /* 기본 화면(home) 트랜잭션 */
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

    }

    /*replace 보다는 add로 쓰도록 */
    private final BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @SuppressLint("NonConstantResourceId")
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                    switch (menuItem.getItemId()){

                        case R.id.nav_home :
                            selectedFragment = new HomeFragment();
                            break;

                        case R.id.nav_search :
                            selectedFragment = new SearchFragment();
                            break;

                        case R.id.nav_add :
                            selectedFragment = null;
                            /* 이부분 activity > firebase storage 이용 */
                            startActivity(new Intent(MainActivity.this , PostActivity.class));
                            break;

                        case R.id.nav_heart :
                            selectedFragment = new NotificationFragment();
                            break;

                        case R.id.nav_profile :
                            /* 해당유저의 uid를 저장해서 fragment 전환 */
                            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                            editor.putString("profileid"  , FirebaseAuth.getInstance().getCurrentUser().getUid());
                            editor.apply();

                            selectedFragment = new ProfileFragment();
                            break;
                    }

                    if (selectedFragment != null){
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container,selectedFragment) /*초기화된 프래그먼트로 replace*/
                                .commit();

                    }


                    return true;
                }
            };

}