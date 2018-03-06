package com.fithou.lap.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private SectionPagerAdapter mSectionPagerAdapter;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        addControl();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Lapit Chat");

        //Tabs
        mSectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void addControl() {
        mViewPager = findViewById(R.id.main_tab_pager);
        mToolbar = findViewById(R.id.main_page_toolbar);
        mTabLayout = findViewById(R.id.main_tabs);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);
        if (currentUser == null) {
            sendToStart();
        }
    }

    private void sendToStart() {

        Intent start_intent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(start_intent);
        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.main_btn_acc_setting:
                Intent settingIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingIntent);
                break;
            case R.id.main_btn_all_user:
                Intent usersIntent=new Intent(MainActivity.this,UsersActivity.class);
                startActivity(usersIntent);
                break;
            case R.id.main_btn_log_out:
                FirebaseAuth.getInstance().signOut();
                sendToStart();
                break;
            case R.id.main_btn_exit:
                break;
        }
        return true;
    }
}
