package com.example.finditmobile.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finditmobile.EditarItemActivity;
import com.example.finditmobile.R;
import com.example.finditmobile.SolicitarItemActivity;
import com.example.finditmobile.model.ItemPerdido;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ItemPerdidoAdapter extends RecyclerView.Adapter<ItemPerdidoAdapter.ViewHolder> {

    private Context context;
    private List<ItemPerdido> itemList;
    private boolean isAdmin = false;

    public ItemPerdidoAdapter(Context context, List<ItemPerdido> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewItem;
        TextView textViewTituloItem;
        TextView textViewDescricaoItem;
        LinearLayout layoutAdminButtons;
        MaterialButton buttonEditar;
        MaterialButton buttonExcluir;
        MaterialButton buttonSolicitar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewItem        = itemView.findViewById(R.id.imageViewItem);
            textViewTituloItem   = itemView.findViewById(R.id.textViewTituloItem);
            textViewDescricaoItem= itemView.findViewById(R.id.textViewDescricaoItem);
            layoutAdminButtons   = itemView.findViewById(R.id.layoutAdminButtons);
            buttonEditar         = itemView.findViewById(R.id.buttonEditar);
            buttonExcluir        = itemView.findViewById(R.id.buttonExcluir);
            buttonSolicitar      = itemView.findViewById(R.id.btn_solicitar);
        }
    }

    @NonNull
    @Override
    public ItemPerdidoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lost, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemPerdidoAdapter.ViewHolder holder, int position) {
        ItemPerdido item = itemList.get(position);

        // Preenche dados básicos
        holder.textViewTituloItem.setText(item.getTitulo());
        holder.textViewDescricaoItem.setText(item.getDescricao());
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getImageUrl())
                    .centerCrop()
                    .into(holder.imageViewItem);
        } else {
            holder.imageViewItem.setImageResource(R.drawable.ic_baseline_image_24);
        }

        // Animação de entrada
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
        holder.itemView.startAnimation(animation);

        // Mostra botões de admin ou de solicitar
        if (isAdmin) {
            holder.layoutAdminButtons.setVisibility(View.VISIBLE);
            holder.buttonSolicitar.setVisibility(View.GONE);
        } else {
            holder.layoutAdminButtons.setVisibility(View.GONE);
            holder.buttonSolicitar.setVisibility(View.VISIBLE);
        }

        // Ação de Editar (admin)
        holder.buttonEditar.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditarItemActivity.class);
            intent.putExtra("itemId", item.getId());
            context.startActivity(intent);
        });

        // Ação de Excluir (admin)
        holder.buttonExcluir.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Excluir Item")
                    .setMessage("Tem certeza que deseja excluir este item?")
                    .setPositiveButton("Sim", (dialog, which) -> excluirItem(item))
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        // Ação de Solicitar (usuário comum)
        holder.buttonSolicitar.setOnClickListener(v -> {
            Intent intent = new Intent(context, SolicitarItemActivity.class);
            intent.putExtra("itemId", item.getId());
            context.startActivity(intent);
        });
    }

    private void excluirItem(ItemPerdido item) {
        Toast.makeText(context, "Excluindo item...", Toast.LENGTH_SHORT).show();

        FirebaseFirestore.getInstance()
                .collection("itens_perdidos")
                .document(item.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    removerImagemDoStorage(item.getImageUrl());
                    Toast.makeText(context, "Item excluído", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Erro ao excluir item", Toast.LENGTH_SHORT).show()
                );
    }

    private void removerImagemDoStorage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
            ref.delete()
                    .addOnSuccessListener(aVoid -> {/* imagem removida */})
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Erro ao remover imagem", Toast.LENGTH_SHORT).show()
                    );
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}