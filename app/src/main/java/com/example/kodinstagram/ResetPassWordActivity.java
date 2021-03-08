package com.example.kodinstagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.kodinstagram.RegisterAndLogin.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ResetPassWordActivity extends AppCompatActivity {

    EditText send_email;
    Button btn_reset;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pass_word);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Reset PassWord");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        send_email = findViewById(R.id.send_email);
        btn_reset = findViewById(R.id.btn_reset);

        firebaseAuth = FirebaseAuth.getInstance();

        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = send_email.getText().toString();
                if (email.equals("")){
                    Toast.makeText(ResetPassWordActivity.this , "please Insert Email!" ,
                            Toast.LENGTH_SHORT).show();
                }else{
                    /* email 값을 파라메터로 넣어서 진행 */
                    firebaseAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(ResetPassWordActivity.this , "please check your Email!" ,
                                                Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(ResetPassWordActivity.this , LoginActivity.class));
                                    }else{
                                        String error = Objects.requireNonNull(task.getException()).getMessage();
                                        Toast.makeText(ResetPassWordActivity.this ,error,
                                                Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });
                }

            }
        });

    }//..............onCreate............................



}