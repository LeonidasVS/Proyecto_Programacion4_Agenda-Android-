package com.example.crud_kotlin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.crud_kotlin.Modelos.Nota;
import com.example.crud_kotlin.R;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Nota> lista;
    private Context context;
    private OnNotaClickListener listener;

    public NoteAdapter(Context context, List<Nota> lista) {
        this.context = context;
        this.lista = new ArrayList<>();
        if (lista != null) this.lista.addAll(lista);
    }

    public interface OnNotaClickListener {
        void onEditarNota(Nota nota);
        void onEliminarNota(Nota nota);
    }

    public void setOnNotaClickListener(OnNotaClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Nota nota = lista.get(position);
        if (nota == null) return;

        holder.tvTitulo.setText(nota.getTitulo() != null ? nota.getTitulo() : "");
        holder.tvContenido.setText(nota.getContenido() != null ? nota.getContenido() : "");
        holder.tvFecha.setText(nota.getFechaCreacion() != null ? nota.getFechaCreacion() : "");

        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) listener.onEditarNota(nota);
        });

        holder.btnEliminar.setOnClickListener(v -> {
            if (listener != null) listener.onEliminarNota(nota);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvContenido, tvFecha;
        MaterialButton btnEditar, btnEliminar;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tv_note_title);
            tvContenido = itemView.findViewById(R.id.tv_note_content);
            tvFecha = itemView.findViewById(R.id.tv_note_date);
            btnEditar = itemView.findViewById(R.id.btn_edit_note);
            btnEliminar = itemView.findViewById(R.id.btn_delete_note);
        }
    }

    public void actualizarLista(List<Nota> nuevaLista) {
        this.lista.clear();
        if (nuevaLista != null) this.lista.addAll(nuevaLista);
        notifyDataSetChanged();
    }
}