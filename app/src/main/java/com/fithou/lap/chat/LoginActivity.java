package com.fithou.lap.chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
//import com.google.firebase.authe.AuthResult;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {
    private EditText mEdtDisplayName, mEdtEmail, mEdtPassword;
    private Button mBtnLogin;
    //fire base auth
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ProgressDialog mProgressDialog;
    private DatabaseReference mUserDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        addControl();
        addEvent();

        mAuth = FirebaseAuth.getInstance();

        setSupportActionBar(mToolbar);
        String sCreatAcc = "Create Account";
        getSupportActionBar().setTitle(sCreatAcc);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressDialog = new ProgressDialog(this);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    private void addControl() {
        mEdtEmail = findViewById(R.id.login_edt_email);
        mEdtPassword = findViewById(R.id.login_edt_password);
        mBtnLogin = findViewById(R.id.btn_login);
        mToolbar = findViewById(R.id.login_toolbar);

    }

    private void addEvent() {
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sEmail = mEdtEmail.getText().toString();
                String sPass = mEdtPassword.getText().toString();
                if (!sEmail.isEmpty() || !sPass.isEmpty()) {
                    mProgressDialog.setTitle("Logging In");
                    mProgressDialog.setMessage("Please wait while we check your credentials");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();
                    loginUser(sEmail, sPass);
                }
            }
        });

    }

    private void loginUser(String sEmail, String sPass) {
        mAuth.signInWithEmailAndPassword(sEmail, sPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    mProgressDialog.dismiss();
                    String current_user_id = mAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    mUserDatabase.child(current_user_id).child("device_token").setValue(deviceToken).addOnSuccessListener
                             
                            (new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Intent mainInten = new Intent(LoginActivity.this, MainActivity.class);
                                    mainInten.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainInten);
                                    finish();
                                }
                            });


                } else {
                    mProgressDialog.hide();
                    Toast.makeText(LoginActivity.this, "Login fail, please check account or your network", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
