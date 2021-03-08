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
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

public class PostActivity extends AppCompatActivity {

    private static final String TAG = "PostActivity";
    private static final int PERMISSION_FILE = 23;
    private static final int ACCESS_FILE = 43;
    Uri imageUri;
    String myUrl = "";
    StorageTask uploadTask;
    StorageReference storageReference;

    ImageView close , image_added;
    TextView post;
    EditText description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        close = findViewById(R.id.close);
        image_added = findViewById(R.id.image_added);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);

        storageReference = FirebaseStorage.getInstance().getReference("posts");
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostActivity.this , MainActivity.class));
                finish();
            }
        });

        image_added.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* 직접적으로 WRITE_EXTERNAL_STORAGE 권한 받아줄것 */
                if (ContextCompat.checkSelfPermission(PostActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                    ActivityCompat.requestPermissions(PostActivity.this ,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE} , PERMISSION_FILE );

                }else{
                    /* 이미지 크롭 */
                    cropPer();
                }
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* 사진업로드 */
                if(imageUri != null){
                    uploadImage();
                }else{
                    Log.d(TAG, "imageUri isEmpty: ");
                }

            }
        });

    }// ..........................onCreate..........................................

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
        /* 스토리지 레퍼런스에 접근할때 이렇게 이미지 파일변환을 통해서 진행한다. */
    }

    /* uri 변환후 레퍼런스생성 => db저장 => 메인이동 */
    private void uploadImage() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting...");
        progressDialog.show();

        try {

            if (imageUri != null){

                final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                        +"."+getFileExtension(imageUri));
                /*스토리지 레퍼런스에만 변환해서 진행 레퍼런스 생성 */

                uploadTask = fileReference.putFile(imageUri);
                /* 해당 레퍼런스를 가지고서 이미지 put */


                uploadTask.continueWithTask( new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()){

                            throw Objects.requireNonNull(task.getException());
                        }
                        /* 스토리지 테스크로써 uri를 반환 */
                        return fileReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri downloadUri = task.getResult();
                            myUrl = downloadUri.toString();


                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
                            /*Posts 레퍼런스 */
                            String postid = reference.push().getKey();
                            /*레퍼런스의 key 생성 */

                            HashMap<String , Object > hashMap = new HashMap<>();
                            hashMap.put("postid" , postid); //postId
                            hashMap.put("postimage" , myUrl);
                            hashMap.put("description" , description.getText().toString());
                            hashMap.put("publisher" , FirebaseAuth.getInstance().getCurrentUser().getUid()); //UserID

                            reference.child(postid).setValue(hashMap);
                            /* db에 map set */

                            /* 완료후 MainActivity 로 전환 => 프래그먼트 replace */
                            progressDialog.dismiss();
                            startActivity(new Intent(PostActivity.this , MainActivity.class));
                            finish();

                            //TODO : DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                            //TODO : hashMap.put("postimage" , myUrl); -- > User모델쪽에 post이미지 변수하나 더 만든다음에 어댑터에서 수정

                        }else{
                            Toast.makeText(PostActivity.this , "upload Failed!" , Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PostActivity.this , e.getMessage() , Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            }else{
                Toast.makeText(PostActivity.this , "No image selected!" , Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

        }catch (Exception e){
            Log.d(TAG , Objects.requireNonNull(e.getMessage()));
            progressDialog.dismiss();
        }


    }

    private void cropPer(){
        Intent intent = new Intent();
        intent.setType("image/*"); /* 저장소로 전환후  */
        intent.setAction(Intent.ACTION_PICK); //선택 ==> data를 담아서 intent 전환
        startActivityForResult(Intent.createChooser(intent , "")  ,  ACCESS_FILE );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACCESS_FILE
                && resultCode == Activity.RESULT_OK
                && data != null
                && data.getData() !=null){

            Uri FILE_URI = data.getData(); //Uri로써 초기화
            //crop activity
            CropImage.activity(FILE_URI)
                    .setGuidelines(CropImageView.Guidelines.ON) //가이드라인
                    .setAspectRatio(1,1) //가로세로 비율
                    .setCropMenuCropButtonTitle("완료")
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            /*크롭후 result file 저장 */
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                assert result != null;
                imageUri = result.getUri(); /* 크롭된 이미지 초기화할당 */
                image_added.setImageURI(imageUri); /* 크롭된이미지를 ImageView에 set */

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                assert result != null;
                Exception error = result.getError();
                Log.d("TAG" , Objects.requireNonNull(error.getMessage()));
                startActivity(new Intent(PostActivity.this , MainActivity.class));
                finish();
            }
        }

    }
}