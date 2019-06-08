package com.example.sentrkk.communityapp.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.sentrkk.communityapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {

    private EditText loginMail, loginPassword ;
    private Button loginButton, regButton, forgetButton;
    private ProgressBar loginProgress;
    private FirebaseAuth mAuth;
    private Intent HomeActivity;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("CommunityApp");

        loginMail = findViewById(R.id.loginMail);
        loginPassword = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
        forgetButton = findViewById(R.id.forgetButton);
        loginProgress = findViewById(R.id.loginProgress);
        mAuth = FirebaseAuth.getInstance(); //firebaseden register olanların bilgileri check etmek icin tanımlamamız gerek
        HomeActivity = new Intent(this,com.example.sentrkk.communityapp.Activities.Home.class);
        regButton = findViewById(R.id.regButton);
        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerActivity = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(registerActivity);
                finish();
            }
        });

        forgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent forgetPasswordActivity = new Intent(getApplicationContext(), ForgetPasswordActivity.class);
                startActivity(forgetPasswordActivity);
            }
        });

        loginProgress.setVisibility(View.INVISIBLE);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginProgress.setVisibility(View.INVISIBLE);
                loginButton.setVisibility(View.INVISIBLE);

                final String mail = loginMail.getText().toString();
                final String password = loginPassword.getText().toString();

                if (mail.isEmpty() || password.isEmpty()){
                    showMessage ("Please verify all field.");
                    loginButton.setVisibility(View.VISIBLE);
                    loginProgress.setVisibility(View.INVISIBLE);

                }
                else
                {

                    signIn(mail,password);
                    loginProgress.setVisibility(View.INVISIBLE);
                    loginButton.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    //ekranda herhangi bi yere dokunulduğunda klavye gidiyor
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }

    @Override
    protected void onStart() {  //eğer user daha önce giriş yaptıysa otomatik olarak homePage'e gider
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null){

            nextPage();

        }

        super.onStart();
    }

    private void signIn(String mail, String password) {

        mAuth.signInWithEmailAndPassword(mail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){
                    loginProgress.setVisibility(View.INVISIBLE);
                    loginButton.setVisibility(View.VISIBLE);
                    nextPage();

                }

                else{
                    showMessage(task.getException().getMessage());
                    loginButton.setVisibility(View.VISIBLE);
                    loginProgress.setVisibility(View.INVISIBLE);

                }


            }
        });



    }

    private void nextPage() {

        startActivity(HomeActivity);
        finish();

    }

    private void showMessage(String text) {

        Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
    }
}
