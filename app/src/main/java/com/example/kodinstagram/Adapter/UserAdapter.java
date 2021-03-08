package com.example.kodinstagram.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kodinstagram.Fragment.ProfileFragment;
import com.example.kodinstagram.MainActivity;
import com.example.kodinstagram.MessageActivity;
import com.example.kodinstagram.Model.Chat;
import com.example.kodinstagram.Model.Post;
import com.example.kodinstagram.Model.User;
import com.example.kodinstagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private static final String TAG = "UserAdapter";
    private Context mContext;
    private List<User> mUsers;
    private boolean isfragment;
    private FirebaseUser firebaseUser;
    String theLastMessage;
    private boolean isChat;

    public UserAdapter() {}

    public UserAdapter(Context mContext, List<User> mUsers , boolean isfragment , boolean isChat) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isfragment = isfragment;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item , viewGroup , false);
        return new UserAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final User user = mUsers.get(position);

        viewHolder.btn_follow.setVisibility(View.VISIBLE);
        viewHolder.username.setText(user.getUsername());
        viewHolder.fullname.setText(user.getFullname());

        try {
            Glide.with(mContext)
                    .load(user.getImageurl())
                    .override(120,120)
                    .into(viewHolder.image_profile);

            isFollowing(user.getId() , viewHolder.btn_follow);
            /*userid 로써 레퍼런스 참조하여 팔로우를 했는지 안했는지 판변 */

            if (user.getId().equals(firebaseUser.getUid())){
                viewHolder.btn_follow.setVisibility(View.GONE);
                /*자기자신은 없애준다.*/
            }

        }catch (Exception e){
            Log.d(TAG , Objects.requireNonNull(e.getMessage()));
        }

        if (isChat){
            //채팅 액티비티의 어댑터도 이 어댑터를 쓰기떄문에
            // 어댑터 파라메터로 플래그를 던져서 실행시킬지 정해줌.
            lastMessage(user.getId() , viewHolder.last_msg);
        }else{
            viewHolder.last_msg.setVisibility(View.GONE);
        }

        //TODO : 생성자 파라메터 확인! java.lang.IllegalArgumentException 예외 발생
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*단순히 해당 유저클릭시 isfragment = true 라면 그냥 replace진행  */
                if (isfragment){
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS" , Context.MODE_PRIVATE).edit();
                    editor.putString("profileid" , user.getId());
                    editor.apply();

                    ((FragmentActivity) mContext)
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container , new ProfileFragment())
                            .commit();
                }else{
                    Intent intent = new Intent(mContext , MainActivity.class);
                    intent.putExtra("publisherid" , user.getId());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            }
        });

        //TODO : 롱클릭으로써 채팅구현 상대의 uid를 같이보냄.
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //TODO : WindowManager$BadTokenException WHEN THIS ADAPTER USES ALERTDIALOG
//                Toast.makeText(mContext , user.getUsername()+"Start Chat!" , Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext , MessageActivity.class);
                intent.putExtra("hisUid" , user.getId());
                Log.d(TAG, "상대 uid : "+ user.getId());
                Log.d(TAG, "나 uid : "+ firebaseUser.getUid());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                return true;
            }
        });

        /*버튼 상태 setValue or remove */
        viewHolder.btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (viewHolder.btn_follow.getText().toString().equals("follow")){

                    FirebaseDatabase.getInstance().getReference()
                            .child("Follow")
                            .child(firebaseUser.getUid())
                            .child("following")
                            .child(user.getId())
                            .setValue(true);

                    FirebaseDatabase.getInstance().getReference()
                            .child("Follow")
                            .child(user.getId())
                            .child("followers")
                            .child(firebaseUser.getUid())
                            .setValue(true);

                    addNotification(user.getId());

                }else{

                    FirebaseDatabase.getInstance().getReference()
                            .child("Follow")
                            .child(firebaseUser.getUid())
                            .child("following")
                            .child(user.getId())
                            .removeValue();

                    FirebaseDatabase.getInstance().getReference()
                            .child("Follow")
                            .child(user.getId())
                            .child("followers")
                            .child(firebaseUser.getUid())
                            .removeValue();

                }

            }
        });


    }

    //팔로우시에 나타내는 addNotification
    private void addNotification(String userid){

        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Notifications")
                .child(userid);

        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put("userid" , firebaseUser.getUid());
        hashMap.put("text" , "Started Following you!");
        hashMap.put("postid" , "");
        hashMap.put("ispost" , false);

        reference.push().setValue(hashMap);

    }


    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public TextView fullname;
        public CircleImageView image_profile;
        public Button btn_follow;
        private TextView last_msg;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            fullname = itemView.findViewById(R.id.fullname);
            image_profile = itemView.findViewById(R.id.image_profile);
            btn_follow = itemView.findViewById(R.id.btn_follow);
            last_msg = itemView.findViewById(R.id.last_msg);


        }
    }

    private void lastMessage(final String userid , final TextView last_msg){

        theLastMessage = "default";
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    assert firebaseUser != null;
                    assert chat != null;
                    try{
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)
                                || chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())){

                            theLastMessage = chat.getMessage();
                        }
                    }catch (Exception e){
                        Log.d("TAG" , Objects.requireNonNull(e.getMessage()));
                    }
                }
                switch (theLastMessage){
                    case "default" : last_msg.setText("No Message..");
                        break;
                    default:
                        last_msg.setText("Message:"+""+theLastMessage);
                        break;
                }

                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void isFollowing(final String userid , final Button button){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow")
                .child(firebaseUser.getUid())
                .child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //userid exists?
                if (dataSnapshot.child(userid).exists()){
                    button.setText("following");
                }else{
                    button.setText("follow");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

}
