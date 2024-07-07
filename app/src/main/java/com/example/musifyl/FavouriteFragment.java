package com.example.musifyl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.musifyl.databinding.FragmentFavouriteBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FavouriteFragment extends Fragment {

    private FragmentFavouriteBinding binding;
    private FirebaseDatabase DataBase;
    private DatabaseReference reference;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavouriteBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DataBase = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        reference = DataBase.getReference().child("Users").child(auth.getCurrentUser().getUid()).child("fav_music");
        fillFavMusicList();
    }
    private void fillFavMusicList() {
        binding.favList.removeAllViews();
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot music : snapshot.getChildren()){
                    createItem(music.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void createItem(String id) {
        DatabaseReference musicRef = DataBase.getReference().child("Music").child(id);
        musicRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                View card = getLayoutInflater().inflate(R.layout.music_card, binding.favList, false);
                TextView name = card.findViewById(R.id.music_name);
                TextView id = card.findViewById(R.id.music_id);
                ImageButton favBtn = card.findViewById(R.id.fav_edit);
                favBtn.setImageDrawable(getResources().getDrawable(R.drawable.icon_fav));

                String musicname = snapshot.child("title").getValue().toString();
                String artist = snapshot.child("artist").getValue().toString();

                name.setText(artist + " - " + musicname);
                id.setText(snapshot.getKey());
                binding.favList.addView(card);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
