package com.example.sentrkk.communityapp.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.sentrkk.communityapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {



    ImageView ImgUserPhoto;
    static int PReqCode= 1;
    static int REQUESCODE= 1;
    Uri pickedImgUri;


    private EditText userEmail , userPassword, getUserPassword2 , userName ;
    private ProgressBar loadingProgress;
    private Button regBtn;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("CommunityApp");


        //initialize views id lerini topluyoz burda
        userEmail = findViewById(R.id.regMail);
        userPassword = findViewById(R.id.regPassword);
        getUserPassword2=findViewById(R.id.regPassword2);
        userName = findViewById(R.id.regName);
        loadingProgress = findViewById(R.id.regProgressBar);
        regBtn = findViewById(R.id.regBtn);
        loadingProgress.setVisibility(View.INVISIBLE);


        mAuth = FirebaseAuth.getInstance(); // bu kısım anlık bilgileri çeksin diye oluşturulmustur

        //Register butonu için onClick ekliyoruz tıklayınca calıssın dıye

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                regBtn.setVisibility(View.INVISIBLE);
                loadingProgress.setVisibility(View.VISIBLE);
                final String email = userEmail.getText().toString();
                final String password = userPassword.getText().toString();
                final String password2 = getUserPassword2.getText().toString();
                final String name = userName.getText().toString();

                if (!email.isEmpty() && !name.isEmpty() && !password.isEmpty() && password.equals(password2) && pickedImgUri != null  ){

                    CreateUserAccount(email, name , password);

                }

                else{
                    //her şey tamam sıkıntı yok

                    loadingProgress.setVisibility(View.INVISIBLE);
                    regBtn.setVisibility(View.VISIBLE);
                    showMessage("Tüm alanları giriniz");

                }
            }
        });

        ImgUserPhoto = findViewById(R.id.regUserPhoto);
        ImgUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //android Sdk versiyon kontrolü yapıyruz
                if(Build.VERSION.SDK_INT >= 22) {

                    checkAndRequestForPermission();

                }
                else {

                    openGallery ();
                }




            }
        });
    }

    private void CreateUserAccount(String email, final String name, String password) {
//bu method kullanıcı yaratmak için password ve email ile

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            //user account created okay
                            showMessage("Hesap oluşturuldu");

                            updateUserInfo(name,pickedImgUri,mAuth.getCurrentUser());

                        }
                        else{
                            //bi sıkıntı var diyip sıfırlıyoruz bilgileri

                            showMessage("Hesap oluşturulma hatalı" );
                            regBtn.setVisibility(View.VISIBLE);
                            loadingProgress.setVisibility(View.INVISIBLE);




                        }
                    }
                });

    }


    //kullanıcı foto ve ismini güncelleyeceğin yer
    private void updateUserInfo(final String name, Uri pickedImgUri, final FirebaseUser currentUser) {
        //öncelikle firebase storage kısmına ulaşcaz url yi çekip

        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users photos"); //burası Storagedaki kısımıla aynı isimde olmak zorunda
        final StorageReference imageFilePath = mStorage.child(pickedImgUri.getLastPathSegment());
        imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //foto alındığını gösteren yer burası
                //şimdi fotonun url kısmını alıcaz

                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        //uri contain Kullanıcı fotoları oke
                        UserProfileChangeRequest profilUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .setPhotoUri(uri)
                                .build();

                        currentUser.updateProfile(profilUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            //kullanıcı infosu okey
                                            showMessage("Kayıt oluşturuldu");
                                            //yeni sayfa için test
                                            updateUI();


                                        }
                                    }
                                });

                    }
                });
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

    private void updateUI() {
//burası ıkıncı sayfayı calıstırmayı saglıyor yani update ettikten sonra

        Intent HomeActivity = new Intent(getApplicationContext(), Home.class);
        startActivity(HomeActivity);
        finish();
    }


    //method for mesaj gösterme
    private void showMessage(String message) {

        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();

    }


    private void openGallery() {

        //open gallery intent and wait for user pick and image

        Intent galleryIntent = new Intent (Intent.ACTION_GET_CONTENT);
        galleryIntent.setType( "image/*");
        startActivityForResult(galleryIntent,REQUESCODE);

    }


    private void checkAndRequestForPermission() {


        if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Toast.makeText(RegisterActivity.this,"Please accept for required permission",Toast.LENGTH_SHORT).show();

            }

            else
            {
                //izinleri okuyor okey mi değil mi diye

                ActivityCompat.requestPermissions(RegisterActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PReqCode);
            }

        }
        else
            openGallery();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data != null ) {

            // the user has successfully picked an image
            // we need to save its reference to a Uri variable yani databaseden çekmesini sağlamamız lazım oke
            pickedImgUri = data.getData() ;
            ImgUserPhoto.setImageURI(pickedImgUri);


        }


    }





}
