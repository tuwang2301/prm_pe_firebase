package com.example.firebasedemo;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;


public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private List<Contact> contactList;

    public ContactAdapter(List<Contact> contactList) {
        this.contactList = contactList;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.tv_id.setText(contact.getId());
        holder.tv_name.setText(contact.getName());
        holder.tv_email.setText(contact.getEmail());
        holder.tv_company.setText(contact.getCompany());
        holder.tv_address.setText(contact.getAddress());
        String url = contact.getPhotoUrl() == null ? "" : contact.getPhotoUrl();
        Glide.with(holder.itemView.getContext())
                .load(url)
                .error(R.drawable.default_avatar)
                .into(holder.imageViewPhoto);
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public void updateList(List<Contact> newList) {
        contactList = newList;
        notifyDataSetChanged();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        TextView tv_name, tv_email, tv_company, tv_address, tv_id;
        ImageView imageViewPhoto;

        CardView cardView;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_id = itemView.findViewById(R.id.tv_id);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_email = itemView.findViewById(R.id.tv_email);
            tv_company = itemView.findViewById(R.id.tv_company);
            tv_address = itemView.findViewById(R.id.tv_address);
            imageViewPhoto = itemView.findViewById(R.id.img_photo);
            cardView = itemView.findViewById(R.id.cardView);
            cardView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(this.getAdapterPosition(), 1, 0, "Edit");
            menu.add(this.getAdapterPosition(), 2, 1, "Delete");
        }
    }
}
