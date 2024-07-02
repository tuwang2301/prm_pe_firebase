package com.example.firebasedemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText editTextSearch;
    private RecyclerView recyclerViewContacts;
    private ValueEventListener contactsEventListener;
    private ContactAdapter contactAdapter;
    private List<Contact> contactList;
    private List<Contact> filteredContactList = new ArrayList<>();
    private DatabaseReference mDatabase;

    private Button btn_add_activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextSearch = findViewById(R.id.editTextSearch);
        recyclerViewContacts = findViewById(R.id.recyclerViewContacts);
        btn_add_activity = findViewById(R.id.btn_add_activity);

        btn_add_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                startActivity(intent);
            }
        });

        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));
        contactList = new ArrayList<>();
        contactAdapter = new ContactAdapter(contactList);
        recyclerViewContacts.setAdapter(contactAdapter);

        mDatabase = FirebaseDatabase.getInstance().getReference("contacts");

        // Load contacts from Firebase
        loadContacts();

        // Set up search functionality
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchContacts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadContacts() {
        contactsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                contactList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Contact contact = snapshot.getValue(Contact.class);
                    contactList.add(contact);
                }
                filteredContactList.clear();
                filteredContactList.addAll(contactList);
                contactAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        };
        mDatabase.addValueEventListener(contactsEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContacts();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (contactsEventListener != null) {
            mDatabase.removeEventListener(contactsEventListener);
        }
    }

    private void searchContacts(String query) {
        filteredContactList.clear();
        for (Contact contact : contactList) {
            if (contact.getName().toLowerCase().contains(query.toLowerCase()) ||
                    contact.getEmail().toLowerCase().contains(query.toLowerCase())) {
                filteredContactList.add(contact);
            }
        }
        contactAdapter.updateList(filteredContactList);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        Contact contact = filteredContactList.get(item.getGroupId());
        if(item.getItemId() == 1){
            Intent intent = new Intent(this, AddActivity.class);
            Gson gson = new Gson();
            String contactJson = gson.toJson(contact);
            intent.putExtra("contact", contactJson);
            startActivity(intent);
            return true;
        }else if(item.getItemId() == 2){
            showDeleteConfirmationDialog(contact.getId());
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void showDeleteConfirmationDialog(final String contactId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this contact?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteContact(contactId);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteContact(String contactId) {
        mDatabase.child(contactId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(MainActivity.this, "Contact deleted successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Failed to delete contact", Toast.LENGTH_SHORT).show();
            }
        });
    }
}