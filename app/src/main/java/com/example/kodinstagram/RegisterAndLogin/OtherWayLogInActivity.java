package com.example.kodinstagram.RegisterAndLogin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.kodinstagram.MainActivity;
import com.example.kodinstagram.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Objects;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;



public class OtherWayLogInActivity extends AppCompatActivity {

    private static final String TAG = "OtherWayLogInActivity" ;

    private static final int RC_SIGN_IN = 100;
    SignInButton mGoogleSigninBtn;
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    CallbackManager mCallbackManager;
    LoginButton loginButton;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_way_log_in);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait..");
        //TODO : GOOGLE , NAVER LOGIN

        mAuth = FirebaseAuth.getInstance();
        //Google Login
        mGoogleSigninBtn = findViewById(R.id.googleLoginBtn);


        //Google Login
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this , googleSignInOptions);
        mGoogleSigninBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent , RC_SIGN_IN);
            }
        });

        //FaceBook Login
        FacebookSdk.sdkInitialize(OtherWayLogInActivity.this);

        mCallbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.facebookLoginBtn);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });


    }//.......................onCreate..............................

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            updateUI(currentUser);
        }
    }


    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Log.d(TAG, "signInWithCredential:success");
                            final FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;

                            if (Objects.requireNonNull
                                    (Objects.requireNonNull(task.getResult())
                                            .getAdditionalUserInfo()).isNewUser()){

                                String userid = user.getUid();
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                        .child("Users")
                                        .child(userid);

                                HashMap<Object , String > hashMap = new HashMap<>();
                                hashMap.put("id" , userid);
                                hashMap.put("username" , user.getDisplayName());
                                hashMap.put("fullname" , user.getDisplayName()); //TODO : fullname change nickName
                                hashMap.put("bio" , "");
                                hashMap.put("imageurl" , "https://firebasestorage.googleapis.com/v0/b/kodinstagram-492ee.appspot.com/o/placeholer.png?alt=media&token=c9c9b116-09cc-4149-bf65-18175c1c8478");
                                hashMap.put("search" , "");
                                hashMap.put("platform" , "facebook");

                                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()){

                                            progressDialog.dismiss();
                                            Log.w(TAG , "Google sign in Successful"+ user.getDisplayName());
                                            Toast.makeText(OtherWayLogInActivity.this , user.getDisplayName() , Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(OtherWayLogInActivity.this , MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        }

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(OtherWayLogInActivity.this , e.getMessage() , Toast.LENGTH_SHORT).show();
                                    }
                                });


                            }else{

                                //TODO : 이부분에서 로그아웃을 진행하고나서 다시 구글계정으로 로그인하는경우 무한로딩 버그생기는 경우 isNewUser 를 잘 확인해야함
                                //당연히 새로운유저가 아니기때문에 , ELSE 를 타야한다.
                                Log.w(TAG , "Google sign in Successful"+ user.getDisplayName());
                                Intent intent = new Intent(OtherWayLogInActivity.this , MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();


                            }

                        } else {

                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(OtherWayLogInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null){

            Intent intent = new Intent(OtherWayLogInActivity.this , MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        }else{
            Toast.makeText(OtherWayLogInActivity.this, "Please signin!",
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account);

            }catch (ApiException e){
                Log.w(TAG , "Google sign in failed" , e);
                Toast.makeText(OtherWayLogInActivity.this , e.getMessage() , Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        progressDialog.show();
        Log.w(TAG , "firebaseAuthWithGoogle" +account.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken() , null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            final FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;

                            if (Objects.requireNonNull
                                    (Objects.requireNonNull(task.getResult())
                                            .getAdditionalUserInfo()).isNewUser()){

                                String email = user.getEmail();
                                String userid = user.getUid();
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                        .child("Users")
                                        .child(userid);

                                HashMap<Object , String > hashMap = new HashMap<>();
                                hashMap.put("id" , userid);
                                hashMap.put("username" , user.getDisplayName());
                                hashMap.put("fullname" , user.getDisplayName()); //TODO : fullname change nickName
                                hashMap.put("bio" , "");
                                hashMap.put("imageurl" , "https://firebasestorage.googleapis.com/v0/b/kodinstagram-492ee.appspot.com/o/placeholer.png?alt=media&token=c9c9b116-09cc-4149-bf65-18175c1c8478");
                                hashMap.put("search" , "");
                                hashMap.put("platform" , "google");

                                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()){

                                            progressDialog.dismiss();
                                            Log.w(TAG , "Google sign in Successful"+ user.getDisplayName());
                                            Toast.makeText(OtherWayLogInActivity.this , user.getDisplayName() , Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(OtherWayLogInActivity.this , MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        }

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(OtherWayLogInActivity.this , e.getMessage() , Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }else{
                                //TODO : 이부분에서 로그아웃을 진행하고나서 다시 구글계정으로 로그인하는경우 무한로딩 버그생기는 경우 isNewUser 를 잘 확인해야함
                                //당연히 새로운유저가 아니기때문에 , ELSE 를 타야한다.
                                Log.w(TAG , "Google sign in Successful"+ user.getDisplayName());
                                Intent intent = new Intent(OtherWayLogInActivity.this , MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }

                        }else{
                            Log.w(TAG , "signInWithCredential failed!");
                            progressDialog.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(OtherWayLogInActivity.this , e.getMessage() , Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });


    }


}
