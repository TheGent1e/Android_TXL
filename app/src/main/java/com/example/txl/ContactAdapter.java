package com.example.txl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    private List<Contact> contactList;
    private List<Contact> originalList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Contact contact);
        void onItemLongClick(Contact contact);
    }

    public ContactAdapter(List<Contact> contactList) {
        this.contactList = contactList != null ? contactList : new ArrayList<>();
        this.originalList = new ArrayList<>(this.contactList);
    }

    public void setOnItemClickListener(OnItemClickListener listener) { this.listener = listener; }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.tvName.setText(contact.getName());
        holder.tvPhone.setText(contact.getMainPhone());
        if (contact.getAvatarPath() != null && !contact.getAvatarPath().isEmpty()) {
            try { holder.ivAvatar.setImageBitmap(BitmapFactory.decodeFile(contact.getAvatarPath())); }
            catch (Exception e) { holder.ivAvatar.setImageResource(R.drawable.circle_avatar); }
        } else { holder.ivAvatar.setImageResource(R.drawable.circle_avatar); }
        if (position > 0) {
            Contact prevContact = contactList.get(position - 1);
            if (!contact.getSortLetter().equals(prevContact.getSortLetter())) {
                holder.tvIndex.setText(contact.getSortLetter());
                holder.tvIndex.setVisibility(View.VISIBLE);
            } else { holder.tvIndex.setVisibility(View.GONE); }
        } else {
            holder.tvIndex.setText(contact.getSortLetter());
            holder.tvIndex.setVisibility(View.VISIBLE);
        }
        holder.itemView.setOnClickListener(v -> { if (listener != null) { listener.onItemClick(contact); } });
        holder.itemView.setOnLongClickListener(v -> { if (listener != null) { listener.onItemLongClick(contact); } return true; });
    }

    @Override public int getItemCount() { return contactList.size(); }

    public void updateData(List<Contact> newList) {
        this.contactList = newList != null ? newList : new ArrayList<>();
        this.originalList = new ArrayList<>(this.contactList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) { this.contactList = new ArrayList<>(originalList); }
        else {
            List<Contact> filteredList = new ArrayList<>();
            for (Contact contact : originalList) {
                if (contact.getName().toLowerCase().contains(query.toLowerCase()) || contact.getPhonesAsString().contains(query)) { filteredList.add(contact); }
            }
            this.contactList = filteredList;
        }
        notifyDataSetChanged();
    }

    public int getPositionForLetter(String letter) {
        for (int i = 0; i < contactList.size(); i++) { if (contactList.get(i).getSortLetter().equals(letter)) { return i; } }
        return -1;
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar; TextView tvName; TextView tvPhone; TextView tvIndex;
        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvIndex = itemView.findViewById(R.id.tv_index);
        }
    }
}