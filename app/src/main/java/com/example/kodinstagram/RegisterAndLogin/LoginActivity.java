package com.example.kodinstagram.RegisterAndLogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kodinstagram.MainActivity;
import com.example.kodinstagram.R;
import com.example.kodinstagram.ResetPassWordActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    EditText email , password;
    Button login;
    TextView txt_signup;
    TextView forgot_password;

    FirebaseAuth auth;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        txt_signup = findViewById(R.id.txt_signup);
        forgot_password = findViewById(R.id.forgot_password);

        pd = new ProgressDialog(LoginActivity.this);

        auth = FirebaseAuth.getInstance();

        txt_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this , RegisterActivity.class));
            }
        });

        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this , ResetPassWordActivity.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.setMessage("Logging In...");
                pd.show();

                String str_email = email.getText().toString();
                String str_password = password.getText().toString();

                if (TextUtils.isEmpty(str_email) || TextUtils.isEmpty(str_password)) {

                    Toast.makeText(LoginActivity.this ,
                            "All fileds are required!"
                    ,Toast.LENGTH_SHORT).show();

                }else if(str_password.length() < 6){
                    Toast.makeText(LoginActivity.this ,
                            "Password at least 6 charters"
                            ,Toast.LENGTH_SHORT).show();
                }else{

                    login(str_email , str_password);

                }

            }
        });




    }

    private void login(String email , String password) {

        auth.signInWithEmailAndPassword(email , password)
                .addOnCompleteListener(LoginActivity.this , new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){
                            DatabaseReference reference = FirebaseDatabase.getInstance()
                                    .getReference().child("Users").child(auth.getCurrentUser().getUid());

                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    pd.dismiss();
                                    Intent intent = new Intent(LoginActivity.this , MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    pd.dismiss();
                                    Log.w(TAG , error.getMessage());

                                }
                            });
                        }else{
                            pd.dismiss();
                            Toast.makeText(LoginActivity.this ,
                                    "Authentication Failed!" , Toast.LENGTH_SHORT).show();
                        }

                    }
                });


    }
}