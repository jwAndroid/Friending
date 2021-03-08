package com.example.kodinstagram.Fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kodinstagram.Adapter.UserAdapter;
import com.example.kodinstagram.Model.User;
import com.example.kodinstagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> mUsers;

    FirebaseUser firebaseUser;
    ProgressDialog progressDialog;

    EditText search_bar;

    public SearchFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        search_bar = view.findViewById(R.id.search_bar);

        mUsers = new ArrayList<>();
        /*mUsers 리스트가 어떻게 구현되어있는지만 파악하면 끝. */
        userAdapter = new UserAdapter(getContext() , mUsers , true , false);
        recyclerView.setAdapter(userAdapter);

        readUsers();

        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                searchUsers(charSequence.toString().toLowerCase());  // charSequence 를 감지해서 list에 넣어주는 부분.
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        return view;
    }

    private void searchUsers(final String s){

        Query query = FirebaseDatabase.getInstance()
                .getReference("Users")
                .orderByChild("search") //정렬
                .startAt(s)
                .endAt(s+"\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    assert user != null;
                    if (!user.getId().equals(firebaseUser.getUid())){
                        if (user.getFullname().toLowerCase().contains(s.toLowerCase())
                            || user.getUsername().toLowerCase().contains(s.toLowerCase())){
                            /*위 3가지 조건을 모두 만족시킨다면 해당 user객체를 add 해줄것 */
                            mUsers.add(user);
                        }
                    }
                }
                userAdapter.notifyDataSetChanged(); //데이터가 들어가고 빠질수 있으니 노티파이 진행
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readUsers(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (search_bar.getText().toString().equals("")){
                    //서치바가 공백이라면 , 아무것도 쓰지않았을때 모든 user를 넣어줌.
                    mUsers.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){ //레퍼런스의 하위노드 스냅샷을 모두 가져와서
                        User user = snapshot.getValue(User.class); //user객체 저장
                        assert user != null;
                        if (!user.getId().equals(firebaseUser.getUid())){ //자기 자신을 제외하고나서
//                            if(user.getFullname().equals("최지웅"))
//                            여기서 , 네임이.뭐와 같다면? "x"와 같다면 add . 니까 당연히 x 를 체크박스 변수(getText)로 두고 add 해라
                            mUsers.add(user); // 나머지 유저를 넣어줄것
                        }
                    }
                    userAdapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


}