package com.example.finditmobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.finditmobile.R;
import com.example.finditmobile.model.Notificacao;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificacaoAdapter extends RecyclerView.Adapter<NotificacaoAdapter.ViewHolder> {

    public interface OnActionListener {
        void onToggleRead(Notificacao n, int position);
        void onDelete(Notificacao n, int position);
    }

    private final List<Notificacao> lista;
    private final OnActionListener listener;
    private final SimpleDateFormat sdf =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public NotificacaoAdapter(List<Notificacao> lista, OnActionListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notificacao, parent, false);
        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notificacao n = lista.get(position);

        holder.titulo.setText(n.getTitulo());
        holder.mensagem.setText(n.getMensagem());
        if (n.getTimestamp() != null) {
            holder.data.setText(sdf.format(n.getTimestamp().toDate()));
        } else {
            holder.data.setText("");
        }

        // visual de lido
        holder.itemView.setAlpha(n.isLida() ? 0.5f : 1f);

        // texto e callback do botão Lido/Não lido
        holder.btnMarcarLido.setText(n.isLida() ? "Não lido" : "Lido");
        holder.btnMarcarLido.setOnClickListener(v ->
                listener.onToggleRead(n, position)
        );

        // callback do excluir
        holder.btnExcluir.setOnClickListener(v ->
                listener.onDelete(n, position)
        );
    }

    @Override public int getItemCount() { return lista.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titulo, mensagem, data;
        MaterialButton btnMarcarLido, btnExcluir;

        public ViewHolder(@NonNull View v) {
            super(v);
            titulo        = v.findViewById(R.id.tvTitulo);
            mensagem      = v.findViewById(R.id.tvMensagem);
            data          = v.findViewById(R.id.tvData);
            btnMarcarLido = v.findViewById(R.id.btnMarcarLido);
            btnExcluir    = v.findViewById(R.id.btnExcluir);
        }
    }
}