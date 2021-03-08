package com.example.kodinstagram.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kodinstagram.Fragment.ProfileFragment;
import com.example.kodinstagram.MainActivity;
import com.example.kodinstagram.Model.Comment;
import com.example.kodinstagram.Model.User;
import com.example.kodinstagram.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>{

    private static final String TAG = "CommentAdapter";
    private Context mContext;
    private List<Comment> mComment;
    private String postid;

    private FirebaseUser firebaseUser;

    public CommentAdapter(Context mContext, List<Comment> mComment , String postid) {
        this.mContext = mContext;
        this.mComment = mComment;
        this.postid = postid;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comments_item , viewGroup ,false);
        return new CommentAdapter.ViewHolder(view);
    }

    //ex ) onCreate
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Comment comment = mComment.get(position);
        viewHolder.comment.setText(comment.getComment());
        getUserInfo(viewHolder.image_profile , viewHolder.username , comment.getPublisher());

        /*코맨츠 or 프로필이미지 클릭시 아이디값 넘겨서 인텐트 진행 */
        viewHolder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext , MainActivity.class);
                intent.putExtra("publisherid" , comment.getPublisher());
                mContext.startActivity(intent);
            }
        });

        viewHolder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext , MainActivity.class);
                intent.putExtra("publisherid" , comment.getPublisher());
                mContext.startActivity(intent);
            }
        });

        /* setOnLongClickListener > db 삭제요청 */
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                /*당연히 자기 자신일때만 진행해야 맞다.*/
                if (comment.getPublisher().equals(firebaseUser.getUid())){
                    AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                    alertDialog.setTitle("삭제하시겠습니까?");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "No",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    dialogInterface.dismiss();
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    FirebaseDatabase.getInstance().getReference("Comments")
                                            .child(postid)
                                            .child(comment.getCommentid())
                                            .removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        Toast.makeText(mContext , "Comments delete!" , Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                            dialogInterface.dismiss();
                                        }
                                    });
                    alertDialog.show();
                }

                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mComment.size();
    }

    //전역
    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView image_profile;
        public TextView username,comment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);


        }

    }

    private void getUserInfo(final ImageView imageView , final TextView username , String publisherid){

        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(publisherid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                try{
                    //Users 쪽에 publiserid 를 파라메터로 던져서 db에 추가 -- > 파라메터를 가지고 User 모델의 해당하는 각 객체의 데이터를 입력
                    assert user != null;
                    Picasso.get().load(user.getImageurl()).into(imageView);
                    username.setText(user.getUsername());

                }catch (Exception e){
                    Log.d(TAG , Objects.requireNonNull(e.getMessage()));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}
