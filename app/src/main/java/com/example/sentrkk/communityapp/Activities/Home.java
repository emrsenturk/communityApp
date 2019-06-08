package com.example.sentrkk.communityapp.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


//all library and db part in here
import com.bumptech.glide.Glide;
import com.example.sentrkk.communityapp.Fragments.HomeFragment;
import com.example.sentrkk.communityapp.Fragments.ProfileFragment;
import com.example.sentrkk.communityapp.Fragments.SettingsFragment;
import com.example.sentrkk.communityapp.Models.Post;
import com.example.sentrkk.communityapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

        //add comment
    private static final int PReqCode = 2;
    private static final int REQUESCODE = 2;
    FirebaseAuth mAuth;
        FirebaseUser currentUser ;
        Dialog popAddPost;
        ImageView popupUserImage,popupAddBtn,popupPostImage;
        TextView popupTitle,popupDescription;
        ProgressBar popupClickProgress;
        private Uri pickedImgUri = null ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializePopup();
        setupPopupImage();


        //initialize all attributes

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popAddPost.show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        updateNavHeader();


        //home fragment ı sabitliyoruz burada

        getSupportFragmentManager().beginTransaction().replace(R.id.container,new HomeFragment()).commit();
    }

    private void setupPopupImage() {

        popupPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //galeriyi açmak için

                checkAndRequestForPermission();



            }
        });
    }

    private void initializePopup() {
        popAddPost = new Dialog(this);
        popAddPost.setContentView(R.layout.popup_add_post);
        popAddPost.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popAddPost.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT,Toolbar.LayoutParams.WRAP_CONTENT);
        popAddPost.getWindow().getAttributes().gravity = Gravity.TOP;

        // ini popup widgets
        popupUserImage = popAddPost.findViewById(R.id.popup_user_image);
        popupPostImage = popAddPost.findViewById(R.id.popup_img);
        popupTitle = popAddPost.findViewById(R.id.popup_title);
        popupDescription = popAddPost.findViewById(R.id.popup_description);
        popupAddBtn = popAddPost.findViewById(R.id.popup_add);
        popupClickProgress = popAddPost.findViewById(R.id.popup_progressBar);




        //post paylaşıldıktan sonraki process için clickListener ekleniyor

        popupAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupAddBtn.setVisibility(View.INVISIBLE);
                popupClickProgress.setVisibility(View.VISIBLE);


                if (!popupTitle.getText().toString().isEmpty()
                        && !popupDescription.getText().toString().isEmpty()
                        && pickedImgUri != null ) {

                //Firebasede post için obje oluşturuyor

                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("blog images");
                    final StorageReference imageFilePath = storageReference.child(pickedImgUri.getLastPathSegment());
                    imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageDownlaodLink = uri.toString();
                                    // create post Object
                                    Post post = new Post(popupTitle.getText().toString(),
                                            popupDescription.getText().toString(),
                                            imageDownlaodLink,
                                            currentUser.getUid(),
                                            currentUser.getPhotoUrl().toString());

                                    // Add post to firebase database

                                    addPost(post);



                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    ShowMessage(e.getMessage());
                                    popupClickProgress.setVisibility(View.INVISIBLE);
                                    popupAddBtn.setVisibility(View.VISIBLE);



                                }
                            });


                        }
                    });


                }

                else {

                    ShowMessage ("Please verify all field and choose a post Image");
                    popupAddBtn.setVisibility(View.VISIBLE);
                    popupClickProgress.setVisibility(View.INVISIBLE);
                }







            }
        });


    }

    private void addPost(Post post) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Post").push();

        //unique id ver
        String key = myRef.getKey();
        post.setPostKey(key);

        myRef.setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ShowMessage("Post added successfully");
                popupClickProgress.setVisibility(View.INVISIBLE);
                popupAddBtn.setVisibility(View.VISIBLE);
                popAddPost.dismiss();
            }
        });



    }

    private void ShowMessage(String message) {

        Toast.makeText(Home.this,message,Toast.LENGTH_LONG).show();
    }

        //kullanıcı fotoyu seçtiğinde buradan işlem devam ediyor

    private void checkAndRequestForPermission() {


        if (ContextCompat.checkSelfPermission(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Toast.makeText(Home.this,"Please accept for required permission",Toast.LENGTH_SHORT).show();

            }

            else
            {
                //izinleri okuyor okey mi değil mi diye

                ActivityCompat.requestPermissions(Home.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PReqCode);
            }

        }
        else
            openGallery();

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data != null ) {

            // the user has successfully picked an image
            // we need to save its reference to a Uri variable yani databaseden çekmesini sağlamamız lazım oke
            pickedImgUri = data.getData() ;
            popupPostImage.setImageURI(pickedImgUri);

            //kullanıcı fotoyu seçtiğinde buradan işlem devam ediyor

        }


    }


    private void openGallery() {

        //open gallery intent and wait for user pick and image

        Intent galleryIntent = new Intent (Intent.ACTION_GET_CONTENT);
        galleryIntent.setType( "image/*");
        startActivityForResult(galleryIntent,REQUESCODE);

    }
    



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            //yönlendirme yapılan yer atıyorum home a yolla tıklayınca


            getSupportActionBar().setTitle("Home");
            getSupportFragmentManager().beginTransaction().replace(R.id.container,new HomeFragment()).commit();

        } else if (id == R.id.nav_profile) {

            getSupportActionBar().setTitle("Profile");
            getSupportFragmentManager().beginTransaction().replace(R.id.container,new ProfileFragment()).commit();

        } else if (id == R.id.nav_settings) {

            getSupportActionBar().setTitle("Setting");
            getSupportFragmentManager().beginTransaction().replace(R.id.container,new SettingsFragment()).commit();

        } else if (id == R.id.nav_logout) {

            FirebaseAuth.getInstance().signOut();
            Intent loginActivity = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(loginActivity);
            finish();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void updateNavHeader(){

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUserName = headerView.findViewById(R.id.navUserName);
        TextView navUserMail = headerView.findViewById(R.id.navUserMail);
        ImageView navUserPhoto = headerView.findViewById(R.id.navUserPhoto);

        navUserMail.setText(currentUser.getEmail()); // kullanıcının maili çektik
        navUserName.setText(currentUser.getDisplayName());

        //fotoyu yükleyeceğiz glidelibrary ile, github kısmından

        Glide.with(this).load(currentUser.getPhotoUrl()).into(navUserPhoto);

    }
}
