package com.fithou.lap.chat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private DatabaseReference mUserDatabase;
    private Query query;
    private FirebaseRecyclerAdapter adapter;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        addControls();
//        addEvents();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mProgressDialog=new ProgressDialog(this);
        mProgressDialog.setTitle("Please wait..");
        mProgressDialog.setMessage("Load all User");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
        query = FirebaseDatabase.getInstance().getReference().child("Users").limitToLast(50);
    }

    @Override
    protected void onStart() {

        super.onStart();
        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(mUserDatabase, Users.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Users, UsersHolder>(options) {
            @Override
            public UsersHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_single_layout, parent, false);

                return new UsersHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UsersHolder holder, int position, @NonNull Users model) {
                holder.setStatus(model.getStatus());
                holder.setName(model.getName());
                holder.setUserImage(model.getImage(),getApplicationContext());
                mProgressDialog.dismiss();

                final String user_id= String.valueOf(getRef(position).getKey());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent=new Intent(UsersActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);
                        startActivity(profileIntent);

                    }
                });

            }
        };
        mUsersList.setAdapter(adapter);
        adapter.startListening();



    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();

    }

    public class UsersHolder extends RecyclerView.ViewHolder {

        View mView;

        UsersHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        private void setUserImage(String thumb_image, Context context){
            CircleImageView userImageView=mView.findViewById(R.id.users_single_image);
            Picasso.with(context).load(thumb_image).placeholder(R.drawable.man).into(userImageView);
        }
        private void setName(String name) {
            TextView userNameView = mView.findViewById(R.id.users_single_name);
            userNameView.setText(name);
        }

        private void setStatus(String status) {
            TextView userStatus = mView.findViewById(R.id.users_single_status);
            userStatus.setText(status);
        }

    }

    private void addControls() {
        mToolbar = findViewById(R.id.users_appBar);
        mUsersList = findViewById(R.id.users_list);
    }

}
