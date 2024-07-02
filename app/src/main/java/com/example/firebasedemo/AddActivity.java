package com.example.firebasedemo;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class AddActivity extends AppCompatActivity {

    private StorageReference storageReference;

    private DatabaseReference mDatabase;

    private Uri imageUri;

    private EditText edt_name, edt_email, edt_company, edt_address;

    private Button btn_select_photo, btn_save, btn_back;

    private ImageView img_photo;

    private TextView tv_title;

    private boolean isEditMode = false;

    private Contact currentContact;


    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if(o.getResultCode() == RESULT_OK){
                imageUri = o.getData().getData();
                Glide.with(getApplicationContext()).load(imageUri).into(img_photo);
        }else {
                Toast.makeText(AddActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
            }
    }
});

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseApp.initializeApp(AddActivity.this);
        storageReference = FirebaseStorage.getInstance().getReference();

        edt_name = findViewById(R.id.edt_name);
        edt_email = findViewById(R.id.edt_email);
        edt_company = findViewById(R.id.edt_company);
        edt_address = findViewById(R.id.edt_address);
        btn_select_photo = findViewById(R.id.btn_select_photo);
        btn_back = findViewById(R.id.btn_back);
        btn_save = findViewById(R.id.btn_save);
        img_photo = findViewById(R.id.img_photo);
        tv_title = findViewById(R.id.tv_title);

        // Kiểm tra intent để xác định chế độ (thêm hoặc chỉnh sửa)
        if (getIntent() != null && getIntent().hasExtra("contact")) {
            isEditMode = true;
            tv_title.setText("Edit Contact");
            String contactJson = getIntent().getStringExtra("contact");
            Gson gson = new Gson();
            currentContact = gson.fromJson(contactJson, Contact.class);
            populateContactDetails(currentContact);
        }

        btn_select_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edt_name.getText().toString();
                String email = edt_email.getText().toString();
                String company = edt_company.getText().toString();
                String address = edt_address.getText().toString();
                boolean valid = !name.isEmpty() && !email.isEmpty();

                if(valid){
                    Contact contact = new Contact(isEditMode ? currentContact.getId() : null ,name,email,company,address);
                    if(imageUri != null){
                        uploadImage(imageUri, contact);
                    }else {
                        addOrUpdateContact(contact);
                    }
                }else {
                    Toast.makeText(AddActivity.this, "Name and email cannot be empty!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT); // OR ACTION_PICK
        intent.setType("image/*");
        activityResultLauncher.launch(intent);
    }

    private void uploadImage(Uri imageUri, Contact contact){
        StorageReference reference = storageReference.child("images/" + System.currentTimeMillis() + ".jpg");
        reference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                Uri downloadUri = uriTask.getResult();

                if (uriTask.isSuccessful()) {
                    String downloadUrl = downloadUri.toString();
                    contact.setPhotoUrl(downloadUrl);
                    addOrUpdateContact(contact);
                    Toast.makeText(AddActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddActivity.this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
                }            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddActivity.this, "There was an error while uploading image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addOrUpdateContact(Contact contact) {
        // If id is null, generate a new one
        if (contact.getId() == null) {
            String id = mDatabase.child("contacts").push().getKey();
            contact.setId(id);
        }
        mDatabase.child("contacts").child(contact.getId()).setValue(contact);
        Toast.makeText(AddActivity.this, "Successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void populateContactDetails(Contact contact) {
        edt_name.setText(contact.getName());
        edt_email.setText(contact.getEmail());
        edt_company.setText(contact.getCompany());
        edt_address.setText(contact.getAddress());
        // Load ảnh vào imgPhoto
        Glide.with(this).load(contact.getPhotoUrl()).into(img_photo);
    }

}