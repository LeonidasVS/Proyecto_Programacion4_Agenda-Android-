package com.example.crud_kotlin.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.crud_kotlin.Modelos.Recordatorio;
import com.example.crud_kotlin.R;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordatorioAdapter extends RecyclerView.Adapter<RecordatorioAdapter.RecordatorioViewHolder> {

    private List<Recordatorio> lista;
    private Context context;
    private OnEliminarRecordatorioListener listener;

    public RecordatorioAdapter(Context context, List<Recordatorio> lista) {
        this.context = context;
        this.lista = new ArrayList<>();
        if (lista != null) {
            this.lista.addAll(lista);
        }
    }

    public interface OnEliminarRecordatorioListener {
        void onEliminarRecordatorio(Recordatorio recordatorio);
    }

    public void setOnEliminarRecordatorioListener(OnEliminarRecordatorioListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecordatorioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_calendary, parent, false);
        return new RecordatorioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordatorioViewHolder holder, int position) {
        Recordatorio r = lista.get(position);
        if (r == null) return;

        // Procesar fecha
        FechaProcesada fp = procesarFecha(r.getFecha());

        // Configurar vistas
        holder.tvDay.setText(fp.dayNumber != null ? fp.dayNumber : "--");
        holder.tvMonth.setText(fp.month != null ? fp.month : "--");
        holder.tvYear.setText(fp.year != null ? fp.year : "--");
        holder.tvDayOfWeek.setText(fp.dayOfWeek != null ? fp.dayOfWeek : "--");
        holder.tvLocation.setText(r.getLugar() != null ? r.getLugar() : "");
        holder.tvHora.setText(r.getHora() != null ? r.getHora() : "");
        holder.tvTitulo.setText(r.getTitulo() != null ? r.getTitulo() : "Sin título");

        // Botones
        holder.btnViewDetails.setOnClickListener(v ->
                Toast.makeText(context, "Detalle: " + r.getTitulo(), Toast.LENGTH_SHORT).show()
        );

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEliminarRecordatorio(r);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class RecordatorioViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvHora, tvDay, tvMonth, tvYear, tvDayOfWeek, tvLocation;
        MaterialButton btnViewDetails, btnDelete;

        public RecordatorioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tv_event_title);
            tvHora = itemView.findViewById(R.id.tv_time);
            tvDay = itemView.findViewById(R.id.tv_day);
            tvMonth = itemView.findViewById(R.id.tv_month);
            tvYear = itemView.findViewById(R.id.tv_year);
            tvDayOfWeek = itemView.findViewById(R.id.tv_day_of_week);
            tvLocation = itemView.findViewById(R.id.tv_location);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    private FechaProcesada procesarFecha(String fechaTexto) {
        FechaProcesada fp = new FechaProcesada();
        try {
            SimpleDateFormat formatoEntrada = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date fecha = formatoEntrada.parse(fechaTexto);

            fp.dayOfWeek = new SimpleDateFormat("EEEE", new Locale("es", "ES")).format(fecha);
            fp.dayNumber = new SimpleDateFormat("dd", Locale.getDefault()).format(fecha);
            fp.month = new SimpleDateFormat("MMM", new Locale("es", "ES")).format(fecha);
            fp.year = new SimpleDateFormat("yyyy", Locale.getDefault()).format(fecha);

        } catch (Exception e) {
            fp.dayOfWeek = "---";
            fp.dayNumber = "--";
            fp.month = "---";
            fp.year = "----";
        }
        return fp;
    }

    static class FechaProcesada {
        String dayOfWeek, dayNumber, month, year;
    }

    public void actualizarLista(List<Recordatorio> nuevaLista) {
        this.lista.clear();
        if (nuevaLista != null) {
            this.lista.addAll(nuevaLista);
        }
        notifyDataSetChanged();
    }
}