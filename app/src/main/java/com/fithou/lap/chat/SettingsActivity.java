package com.fithou.lap.chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurentUser;
    private TextView mTvName, mTvStatus;
    private CircleImageView mCircleDisplayImageView;
    private Button mBtnChooseImg, mBtnStatus;
    private static final int GALLERY_PICK = 1;

    //storage firabase
    private StorageReference mImageStorage;
    //dialog
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        addControl();

        mImageStorage = FirebaseStorage.getInstance().getReference();
        mCurentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.keepSynced(true);

        addEvent();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Load User data");
        mProgressDialog.setMessage("Please wait we load profile");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }

    private void addControl() {
        mCircleDisplayImageView = findViewById(R.id.settings_img_avatar);
        mTvName = findViewById(R.id.settings_text_display_name);
        mTvStatus = findViewById(R.id.settings_text_status);
        mBtnStatus = findViewById(R.id.settings_status_btn);
        mBtnChooseImg = findViewById(R.id.settings_changimg_btn);
    }

    private void addEvent() {
        mBtnChooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "Choose Image"), GALLERY_PICK);

//                CropImage.activity()
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .start(SettingsActivity.this);

            }
        });
        mBtnStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                startActivity(statusIntent);
            }
        });
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child("name").getValue();
                final String image = (String) dataSnapshot.child("image").getValue();
                String status = (String) dataSnapshot.child("status").getValue();
                String thumb_image = (String) dataSnapshot.child("thumb_image").getValue();
                mTvName.setText(name);
                mTvStatus.setText(status);

                if (!image.equals("default")) {
//                    Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.man).into(mCircleDisplayImageView);

                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.man)
                            .into(mCircleDisplayImageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    //
                                }

                                @Override
                                public void onError() {
                                    Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.man).into(mCircleDisplayImageView);

                                }
                            });


                }
                mProgressDialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("Upload image");
                mProgressDialog.setMessage("Please wait upload image");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                File thumb_filePath = new File(resultUri.getPath());
                String current_user_id = mCurentUser.getUid();
                try {
                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] thumb_byte = baos.toByteArray();
                    StorageReference filePath = mImageStorage.child("profile_image").child(current_user_id + ".jpg");
                    final StorageReference thumb_filepath = mImageStorage.child("profile_image").child("thumbs").child("current_user_id" + ".jpg");

                    filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                final String download_url = task.getResult().getDownloadUrl().toString();

                                UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                        String thumb_download = thumb_task.getResult().getDownloadUrl().toString();

                                        if (thumb_task.isSuccessful()) {

                                            Map update_hashMap = new HashMap();
                                            update_hashMap.put("image", download_url);
                                            update_hashMap.put("thumb_image", thumb_download);
                                            mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        mProgressDialog.dismiss();
                                                        Toast.makeText(SettingsActivity.this, "Successful upload", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        } else {
                                            Toast.makeText(SettingsActivity.this, "faild", Toast.LENGTH_SHORT).show();
                                            mProgressDialog.dismiss();
                                        }


                                    }
                                });


                            } else {
                                Toast.makeText(SettingsActivity.this, "faild", Toast.LENGTH_SHORT).show();
                                mProgressDialog.dismiss();
                            }

                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
