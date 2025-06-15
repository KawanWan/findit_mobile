// com/example/finditmobile/adapter/SolicitacaoAdapter.java
package com.example.finditmobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.finditmobile.R;
import com.example.finditmobile.model.Solicitacao;
import java.text.DateFormat;
import java.util.List;

public class SolicitacaoAdapter extends RecyclerView.Adapter<SolicitacaoAdapter.ViewHolder> {

    private Context context;
    private List<Solicitacao> lista;

    public SolicitacaoAdapter(Context ctx, List<Solicitacao> lista) {
        this.context = ctx;
        this.lista = lista;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMensagem, tvStatus, tvData;
        public ViewHolder(@NonNull View v) {
            super(v);
            tvMensagem = v.findViewById(R.id.tv_solicitacao_mensagem);
            tvStatus   = v.findViewById(R.id.tv_solicitacao_status);
            tvData     = v.findViewById(R.id.tv_solicitacao_data);
        }
    }

    @NonNull
    @Override
    public SolicitacaoAdapter.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_solicitacao, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull SolicitacaoAdapter.ViewHolder holder, int pos) {
        Solicitacao s = lista.get(pos);
        holder.tvMensagem.setText(s.getMensagem());
        holder.tvStatus.setText(s.getStatus());
        if (s.getTimestamp() != null) {
            String dt = DateFormat.getDateTimeInstance()
                    .format(s.getTimestamp());
            holder.tvData.setText(dt);
        } else {
            holder.tvData.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}