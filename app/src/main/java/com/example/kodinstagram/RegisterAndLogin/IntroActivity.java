package com.example.kodinstagram.RegisterAndLogin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.kodinstagram.MainActivity;
import com.example.kodinstagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class IntroActivity extends AppCompatActivity {

    private static final String TAG = "SubActivity" ;
    ImageView introImage_iv;
    LinearLayout introLayout;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        introImage_iv = findViewById(R.id.introImage_iv);
        introLayout = findViewById(R.id.introLayout);

        /* 애니메이션 객체로써 효과추가 >> 두가지에 추가 */
        Animation animation = AnimationUtils.loadAnimation(this , R.anim.mytran);
        introImage_iv.startAnimation(animation);
        introLayout.startAnimation(animation);

        Thread timeThread = new Thread(){
            public void run(){
                try {
                    sleep(3000);

                }catch (Exception e){
                    Log.d(TAG , Objects.requireNonNull(e.getMessage()));
                }
                finally {

                    firebaseUser = FirebaseAuth.getInstance().getCurrentUser();//auth객체 얻어와서
                    if (firebaseUser != null){ //getCurrentUser존재한다면 >> 바로 메인으로
                        startActivity(new Intent(IntroActivity.this , MainActivity.class));
                        finish();
                    }else{ // 처음이라면 startActivity (for register)
                        Intent intent = new Intent(IntroActivity.this , StartActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        };
        timeThread.start();
    }
}