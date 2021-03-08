package com.example.kodinstagram.RegisterAndLogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kodinstagram.MainActivity;
import com.example.kodinstagram.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText username,fullname,email,password,passwordConfirm;
    Button register;
    TextView txt_login , txt_LogInTheOtherWay;

    //Firebase Setting
    FirebaseAuth auth;
    DatabaseReference reference;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = findViewById(R.id.username);
        fullname = findViewById(R.id.fullname);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register = findViewById(R.id.register);
        txt_login = findViewById(R.id.txt_login);
        passwordConfirm = findViewById(R.id.passwordConfirm);
        txt_LogInTheOtherWay = findViewById(R.id.txt_LogInTheOtherWay);

        auth = FirebaseAuth.getInstance();

        txt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this , LoginActivity.class));
                finish();

            }
        });

        txt_LogInTheOtherWay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this , OtherWayLogInActivity.class));
                finish();

            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pd = new ProgressDialog(RegisterActivity.this);
                pd.setMessage("Please wait..");
                pd.show();

                String str_username = username.getText().toString();
                String str_fullname = fullname.getText().toString();
                String str_email = email.getText().toString();
                String str_password = password.getText().toString();
                String passwordConfirm = password.getText().toString();

                if (TextUtils.isEmpty(str_username) || TextUtils.isEmpty(str_fullname) ||
                        TextUtils.isEmpty(str_email) || TextUtils.isEmpty(str_password)) {

                    Toast.makeText(RegisterActivity.this , "모두 넣어주세요!" , Toast.LENGTH_SHORT).show();
                    pd.dismiss();

                }else if(str_password.length() < 6){
                    Toast.makeText(RegisterActivity.this , "패스워드는 최소6자리 이상입니다." , Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }else{
                    if (password.getText().toString().equals(passwordConfirm)){
                        /* 공백,이메일상태,패스워드 제어해주고나서 모두 충족한다면 register 진행 */
                        register(str_username , str_fullname , str_email , str_password);
                        pd.dismiss();
                    }else{
                        Toast.makeText(RegisterActivity.this , "비밀번호가 서로 일치하지 않습니다." , Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }

                }
            }
        });

    }


    /* register 함수 부분 */
    private void register(final String username , final String fullname , String email  , String password ) {
        /* 유저의 이메일,패스워드를 파라메터로 던져서 auth와 createUserWithEmailAndPassword 로 연동  */
        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(RegisterActivity.this , new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    /* 현재 유저 받아와서 uid 미리 할당  */
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    String userid = firebaseUser.getUid();

                    /* db를 위한 레퍼런스 객체 생성 */
                    reference = FirebaseDatabase.getInstance().getReference()
                            .child("Users")
                            .child(userid); /* 고유 uid로써 따로따로 저장 */

                    HashMap<String , Object> hashMap = new HashMap<>();
                    hashMap.put("id" , userid);
                    hashMap.put("username" , username.toLowerCase());
                    hashMap.put("fullname" , fullname); //TODO : fullname change nickName
                    hashMap.put("bio" , "");
                    hashMap.put("imageurl" , "https://firebasestorage.googleapis.com/v0/b/kodinstagram-492ee.appspot.com/o/placeholer.png?alt=media&token=c9c9b116-09cc-4149-bf65-18175c1c8478");
                    hashMap.put("search" , username.toLowerCase());
                    hashMap.put("platform" , "none");
                    /* 해시로 묶어서 */

                    /* 레퍼런스에 해시를 set */
                    reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                pd.dismiss();
                                Intent intent = new Intent(RegisterActivity.this , MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                /*새로운 Task 생성 후 테스크안에 activity를 추가 | 새로운 TASK 생성   */
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                }else{
                    pd.dismiss();
                    Toast.makeText(RegisterActivity.this ,
                            "You can`t register with this email or password"
                    ,Toast.LENGTH_SHORT).show();
                }

            }
        });


    }


}