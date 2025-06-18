package com.example.finditmobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finditmobile.R;
import com.example.finditmobile.model.Solicitacao;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SolicitacaoAdapter
        extends RecyclerView.Adapter<SolicitacaoAdapter.ViewHolder> {

    public interface OnActionListener {
        void onChangeStatus(Solicitacao solicitacao, String novoStatus);
    }
    public interface OnUserActionListener {
        void onEdit(Solicitacao solicitacao);
        void onDelete(Solicitacao solicitacao);
    }

    private Context context;
    private List<Solicitacao> lista;
    private boolean isAdmin;
    private OnActionListener adminListener;
    private OnUserActionListener userListener;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    // caches
    private Map<String, String> cacheNomeItem = new HashMap<>();
    private Map<String, String> cacheImageUrl = new HashMap<>();
    private Map<String, String> cacheUserName = new HashMap<>();

    public SolicitacaoAdapter(Context ctx,
                              List<Solicitacao> lista,
                              boolean isAdmin,
                              OnActionListener adminListener,
                              OnUserActionListener userListener) {
        this.context       = ctx;
        this.lista         = lista;
        this.isAdmin       = isAdmin;
        this.adminListener = adminListener;
        this.userListener  = userListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoto;
        TextView  tvNomeObjeto, tvUsuario, tvMensagem, tvStatus, tvResposta, tvData;
        LinearLayout llAdmin, llUser;
        MaterialButton btnAceitar, btnRecusar, btnEditar, btnExcluir;

        public ViewHolder(@NonNull View v) {
            super(v);
            ivFoto       = v.findViewById(R.id.iv_solicitacao_foto);
            tvNomeObjeto = v.findViewById(R.id.tv_solicitacao_nome_objeto);
            tvUsuario    = v.findViewById(R.id.tv_solicitacao_usuario);
            tvMensagem   = v.findViewById(R.id.tv_solicitacao_mensagem);
            tvStatus     = v.findViewById(R.id.tv_solicitacao_status);
            tvResposta   = v.findViewById(R.id.tv_solicitacao_resposta);
            tvData       = v.findViewById(R.id.tv_solicitacao_data);

            llAdmin      = v.findViewById(R.id.ll_actions_admin);
            btnAceitar   = v.findViewById(R.id.btnAceitar);
            btnRecusar   = v.findViewById(R.id.btnRecusar);

            llUser       = v.findViewById(R.id.ll_actions_user);
            btnEditar    = v.findViewById(R.id.buttonEditar);
            btnExcluir   = v.findViewById(R.id.buttonExcluir);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_solicitacao, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull final ViewHolder holder,
            int position) {

        Solicitacao s = lista.get(position);

        // usuário (apenas admin)
        if (isAdmin) {
            holder.tvUsuario.setVisibility(View.VISIBLE);
            String uid = s.getUserId();
            if (cacheUserName.containsKey(uid)) {
                holder.tvUsuario.setText("De: " + cacheUserName.get(uid));
            } else {
                holder.tvUsuario.setText("De: carregando...");
                firestore.collection("users")
                        .document(uid)
                        .get()
                        .addOnSuccessListener(doc -> {
                            String nome = doc.exists()
                                    ? doc.getString("nome")
                                    : null;
                            if (nome == null || nome.isEmpty()) {
                                nome = "Nome indisponível";
                            }
                            cacheUserName.put(uid, nome);
                            holder.tvUsuario.setText("De: " + nome);
                        })
                        .addOnFailureListener(e ->
                                holder.tvUsuario.setText("De: erro")
                        );
            }
        } else {
            holder.tvUsuario.setVisibility(View.GONE);
        }

        // mensagem/local
        holder.tvMensagem.setText("Local da Perda: " + s.getMensagem());

        // status
        holder.tvStatus.setText("Status: " + s.getStatus());

        // respostaAdmin
        String resp = s.getRespostaAdmin();
        if (resp != null && !resp.isEmpty()) {
            holder.tvResposta.setVisibility(View.VISIBLE);
            holder.tvResposta.setText("Resposta: " + resp);
        } else {
            holder.tvResposta.setVisibility(View.GONE);
        }

        // data
        if (s.getTimestamp() != null) {
            holder.tvData.setText(
                    DateFormat.getDateTimeInstance().format(s.getTimestamp())
            );
        } else {
            holder.tvData.setText("");
        }

        // nome + imagem do item
        String itemId = s.getItemId();
        holder.tvNomeObjeto.setText(
                cacheNomeItem.getOrDefault(itemId, "Carregando...")
        );
        holder.ivFoto.setImageResource(R.drawable.placeholder);
        if (cacheImageUrl.containsKey(itemId)) {
            Glide.with(context)
                    .load(cacheImageUrl.get(itemId))
                    .centerCrop()
                    .into(holder.ivFoto);
        }
        if (!cacheNomeItem.containsKey(itemId)
                || !cacheImageUrl.containsKey(itemId)) {
            firestore.collection("itens_perdidos")
                    .document(itemId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String nome  = doc.exists()
                                ? doc.getString("titulo")
                                : "Item removido";
                        String image = doc.exists()
                                ? doc.getString("imageUrl")
                                : null;
                        cacheNomeItem.put(itemId, nome);
                        if (image != null) cacheImageUrl.put(itemId, image);
                        holder.tvNomeObjeto.setText(nome);
                        if (image != null) {
                            Glide.with(context)
                                    .load(image)
                                    .centerCrop()
                                    .into(holder.ivFoto);
                        }
                    })
                    .addOnFailureListener(e ->
                            holder.tvNomeObjeto.setText("Erro"));
        }

        // ações admin
        if (isAdmin && "pendente".equalsIgnoreCase(s.getStatus())) {
            holder.llAdmin.setVisibility(View.VISIBLE);
            holder.btnAceitar.setOnClickListener(v ->
                    adminListener.onChangeStatus(s, "aceita")
            );
            holder.btnRecusar.setOnClickListener(v ->
                    adminListener.onChangeStatus(s, "recusada")
            );
        } else {
            holder.llAdmin.setVisibility(View.GONE);
        }

        // ações usuário
        if (!isAdmin) {
            holder.llUser.setVisibility(View.VISIBLE);
            holder.btnEditar.setOnClickListener(v ->
                    userListener.onEdit(s)
            );
            holder.btnExcluir.setOnClickListener(v ->
                    userListener.onDelete(s)
            );
        } else {
            holder.llUser.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}