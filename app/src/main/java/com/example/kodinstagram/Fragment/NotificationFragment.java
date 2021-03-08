package com.example.kodinstagram.Fragment;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kodinstagram.Adapter.NotificationAdapter;
import com.example.kodinstagram.Adapter.UserAdapter;
import com.example.kodinstagram.Model.Notification;
import com.example.kodinstagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class NotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    FirebaseUser firebaseUser;
    DatabaseReference reference;

    ProgressDialog progressDialog;

    public NotificationFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

       View view = inflater.inflate(R.layout.fragment_notification, container, false);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

       //리사이클러뷰 셋팅
       recyclerView = view.findViewById(R.id.recycler_view);
       recyclerView.setHasFixedSize(true);
       LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
       recyclerView.setLayoutManager(linearLayoutManager);
       //어댑터와 , 리스트 셋팅
       notificationList = new ArrayList<>();
       notificationAdapter = new NotificationAdapter(getContext() , notificationList);
       recyclerView.setAdapter(notificationAdapter);

       readNotification();

        return view;
    }

    private void readNotification() {

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Notifications")
                .child(firebaseUser.getUid());
        /* 하위노드는 현재자기자신의 uid로써 레퍼런스 인스턴스객체 생성*/

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notificationList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Notification notification = snapshot.getValue(Notification.class);
                    assert notification != null;
                    if (!notification.getUserid().equals(firebaseUser.getUid())){ //자기자신을 제외한 나머지
                        notificationList.add(notification);
                    }

                }

                //제일 최신것을 상단으로
                Collections.reverse(notificationList);
                notificationAdapter.notifyDataSetChanged();
                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}