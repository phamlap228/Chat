package com.fithou.lap.chat;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private ImageView mProfileImage;
    private Button mProfileSendReqBtn, mProfileDeclareReqBtn;

    private ProgressDialog mProgressDialog;

    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;

    private FirebaseUser mCureentUser;

    private String mCurrent_state;

    private String user_id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        addControls();

        user_id = getIntent().getStringExtra("user_id");
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id+"");
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mCureentUser = FirebaseAuth.getInstance().getCurrentUser();
        addEvents();

        mCurrent_state = "not_friends";
        mProfileDeclareReqBtn.setVisibility(View.INVISIBLE);
        //dialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Load User data");
        mProgressDialog.setMessage("Please wait we load profile");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }

    private void addEvents() {
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = (String) dataSnapshot.child("name").getValue();
                String display_status = (String) dataSnapshot.child("status").getValue();
                String display_image = (String) dataSnapshot.child("image").getValue();
                mProfileName.setText(display_name);
                mProfileStatus.setText(display_status);

                Picasso.with(ProfileActivity.this).load(display_image).placeholder(R.drawable.ic_man_user).into(mProfileImage);

                //-----------Friends list / request feature
                mFriendReqDatabase.child(mCureentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id+"")) {
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if (req_type.equals("received")) {
                                mCurrent_state = "req_received";
                                mProfileSendReqBtn.setText("ACCEPT FRIEND REQUEST");
                                mProfileDeclareReqBtn.setVisibility(View.VISIBLE);
                                mProfileDeclareReqBtn.setEnabled(true);
                            } else if (req_type.equals("sent")) {

                                mCurrent_state = "req_sent";
                                mProfileSendReqBtn.setText("CANCEL FRIEND REQUES");

                                mProfileDeclareReqBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclareReqBtn.setEnabled(false);

                            }

                            mProgressDialog.dismiss();
                        } else {
                            mFriendDatabase.child(mCureentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)) {
                                        mCurrent_state = "friends";
                                        mProfileSendReqBtn.setText(" UNFRIEND THIS PERSON");

                                        mProfileDeclareReqBtn.setVisibility(View.INVISIBLE);
                                        mProfileDeclareReqBtn.setEnabled(false);
                                    }

                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {


                                    mProgressDialog.dismiss();
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProfileSendReqBtn.setEnabled(false);

                //---------------------not friend state------------
                if (mCurrent_state.equals("not_friends")) {
                    mFriendReqDatabase.child(mCureentUser.getUid()).child(user_id).child("request_type").setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mFriendReqDatabase.child(user_id).child(mCureentUser.getUid()).child("request_type")
                                                .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                HashMap<String, String> notificationData = new HashMap<>();
                                                notificationData.put("from", mCureentUser.getUid());
                                                notificationData.put("type", "request");
                                                mNotificationDatabase.child(user_id).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        //
                                                        mProfileSendReqBtn.setEnabled(true);
                                                        mCurrent_state = "req_sent";
                                                        mProfileSendReqBtn.setText("CANCEL FRIEND REQUEST");

                                                        mProfileDeclareReqBtn.setVisibility(View.INVISIBLE);
                                                        mProfileDeclareReqBtn.setEnabled(false);
                                                    }
                                                });


//                                        Toast.makeText(ProfileActivity.this, "Request sent successfully!", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    } else {
                                        Toast.makeText(ProfileActivity.this, "Failed sending request friends", Toast.LENGTH_SHORT).show();
                                    }
                                    mProfileSendReqBtn.setEnabled(true);
                                }
                            });
                }
                //Cancel request
                if (mCurrent_state.equals("req_sent")) {
                    mFriendReqDatabase.child(mCureentUser.getUid()).child(user_id).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    mFriendReqDatabase.child(user_id).child(mCureentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mProfileSendReqBtn.setEnabled(true);
                                            mCurrent_state = "not_friends";
                                            mProfileSendReqBtn.setText("SEND FRIEND REQUEST");

                                            mProfileDeclareReqBtn.setVisibility(View.INVISIBLE);
                                            mProfileDeclareReqBtn.setEnabled(false);
                                        }
                                    });
                                }
                            });
                }
                //-------------Request received state------------
                if (mCurrent_state.equals("req_received")) {

                    final String currenDate = DateFormat.getDateInstance().format(new Date());
                    mFriendDatabase.child(mCureentUser.getUid()).child(user_id).setValue(currenDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendDatabase.child(user_id).child(mCureentUser.getUid()).setValue(currenDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mFriendReqDatabase.child(mCureentUser.getUid()).child(user_id).removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    mFriendReqDatabase.child(user_id).child(mCureentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mProfileSendReqBtn.setEnabled(true);
                                                            mCurrent_state = "friends";
                                                            mProfileSendReqBtn.setText(" UNFRIEND THIS PERSON");
                                                            mProfileDeclareReqBtn.setVisibility(View.INVISIBLE);
                                                            mProfileDeclareReqBtn.setEnabled(false);
                                                        }
                                                    });
                                                }
                                            });
                                }
                            });
                        }
                    });

                }
            }
        });
    }

    private void addControls() {
        mProfileName = findViewById(R.id.profile_displayName);
        mProfileStatus = findViewById(R.id.profile_status);
        mProfileFriendsCount = findViewById(R.id.profile_total_friends);
        mProfileImage = findViewById(R.id.profile_image);
        mProfileSendReqBtn = findViewById(R.id.profile_send_req_btn);
        mProfileDeclareReqBtn = findViewById(R.id.profile_decline_req_btn);
    }
}
