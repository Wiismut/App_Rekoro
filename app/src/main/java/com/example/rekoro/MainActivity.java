package com.example.rekoro;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;
import android.app.Dialog;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.ktx.Firebase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private LinearLayout tasksContainer;
    private StorageReference storageRef;
    private FirebaseFirestore db;
    private String userId;
    private TextView userEmailText;
    private static final int GALLERYPICK = 1;
    private Uri ImageUri;
    private ImageView imageTitle;
    private ProgressDialog loadingBar;
    private boolean isEditingImage = false;

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

        storageRef = FirebaseStorage.getInstance().getReference();
        userEmailText = findViewById(R.id.userEmailText);
        tasksContainer = findViewById(R.id.tasksContainer);
        db = FirebaseFirestore.getInstance();
        loadingBar = new ProgressDialog(this);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            userEmailText.setText(user.getEmail());
        } else {
            startActivity(new Intent(MainActivity.this, EnterActivity.class));
            finish();
            return;
        }
        loadTasks();

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

        FloatingActionButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageUri = null;
                showAddItemDialog();
            }
        });
    }

    private void loadTasks() {
        db.collection("users")
                .document(userId)
                .collection("tasks")
                .orderBy("title", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(MainActivity.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        tasksContainer.removeAllViews();

                        for (QueryDocumentSnapshot doc : value) {
                            TaskItem task = doc.toObject(TaskItem.class);
                            task.setId(doc.getId());
                            addTaskToView(task);
                        }
                    }
                });
    }

    private void addTaskToView(TaskItem task) {
        View taskView = getLayoutInflater().inflate(R.layout.item_task, tasksContainer, false);

        TextView categoryText = taskView.findViewById(R.id.categoryText);
        TextView titleText = taskView.findViewById(R.id.titleText);
        TextView progressText = taskView.findViewById(R.id.progressText);
        TextView countText = taskView.findViewById(R.id.countText);

        categoryText.setText(task.getCategory());
        titleText.setText(task.getTitle());
        progressText.setText(task.getProgress() + "/");
        countText.setText(task.getCount());

        CardView showWind = taskView.findViewById(R.id.card);

        showWind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showItemWindow(task);
            }
        });

        tasksContainer.addView(taskView, 0);
    }

    private void showItemWindow(TaskItem task) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.show_window);

        TextView titleText = dialog.findViewById(R.id.title);
        TextView descriptionText = dialog.findViewById(R.id.description);
        TextView categoryText = dialog.findViewById(R.id.category);
        TextView progressText = dialog.findViewById(R.id.progress);
        TextView countText = dialog.findViewById(R.id.count);
        ImageView imageView = dialog.findViewById(R.id.imageTitle);

        titleText.setText(task.getTitle());
        descriptionText.setText(task.getDescription());
        categoryText.setText("Категория: " + task.getCategory());
        progressText.setText("Прогресс: " + task.getProgress());
        countText.setText("Количество: " + task.getCount());
        if (task.getImageUrl() != null && !task.getImageUrl().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(task.getImageUrl(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imageView.setImageBitmap(decodedByte);
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.add_image);
            }
        } else {
            imageView.setImageResource(R.drawable.add_image);
        }

        Button deleteButton = dialog.findViewById(R.id.deleteButton);
        Button editButton = dialog.findViewById(R.id.editButton);

        deleteButton.setOnClickListener(v -> {
            showAlertDialog(MainActivity.this, "Удаление записи", "Удалить?",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface alertDialog, int which) {
                            deleteTask(task.getId());
                            dialog.dismiss();
                        }
                    },
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface alertDialog, int which) {
                            dialog.dismiss();
                        }
                    }
            );
        });

        editButton.setOnClickListener(v -> {
            dialog.dismiss();
            isEditingImage = true;
            showEditItemDialog(task);
        });

        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    (int) (getResources().getDisplayMetrics().heightPixels * 0.7)
            );
        }
    }

    private void showAlertDialog(Context context, String title, String message,
                                 DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Да", positiveListener);
        if (negativeListener != null) {
            builder.setNegativeButton("Отмена", negativeListener);
        }
        builder.show();
    }

    private void deleteTask(String taskId) {
        db.collection("users")
                .document(userId)
                .collection("tasks")
                .document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Запись удалена", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                });
    }

    private void OpenGallary() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERYPICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERYPICK && resultCode == RESULT_OK && data != null) {
            ImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), ImageUri);
                imageTitle.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateTask(String taskId, TaskItem task, Dialog dialog) {
        db.collection("users")
                .document(userId)
                .collection("tasks")
                .document(taskId)
                .set(task)
                .addOnSuccessListener(aVoid -> {
                    loadingBar.dismiss();
                    Toast.makeText(MainActivity.this, "Изменения сохранены", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    loadingBar.dismiss();
                    Toast.makeText(MainActivity.this, "Ошибка при сохранении изменений", Toast.LENGTH_SHORT).show();
                });
    }

    private Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    private void showEditItemDialog(TaskItem task) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.create_window);

        EditText categoryEdit = dialog.findViewById(R.id.category);
        EditText titleEdit = dialog.findViewById(R.id.title);
        EditText progressEdit = dialog.findViewById(R.id.progress);
        EditText countEdit = dialog.findViewById(R.id.count);
        EditText descriptionEdit = dialog.findViewById(R.id.description);
        imageTitle = dialog.findViewById(R.id.imageTitle);
        categoryEdit.setText(task.getCategory());
        titleEdit.setText(task.getTitle());
        progressEdit.setText(task.getProgress());
        countEdit.setText(task.getCount());
        descriptionEdit.setText(task.getDescription());
        if (task.getImageUrl() != null && !task.getImageUrl().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(task.getImageUrl(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imageTitle.setImageBitmap(decodedByte);
                ImageUri = null;
            } catch (Exception e) {
                imageTitle.setImageResource(R.drawable.add_image);
            }
        } else {
            imageTitle.setImageResource(R.drawable.add_image);
        }
        imageTitle.setOnClickListener(v -> OpenGallary());

        Button saveButton = dialog.findViewById(R.id.saveButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String category = categoryEdit.getText().toString().trim();
            String title = titleEdit.getText().toString().trim();
            String progress = progressEdit.getText().toString().trim();
            String count = countEdit.getText().toString().trim();
            String description = descriptionEdit.getText().toString().trim();

            if (TextUtils.isEmpty(category)) {
                Toast.makeText(this, "Введите категорию", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(this, "Введите название", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(progress)) {
                Toast.makeText(this, "Введите прогресс", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(count)) {
                Toast.makeText(this, "Введите количество", Toast.LENGTH_SHORT).show();
                return;
            }

            loadingBar.setTitle("Обновление задачи");
            loadingBar.setMessage("Пожалуйста, подождите...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            if (ImageUri != null) {
                new Thread(() -> {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), ImageUri);
                        bitmap = scaleBitmap(bitmap, 800);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
                        final String imageString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

                        runOnUiThread(() -> {
                            TaskItem updatedTask = new TaskItem(category, title, progress, count, description, imageString);
                            updateTask(task.getId(), updatedTask, dialog);
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            loadingBar.dismiss();
                            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
            } else {
                TaskItem updatedTask = new TaskItem(category, title, progress, count, description, task.getImageUrl());
                updateTask(task.getId(), updatedTask, dialog);
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    (int) (getResources().getDisplayMetrics().heightPixels * 0.7)
            );
        }
    }

    private void showAddItemDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.create_window);

        EditText categoryEdit = dialog.findViewById(R.id.category);
        EditText titleEdit = dialog.findViewById(R.id.title);
        EditText progressEdit = dialog.findViewById(R.id.progress);
        EditText countEdit = dialog.findViewById(R.id.count);
        EditText descriptionEdit = dialog.findViewById(R.id.description);
        imageTitle = dialog.findViewById(R.id.imageTitle);
        Button saveButton = dialog.findViewById(R.id.saveButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);

        imageTitle.setImageResource(R.drawable.add_image);
        imageTitle.setOnClickListener(v -> OpenGallary());

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String category = categoryEdit.getText().toString().trim();
            String title = titleEdit.getText().toString().trim();
            String progress = progressEdit.getText().toString().trim();
            String count = countEdit.getText().toString().trim();
            String description = descriptionEdit.getText().toString().trim();

            if (TextUtils.isEmpty(category)) {
                Toast.makeText(this, "Введите категорию", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(this, "Введите название", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(progress)) {
                Toast.makeText(this, "Введите прогресс", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(count)) {
                Toast.makeText(this, "Введите количество", Toast.LENGTH_SHORT).show();
                return;
            }

            if (ImageUri == null) {
                Toast.makeText(this, "Добавьте изображение", Toast.LENGTH_SHORT).show();
                return;
            }

            loadingBar.setTitle("Добавление задачи");
            loadingBar.setMessage("Пожалуйста, подождите...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            new Thread(() -> {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), ImageUri);
                    bitmap = scaleBitmap(bitmap, 800);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
                    final String imageString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

                    runOnUiThread(() -> {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();

                            TaskItem task = new TaskItem(category, title, progress, count, description, imageString);

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users")
                                    .document(uid)
                                    .collection("tasks")
                                    .add(task)
                                    .addOnSuccessListener(documentReference -> {
                                        loadingBar.dismiss();
                                        Toast.makeText(MainActivity.this, "Сохранено", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    })
                                    .addOnFailureListener(e -> {
                                        loadingBar.dismiss();
                                        Toast.makeText(MainActivity.this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        loadingBar.dismiss();
                        Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    (int) (getResources().getDisplayMetrics().heightPixels * 0.7)
            );
        }
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int maxDimension) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxDimension && height <= maxDimension) {
            return bitmap;
        }

        float ratio = (float) width / (float) height;
        int newWidth, newHeight;

        if (width > height) {
            newWidth = maxDimension;
            newHeight = (int) (newWidth / ratio);
        } else {
            newHeight = maxDimension;
            newWidth = (int) (newHeight * ratio);
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
}