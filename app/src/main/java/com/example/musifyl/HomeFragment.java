package com.example.musifyl;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.service.autofill.FieldClassification;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.musifyl.databinding.FragmentHomeBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private FirebaseDatabase database;
    DatabaseReference reference;

    MediaPlayer mediaPlayer;
    boolean isPlaying = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child("Music");

        binding.searchET.addTextChangedListener(textWatcher);


        fillMusicList();
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String text = binding.searchET.getText().toString();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!text.isEmpty()){
                        showSearchResult(text);
                    }else{
                        fillMusicList();
                    }
                }
            }, 500);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void showSearchResult(String text) {
        String newText = text.replaceAll(" ","").replace("-", "").toLowerCase();
        Pattern pattern = Pattern.compile(newText);
        binding.musicListLL.removeAllViews();
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot music : snapshot.getChildren()){
                    String musicString = music.child("artist").getValue(String.class) + music.child("title").getValue(String.class);
                    musicString = musicString.replaceAll(" ", "").toLowerCase();
                    Log.d("MSG", musicString);
                    Log.d("MSG", newText);
                    Matcher matcher = pattern.matcher(musicString);

                    if (matcher.find()){
                        View card = getLayoutInflater().inflate(R.layout.music_card, binding.musicListLL, false);
                        TextView name = card.findViewById(R.id.music_name);
                        TextView id = card.findViewById(R.id.music_id);

                        String musicname = music.child("title").getValue().toString();
                        String artist = music.child("artist").getValue().toString();

                        name.setText(artist + " - " + musicname);
                        id.setText(music.getKey());

                        binding.musicListLL.addView(card);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fillMusicList() {
        binding.musicListLL.removeAllViews();
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot music : snapshot.getChildren()){
                    View card = getLayoutInflater().inflate(R.layout.music_card, binding.musicListLL, false);
                    TextView name = card.findViewById(R.id.music_name);
                    TextView id = card.findViewById(R.id.music_id);

                    String musicname = music.child("title").getValue().toString();
                    String artist = music.child("artist").getValue().toString();

                    name.setText(artist + " - " + musicname);
                    id.setText(music.getKey());

                    binding.musicListLL.addView(card);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
