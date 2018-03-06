package com.fithou.lap.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {
    private Button mBtnReg, mBtnSigup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        addControl();
        addEvent();
    }

    private void addEvent() {
        mBtnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent register_intent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(register_intent);
            }
        });
        mBtnSigup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent register_intent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(register_intent);
            }
        });
    }

    private void addControl() {
        mBtnSigup = findViewById(R.id.start_btn_login);
        mBtnReg = findViewById(R.id.start_btn_register);
    }
}
