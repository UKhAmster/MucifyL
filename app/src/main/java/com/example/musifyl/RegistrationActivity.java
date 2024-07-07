package com.example.musifyl;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.musifyl.databinding.ActivityRegistrationBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class RegistrationActivity extends AppCompatActivity {
    private ActivityRegistrationBinding binding;

    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        Intent NavDrawIntent = new Intent(this, NavigationDrawerActivity.class);
        Intent LoginIntent = new Intent(this, LoginActivity.class);

        binding.conBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.emailReg.getText().toString();
                String pass = binding.passwordReg.getText().toString();
                String conPass = binding.passwordRegCon.getText().toString();
                if (!email.isEmpty() && !pass.isEmpty() && !conPass.isEmpty() && pass.equals(conPass)){
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                startActivity(NavDrawIntent);
                                finish();
                            }
                        }
                    });
                }
            }
        });
        binding.linkToAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(LoginIntent);
                finish();
            }
        });
    }
}
