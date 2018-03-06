package com.fithou.lap.chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private EditText mEdtDisplayName, mEdtEmail, mEdtPassword;
    private Button mCreateAcc;
    //fire base auth
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ProgressDialog mProgressDialog;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        addControl();
        addEvent();

        mAuth = FirebaseAuth.getInstance();

        setSupportActionBar(mToolbar);
        String sCreatAcc = "Creat Account";
        getSupportActionBar().setTitle(sCreatAcc);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mProgressDialog = new ProgressDialog(this);
    }

    private void addControl() {
        mCreateAcc = findViewById(R.id.btn_create_reg);
        mEdtDisplayName = findViewById(R.id.edt_display_name);
        mEdtEmail = findViewById(R.id.edt_email);
        mEdtPassword = findViewById(R.id.edt_password);
        mToolbar = findViewById(R.id.reg_toolbar);
    }

    private void addEvent() {
        mCreateAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sDisplayName = mEdtDisplayName.getText().toString();
                String sEmail = mEdtEmail.getText().toString();
                String sPassword = mEdtPassword.getText().toString();

                if (!TextUtils.isEmpty(sDisplayName) || !TextUtils.isEmpty(sEmail) || !TextUtils.isEmpty(sPassword)) {
                    mProgressDialog.setTitle("Registering User");
                    mProgressDialog.setMessage("Please wait while we create your account!");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();
                    registerUser(sDisplayName, sEmail, sPassword);
                }
                registerUser(sDisplayName, sEmail, sPassword);
            }
        });

    }

    private void registerUser(final String sDisplayName, String sEmail, String sPassword) {
        mAuth.createUserWithEmailAndPassword(sEmail, sPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String sUid = currentUser.getUid();

                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(sUid);

                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put("name", sDisplayName);
                    userMap.put("status", "Hi there I'm using App chat");
                    userMap.put("image", "default");
                    userMap.put("thumb_image", "default");

                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mProgressDialog.dismiss();
                                Intent mainInten = new Intent(RegisterActivity.this, MainActivity.class);
                                mainInten.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainInten);
                                finish();
                            }
                        }
                    });

                } else {
                    mProgressDialog.hide();
                    Toast.makeText(RegisterActivity.this, "Cannot sign in, " +
                            "please check form and try again2", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
