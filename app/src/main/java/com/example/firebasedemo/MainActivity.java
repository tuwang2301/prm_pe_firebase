package com.example.firebasedemo;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText editTextSearch;
    private RecyclerView recyclerViewContacts;
    private ContactAdapter contactAdapter;
    private List<Contact> contactList;
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
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                contactList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Contact contact = snapshot.getValue(Contact.class);
                    contactList.add(contact);
                }
                contactAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    private void searchContacts(String query) {
        List<Contact> filteredList = new ArrayList<>();
        for (Contact contact : contactList) {
            if (contact.getName().toLowerCase().contains(query.toLowerCase()) ||
                    contact.getEmail().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(contact);
            }
        }
        contactAdapter.updateList(filteredList);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == 1){
            Toast.makeText(this, "Edit " + item.getGroupId(), Toast.LENGTH_SHORT).show();
            return true;
        }else if(item.getItemId() == 2){
            Toast.makeText(this, "Delete " + item.getGroupId(), Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onContextItemSelected(item);
    }
}