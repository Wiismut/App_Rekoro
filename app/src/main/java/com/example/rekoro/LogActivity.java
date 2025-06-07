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
import com.google.android.material.snackbar.Snackbar;
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
                String email = binding.logLogin.getText().toString().trim();
                String password = binding.logPassword.getText().toString().trim();
                String password2 = binding.logPassword2.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty() || password2.isEmpty()) {
                    Toast.makeText(LogActivity.this, "Заполните поля!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isEmailValid(email)) {
                    Toast.makeText(LogActivity.this, "Некорректный email!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(password2)) {
                    Toast.makeText(LogActivity.this, "Пароли не совпадают!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isPasswordValid(password)) {
                    //Toast.makeText(LogActivity.this, "Пароль должен содержать хотя бы одну заглавную букву, одну цифру и быть не менее 6 символов", Toast.LENGTH_LONG).show();
                    Snackbar.make(findViewById(android.R.id.content), "Пароль должен содержать хотя бы одну заглавную букву, одну цифру и быть не менее 6 символов", Snackbar.LENGTH_LONG).show();


                    return;
                }

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    HashMap<String, String> userInfo = new HashMap<>();
                                    userInfo.put("email", email);
                                    FirebaseDatabase.getInstance().getReference().child("Users")
                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .setValue(userInfo);

                                    startActivity(new Intent(LogActivity.this, MainActivity.class));
                                } else {
                                    if (task.getException() instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                                        Toast.makeText(LogActivity.this, "Пользователь с таким email уже зарегистрирован", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String errorMessage = "Ошибка: " + task.getException().getMessage();
                                        Toast.makeText(LogActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                        Log.e("LogActivity", "Полный текст ошибки: " + errorMessage);
                                    }
                                }
                            }
                        });
            }
        });

    }
    public boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean isPasswordValid(String password) {
        return password.matches("^(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{6,}$");
    }

    public void openEnter(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}