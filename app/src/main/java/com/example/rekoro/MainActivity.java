package com.example.rekoro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.TextView;
import android.app.Dialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ktx.Firebase;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ImageButton exitButton = findViewById(R.id.exit);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, EnterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });


        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            startActivity(new Intent(MainActivity.this, EnterActivity.class));
        }
        TextView userEmailText = findViewById(R.id.userEmailText);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            userEmailText.setText(user.getEmail());
        } else {
            userEmailText.setText("Ошибка: пользователь не найден");
        }


        FloatingActionButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddItemDialog();
            }
        });
    }

   /* private void showAddItemDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.create_window);

        Button saveButton = dialog.findViewById(R.id.saveButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "Сохранено", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int)(getResources().getDisplayMetrics().widthPixels * 0.9),
                    (int)(getResources().getDisplayMetrics().heightPixels * 0.5)
            );
        }
    }
    */
   private void showAddItemDialog() {
       final Dialog dialog = new Dialog(this);
       dialog.setContentView(R.layout.create_window);

       EditText categoryEdit = dialog.findViewById(R.id.category);
       EditText titleEdit = dialog.findViewById(R.id.title);
       EditText progressEdit = dialog.findViewById(R.id.progress);
       EditText countEdit = dialog.findViewById(R.id.count);
       EditText descriptionEdit = dialog.findViewById(R.id.description);

       Button saveButton = dialog.findViewById(R.id.saveButton);
       Button cancelButton = dialog.findViewById(R.id.cancelButton);

       cancelButton.setOnClickListener(v -> dialog.dismiss());

       saveButton.setOnClickListener(v -> {
           String category = categoryEdit.getText().toString().trim();
           String title = titleEdit.getText().toString().trim();
           String progress = progressEdit.getText().toString().trim();
           String count = countEdit.getText().toString().trim();
           String description = descriptionEdit.getText().toString().trim();

           FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
           if (user != null) {
               String uid = user.getUid();

               TaskItem task = new TaskItem(category, title, progress, count, description);

               FirebaseFirestore db = FirebaseFirestore.getInstance();
               db.collection("users")
                       .document(uid)
                       .collection("tasks")
                       .add(task)
                       .addOnSuccessListener(documentReference -> {
                           Toast.makeText(MainActivity.this, "Сохранено", Toast.LENGTH_SHORT).show();
                           dialog.dismiss();
                       })
                       .addOnFailureListener(e -> {
                           Toast.makeText(MainActivity.this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show();
                       });
           }
       });

       dialog.show();
       if (dialog.getWindow() != null) {
           dialog.getWindow().setLayout(
                   (int)(getResources().getDisplayMetrics().widthPixels * 0.9),
                   (int)(getResources().getDisplayMetrics().heightPixels * 0.5)
           );
       }
   }


}


