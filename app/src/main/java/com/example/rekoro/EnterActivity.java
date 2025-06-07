package com.example.rekoro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.rekoro.databinding.ActivityEnterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.ktx.Firebase;

public class EnterActivity extends AppCompatActivity {

    private ActivityEnterBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_enter);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding = ActivityEnterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.logLogin.getText().toString().isEmpty() || binding.logPassword.getText().toString().isEmpty()){
                    Toast.makeText(EnterActivity.this, "Заполните поля!", Toast.LENGTH_SHORT).show();
                }
                else{
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(binding.logLogin.getText().toString(), binding.logPassword.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        startActivity(new Intent(EnterActivity.this, MainActivity.class));
                                    }
                                    else{
                                        Toast.makeText(EnterActivity.this, "Проверьте корректность данных!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }
    public void openLog(View view){
        Intent intent = new Intent(this, LogActivity.class);
        startActivity(intent);
    }
}