package com.example.rekoro;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.rekoro.databinding.ActivityLogBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LogActivity extends AppCompatActivity {
    private ActivityLogBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding = ActivityLogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.logLogin.getText().toString().isEmpty() || binding.logPassword.getText().toString().isEmpty() || binding.logPassword2.getText().toString().isEmpty()){
                    Toast.makeText(LogActivity.this, "Заполните поля!", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (!binding.logPassword.getText().toString().equals(binding.logPassword2.getText().toString())) {
                        Toast.makeText(LogActivity.this, "Пароли не совпадают!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(binding.logLogin.getText().toString(),binding.logPassword.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        HashMap<String, String> userInfo = new HashMap<>();
                                        userInfo.put("email", binding.logLogin.getText().toString());
                                        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .setValue(userInfo);
                                        startActivity(new Intent(LogActivity.this, MainActivity.class));
                                    }
                                    else {
                                        String errorMessage = "Ошибка: " + task.getException().getMessage();
                                        Toast.makeText(LogActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                        Log.e("LogActivity", "Полный текст ошибки: " + errorMessage); // Вывод в Logcat
                                    }

                                }
                            });

                }
            }
        });
    }
    public void openEnter(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}