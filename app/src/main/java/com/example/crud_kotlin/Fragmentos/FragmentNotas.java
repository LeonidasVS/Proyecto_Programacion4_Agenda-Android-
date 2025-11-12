package com.example.crud_kotlin.Fragmentos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.crud_kotlin.Modelos.Nota;
import com.example.crud_kotlin.R;
import com.example.crud_kotlin.adapter.NoteAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FragmentNotas extends Fragment {

    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private FirebaseFirestore db;
    private List<Nota> listaNotas = new ArrayList<>();
    private FloatingActionButton fabAddNote;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notas, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewNotas);
        fabAddNote = view.findViewById(R.id.fab_add_note);
        db = FirebaseFirestore.getInstance();

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NoteAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Listeners
        adapter.setOnNotaClickListener(new NoteAdapter.OnNotaClickListener() {
            @Override
            public void onEditarNota(Nota nota) {
                abrirEditarNota(nota);
            }

            @Override
            public void onEliminarNota(Nota nota) {
                eliminarNota(nota);
            }
        });

        fabAddNote.setOnClickListener(v -> abrirCrearNota());

        cargarNotas();

        return view;
    }

    private void cargarNotas() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) return;

        db.collection("notas")
                .whereEqualTo("usuario", usuario.getUid())
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    List<Nota> nuevaLista = new ArrayList<>();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Nota nota = doc.toObject(Nota.class);
                            if (nota != null) {
                                nota.setId(doc.getId());
                                nuevaLista.add(nota);
                            }
                        }
                    }
                    adapter.actualizarLista(nuevaLista);
                });
    }

    private void abrirCrearNota() {
        // Aquí abres el fragment/activity para crear nota
        FragmentCrearNota fragment = new FragmentCrearNota();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentoFL, fragment)
                .addToBackStack("notas")
                .commit();
    }

    private void abrirEditarNota(Nota nota) {
        // Abrir edición pasando la nota como argumento
        FragmentCrearNota fragment = new FragmentCrearNota();
        Bundle args = new Bundle();
        args.putString("nota_id", nota.getId());
        args.putString("titulo", nota.getTitulo());
        args.putString("contenido", nota.getContenido());
        fragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentoFL, fragment)
                .addToBackStack("notas")
                .commit();
    }

    private void eliminarNota(Nota nota) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Nota")
                .setMessage("¿Eliminar: " + nota.getTitulo() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    db.collection("notas")
                            .document(nota.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(requireContext(), "Nota eliminada", Toast.LENGTH_SHORT).show();
                                cargarNotas();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}