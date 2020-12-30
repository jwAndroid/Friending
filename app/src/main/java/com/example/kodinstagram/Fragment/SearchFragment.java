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

    //TODO : 서치해주는쪽에서 예를들어서 이름 : 최지웅 인데 ,
    //TODO : 검색에 지웅만 쳐도 나오게끔해주고 싶은데 안나옴 최지웅 이렇게 정확하게 쳐야함
    //TODO : 참조 : D: socialmediatest 의 Users 프래그먼트 참고 단 이부분 서치바 자체를 변경해야함

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> mUsers;

    FirebaseUser firebaseUser;
    ProgressDialog progressDialog;

    EditText search_bar;


    public SearchFragment() {
    }

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
        userAdapter = new UserAdapter(getContext() , mUsers , true , false);
        recyclerView.setAdapter(userAdapter);

        readUsers();

        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                searchUsers(charSequence.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        return view;
    }

    private void searchUsers(final String s){

        Query query = FirebaseDatabase.getInstance()
                .getReference("Users")
                .orderByChild("search")
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
                            mUsers.add(user);
                        }
                    }
                }
                userAdapter.notifyDataSetChanged();
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
                    mUsers.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        User user = snapshot.getValue(User.class);
                        assert user != null;
                        if (!user.getId().equals(firebaseUser.getUid())){
//                            if(user.getFullname().equals("최지웅"))
//                            여기서 , 네임이.뭐와 같다면? "x"와 같다면 add . 니까 당연히 x 를 체크박스 변수(getText)로 두고 add 해라
                            mUsers.add(user);
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