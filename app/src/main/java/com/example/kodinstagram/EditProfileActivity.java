package com.example.kodinstagram;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.kodinstagram.Model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity" ;
    private static final int PERMISSION_FILE = 23;
    private static final int ACCESS_FILE = 43;
    ImageView close , image_profile;
    TextView save,tv_change;
    EditText fullname , username , bio;

    FirebaseUser firebaseUser;

    private Uri mImageUri;
    private StorageTask uploadTask;
    StorageReference storageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);


        close = findViewById(R.id.close);
        image_profile = findViewById(R.id.image_profile);
        save = findViewById(R.id.save);
        tv_change = findViewById(R.id.tv_change);

        fullname = findViewById(R.id.fullname);
        username = findViewById(R.id.username);
        bio = findViewById(R.id.bio);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //storageRef node create
        storageRef = FirebaseStorage.getInstance().getReference("uploads");

        //로그인된 해당user db접근
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(firebaseUser.getUid());
        //addChange 이벤트 리스너
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                //해당 유저 모델 객체생성

                assert user != null;
                fullname.setText(user.getFullname());
                username.setText(user.getUsername());
                bio.setText(user.getBio());

                try {
                    Glide.with(EditProfileActivity.this).load(user.getImageurl()).override(100,100).into(image_profile);
//                    Picasso.get().load(user.getImageurl()).into(image_profile);
                }catch (Exception e){
                    Log.d(TAG , Objects.requireNonNull(e.getMessage()));
                }

                //현재해당유저를 db에 접근해서 setting 해주는작업  User user = dataSnapshot.getValue(User.class); 를 통해서

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tv_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(EditProfileActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                    ActivityCompat.requestPermissions(EditProfileActivity.this ,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE} , PERMISSION_FILE );

                }else{
                    cropPer();
                }
            }
        });

        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropPer();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateProfile(fullname.getText().toString(), username.getText().toString() , bio.getText().toString());
                finish();

            }
        });

    }//...........onCreate.................

    private void cropPer(){

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent , "Pilih gambar")  ,  ACCESS_FILE );

    }

    private void updateProfile(String fullname, String username, String bio) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(firebaseUser.getUid());

        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put("fullname" , fullname);
        hashMap.put("username" , username);
        hashMap.put("search" , username);
        hashMap.put("bio" , bio);
        reference.updateChildren(hashMap);

    }

    private String getFileExtension(Uri uri){

        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading.....");
        pd.show();

        if (mImageUri != null){
            //storage db 를 System.currentTimeMillis() +"."+ getFileExtension(mImageUri) 형태로 create
            //storage db 에 uid/ System.currentTimeMillis() +"."+ getFileExtension(mImageUri) << 로 upload하기
            final StorageReference filereference = storageRef.child(System.currentTimeMillis()
                    +"."+getFileExtension(mImageUri));

            //filereference db에 mImageUri 넣고 uploadTask 에 저장장
            uploadTask = filereference.putFile(mImageUri);
            //continueWithTask 로 imageUrl Download try!
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }

                    return filereference.getDownloadUrl();
                    //여기까지 url return
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){

                        try {
                            Uri downloadUri = task.getResult();

                            String myUrl = downloadUri.toString();

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                                    .child(firebaseUser.getUid());

                            HashMap<String , Object> hashMap = new HashMap<>();
                            hashMap.put("imageurl" , "" + myUrl);
                            reference.updateChildren(hashMap);
                            pd.dismiss();

                            }catch (Exception e){
                                Log.d(TAG , Objects.requireNonNull(e.getMessage()));
                            }
                    }else{
                        Toast.makeText(EditProfileActivity.this , "Failed! checks Log .. " , Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfileActivity.this , e.getMessage() , Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(EditProfileActivity.this , "No image selected! " , Toast.LENGTH_SHORT).show();
        }

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACCESS_FILE
                && resultCode == Activity.RESULT_OK
                && data != null
                && data.getData() !=null){

            Uri FILE_URI = data.getData();

            //crop activity
            CropImage.activity(FILE_URI)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setFixAspectRatio(true)
                    .start(this);
        }

        //main으로 넘어간 data
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                uploadImage();
                image_profile.setImageURI(mImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d("TAG" , error.getMessage());
            }
        }

    }
}
