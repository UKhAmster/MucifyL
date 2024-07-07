package com.example.musifyl;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;

import com.example.musifyl.databinding.ActivityNavigationDrawerBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

public class NavigationDrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    ///Binding///
    private ActivityNavigationDrawerBinding binding;

    ///Firebase///
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    DatabaseReference reference;

    ///MediaPlayer///
    MediaPlayer mediaPlayer;
    boolean isPlaying = false;
    String loadedMusicID;

    ImageView prevButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNavigationDrawerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child("Music");
        mediaPlayer = new MediaPlayer();

        setSupportActionBar(binding.toolbar);
        binding.navView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar,
                R.string.open_navdrawer, R.string.close_navdrawer);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            binding.navView.setCheckedItem(R.id.nav_home);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_favorite){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FavouriteFragment()).commit();
        } else if (item.getItemId() == R.id.nav_home){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        } else if (item.getItemId() == R.id.nav_settings) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
        } else if (item.getItemId() == R.id.nav_logout) {
            firebaseAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void PlayPause(View view){
        Log.d("MSG", (view.getLayoutParams().toString()));
        if (prevButton != null){
            prevButton.setImageDrawable(getResources().getDrawable(R.drawable.icon_play));
        }
        ConstraintLayout parent = (ConstraintLayout) view;
        TextView idTV = (TextView) ((ConstraintLayout) view).getChildAt(0);
        LinearLayout ll = (LinearLayout) parent.getChildAt(1);
        ImageView playButton = (ImageView) ll.getChildAt(0);
        prevButton = playButton;
        String musicID = idTV.getText().toString();
        Log.d("MSG", "MusicID "+musicID);
        Log.d("MSG", "loadedMusicID "+loadedMusicID);
        if (musicID.equals(loadedMusicID)){
            if (isPlaying){
                mediaPlayer.pause();
                playButton.setImageDrawable(getResources().getDrawable(R.drawable.icon_play));
            }else{
                mediaPlayer.start();
                playButton.setImageDrawable(getResources().getDrawable(R.drawable.icon_pause));
            }
            isPlaying = !isPlaying;
        }else {
            mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            getMusicSource(musicID, new DataFoundSuccessListener() {
                @Override
                public void onSuccess(String url) {
                    if (url != null){
                        try {
                            loadedMusicID = musicID;
                            isPlaying = true;
                            mediaPlayer.setDataSource(url);
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            playButton.setImageDrawable(getResources().getDrawable(R.drawable.icon_pause));
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public void getMusicSource(String id, DataFoundSuccessListener listener){

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot music : snapshot.getChildren()){
                    if (music.getKey().equals(id)){
                        listener.onSuccess(music.child("source").getValue(String.class));
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void FavEdit(View view) {

        ConstraintLayout parent = (ConstraintLayout) view.getParent().getParent();
        TextView id = (TextView) parent.getChildAt(0);

        ImageButton button = (ImageButton) view;
        if (button.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.icon_fav).getConstantState())) {
            button.setImageDrawable(getResources().getDrawable(R.drawable.ic_unlike));
            DeleteFromFavourites(id.getText().toString());
            Log.d("MSG", "Удаление рецепта");
        } else {
            button.setImageDrawable(getResources().getDrawable(R.drawable.icon_fav));
            AddToFavourite(id.getText().toString());
            Log.d("MSG", "Добавление рецепта");

        }
    }
    private void AddToFavourite(String id) {
        DatabaseReference userRef = database.getReference().child("Users").child(firebaseAuth.getCurrentUser().getUid()).child("fav_music");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long itemCount = snapshot.getChildrenCount();
                userRef.child(String.valueOf(itemCount)).setValue(id);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void DeleteFromFavourites(String id) {
        DatabaseReference userRef = database.getReference().child("Users").child(firebaseAuth.getCurrentUser().getUid()).child("fav_music");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot item : snapshot.getChildren()){
                    if (item.getValue().toString().equals(id)){
                        userRef.child(item.getKey()).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    public void BackSett(View view){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
    }
    public void FavBack(View view){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
    }
}
