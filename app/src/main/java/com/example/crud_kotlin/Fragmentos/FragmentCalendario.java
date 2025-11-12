package com.example.crud_kotlin.Fragmentos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.crud_kotlin.AlarmUtils;
import com.example.crud_kotlin.Modelos.Recordatorio;
import com.example.crud_kotlin.R;
import com.example.crud_kotlin.adapter.RecordatorioAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FragmentCalendario extends Fragment {

    private RecyclerView recyclerView;
    private RecordatorioAdapter adapter;
    private FirebaseFirestore db;
    private List<Recordatorio> listaRecordatorios = new ArrayList<>();
    private FloatingActionButton fabAdd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendario, container, false);

        // Inicializar componentes
        fabAdd = view.findViewById(R.id.fab_add);
        recyclerView = view.findViewById(R.id.recyclerViewCalendario);
        db = FirebaseFirestore.getInstance();

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecordatorioAdapter(getContext(), listaRecordatorios); // Pasar la lista real
        recyclerView.setAdapter(adapter);

        // Configurar listeners
        adapter.setOnRecordatorioClickListener(new RecordatorioAdapter.OnRecordatorioClickListener() {
            @Override
            public void onEditarRecordatorio(Recordatorio recordatorio) {
                editarRecordatorio(recordatorio);
            }

            @Override
            public void onEliminarRecordatorio(Recordatorio recordatorio) {
                eliminarRecordatorio(recordatorio);
            }
        });

        configurarFAB();
        cargarRecordatorios();

        return view;
    }

    private void configurarFAB() {
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> navegarAFragmentoAgregar());
        }
    }

    private void navegarAFragmentoAgregar() {
        try {
            FragmentCalendaryAdd fragment = new FragmentCalendaryAdd();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentoFL, fragment)
                    .addToBackStack("calendario")
                    .commit();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error al abrir formulario", Toast.LENGTH_SHORT).show();
        }
    }

    private void editarRecordatorio(Recordatorio recordatorio) {
        try {
            FragmentCalendaryEdit fragmentEdit = FragmentCalendaryEdit.newInstance(recordatorio);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentoFL, fragmentEdit)
                    .addToBackStack("calendario")
                    .commit();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error al editar recordatorio", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarRecordatorios() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = usuario.getUid();
        Log.d("FragmentCalendario", "Cargando recordatorios para usuario: " + userId);

        db.collection("recordatorios")
                .whereEqualTo("usuario", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("FragmentCalendario", "Error: " + error.getMessage());
                        return;
                    }

                    Log.d("FragmentCalendario", "Snapshot recibido. Documentos: " + (value != null ? value.size() : 0));

                    listaRecordatorios.clear();

                    if (value != null && !value.isEmpty()) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Recordatorio recordatorio = doc.toObject(Recordatorio.class);
                            if (recordatorio != null) {
                                recordatorio.setId(doc.getId());
                                listaRecordatorios.add(recordatorio);
                                Log.d("FragmentCalendario", "Recordatorio cargado: " + recordatorio.getTitulo() + " - ID: " + doc.getId());
                            }
                        }
                    } else {
                        Log.d("FragmentCalendario", "No se encontraron documentos");
                    }

                    // Ordenar por fecha y hora
                    ordenarRecordatoriosLocalmente(listaRecordatorios);

                    // Actualizar adapter
                    adapter.actualizarLista(listaRecordatorios);

                    Log.d("FragmentCalendario", "Total en lista: " + listaRecordatorios.size());

                    if (listaRecordatorios.isEmpty()) {
                        Toast.makeText(requireContext(), "No tienes recordatorios", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void ordenarRecordatoriosLocalmente(List<Recordatorio> lista) {
        Collections.sort(lista, (r1, r2) -> {
            try {
                SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US);
                Date fechaHora1 = formato.parse(r1.getFecha() + " " + r1.getHora());
                Date fechaHora2 = formato.parse(r2.getFecha() + " " + r2.getHora());
                return fechaHora1.compareTo(fechaHora2);
            } catch (Exception e) {
                return 0;
            }
        });
    }

    private void eliminarRecordatorio(Recordatorio recordatorio) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Recordatorio")
                .setMessage("¿Eliminar: " + recordatorio.getTitulo() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    db.collection("recordatorios")
                            .document(recordatorio.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                AlarmUtils.INSTANCE.cancelarAlarma(requireContext(), recordatorio);
                                Toast.makeText(requireContext(), "Recordatorio eliminado", Toast.LENGTH_SHORT).show();
                                // No necesitas recargar porque el snapshotListener se actualiza automáticamente
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarRecordatorios(); // Forzar recarga al volver
    }
}