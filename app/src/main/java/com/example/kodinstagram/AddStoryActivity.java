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
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

public class AddStoryActivity extends AppCompatActivity {

    private static final String TAG = "AddStoryActivity";
    private static final int PERMISSION_FILE = 23;
    private static final int ACCESS_FILE = 43;
    private Uri mImageUri;
    String myUrl ="";
    private StorageTask storageTask;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);

        storageReference = FirebaseStorage.getInstance().getReference("story");

        if (ContextCompat.checkSelfPermission(AddStoryActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(AddStoryActivity.this ,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE} , PERMISSION_FILE );

        }else{
            cropPer();
        }

    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void cropPer(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent , "Pilih gambar")  ,  ACCESS_FILE );
    }

    private void publishStory(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Posting...");
        pd.show();

        if (mImageUri != null){

            try {
                final StorageReference imageReference = storageReference.child(System.currentTimeMillis()
                +"."+getFileExtension(mImageUri));

                storageTask = imageReference.putFile(mImageUri);
                storageTask.continueWithTask(new Continuation() {
                    @Override
                    public Task<Uri> then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }
                        return imageReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri downloadUri = task.getResult();
                            myUrl = downloadUri.toString();

                            String myid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                                    .child(myid);//story setValue

                            String storyid = reference.push().getKey(); // push -- > create key
                            long timeend = System.currentTimeMillis()+86400000; // 1day

                            HashMap<String , Object> hashMap = new HashMap<>();
                            hashMap.put("imageurl" , myUrl);
                            hashMap.put("timestart" , ServerValue.TIMESTAMP);
                            hashMap.put("timeend" , timeend);
                            hashMap.put("storyid" , storyid);
                            hashMap.put("userid" , myid);

                            assert storyid != null;
                            reference.child(storyid).setValue(hashMap);
                            pd.dismiss();
                            finish();

                        }else{
                            Toast.makeText(AddStoryActivity.this , "Failed!" , Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddStoryActivity.this , e.getMessage() , Toast.LENGTH_SHORT).show();
                    }
                });

            }catch (Exception e){
                Log.d(TAG , e.getMessage());
            }

        }else{
            Toast.makeText(AddStoryActivity.this , "No image Selected!" , Toast.LENGTH_SHORT).show();
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
                    .setAspectRatio(9,16)
                    .start(this);
        }

        //main으로 넘어간 data
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                assert result != null;
                mImageUri = result.getUri();
                publishStory();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                assert result != null;
                Exception error = result.getError();
                Log.d("TAG" , Objects.requireNonNull(error.getMessage()));
                Toast.makeText(AddStoryActivity.this , "Someting gone wrong!" , Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AddStoryActivity.this , MainActivity.class));
                finish();
            }
        }
    }
}