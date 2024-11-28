package com.example.enviaya;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReportesActivity extends AppCompatActivity {

    private RecyclerView reportesRecyclerView, problemasRecyclerView;
    private ReporteAdapter reporteAdapter;
    private ProblemaAdapter problemaAdapter;
    private List<Reporte> reporteList, filteredReportes;
    private List<Problema> problemaList, filteredProblemas;
    private DatabaseReference reportesRef, problemasRef;
    private Spinner spinnerPaquetes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportes);

        // Inicializar los RecyclerView
        reportesRecyclerView = findViewById(R.id.reportesRecyclerView);
        reportesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        problemasRecyclerView = findViewById(R.id.problemasRecyclerView);
        problemasRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar el Spinner
        spinnerPaquetes = findViewById(R.id.spinnerPaquetes);

        // Inicializar Firebase
        reportesRef = FirebaseDatabase.getInstance().getReference("Reportes");
        problemasRef = FirebaseDatabase.getInstance().getReference("Problemas");

        // Inicializar las listas
        reporteList = new ArrayList<>();
        filteredReportes = new ArrayList<>();
        problemaList = new ArrayList<>();
        filteredProblemas = new ArrayList<>();

        reporteAdapter = new ReporteAdapter(filteredReportes);
        problemaAdapter = new ProblemaAdapter(filteredProblemas);

        reportesRecyclerView.setAdapter(reporteAdapter);
        problemasRecyclerView.setAdapter(problemaAdapter);

        // Cargar los datos desde Firebase
        cargarReportes();
        cargarProblemas();

        // Configurar el Spinner
        spinnerPaquetes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedIdPaquete = (String) parentView.getItemAtPosition(position);
                filtrarPorIdPaquete(selectedIdPaquete);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Si no se selecciona nada, mostrar todos los datos
                mostrarTodos();
            }
        });
    }

    private void cargarReportes() {
        reportesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                reporteList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Reporte reporte = dataSnapshot.getValue(Reporte.class);
                    if (reporte != null) {
                        reporteList.add(reporte);
                    }
                }
                actualizarSpinner();
                mostrarTodos();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ReportesActivity.this, "Error al cargar reportes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarProblemas() {
        problemasRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                problemaList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Problema problema = dataSnapshot.getValue(Problema.class);
                    if (problema != null) {
                        problemaList.add(problema);
                    }
                }
                mostrarTodos();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ReportesActivity.this, "Error al cargar problemas: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarSpinner() {
        Set<String> idPaquetes = new HashSet<>();
        for (Reporte reporte : reporteList) {
            idPaquetes.add(reporte.getIdPaquete());
        }
        for (Problema problema : problemaList) {
            idPaquetes.add(problema.getIdPaquete());
        }

        List<String> idPaqueteList = new ArrayList<>(idPaquetes);
        idPaqueteList.add(0, "Selecciona un paquete");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, idPaqueteList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaquetes.setAdapter(spinnerAdapter);
    }

    private void filtrarPorIdPaquete(String idPaquete) {
        filteredReportes.clear();
        filteredProblemas.clear();

        if (idPaquete.equals("Selecciona un paquete")) {
            mostrarTodos();
        } else {
            for (Reporte reporte : reporteList) {
                if (reporte.getIdPaquete().equals(idPaquete)) {
                    filteredReportes.add(reporte);
                }
            }
            for (Problema problema : problemaList) {
                if (problema.getIdPaquete().equals(idPaquete)) {
                    filteredProblemas.add(problema);
                }
            }
        }

        reporteAdapter.notifyDataSetChanged();
        problemaAdapter.notifyDataSetChanged();
    }

    private void mostrarTodos() {
        filteredReportes.clear();
        filteredProblemas.clear();

        filteredReportes.addAll(reporteList);
        filteredProblemas.addAll(problemaList);

        reporteAdapter.notifyDataSetChanged();
        problemaAdapter.notifyDataSetChanged();
    }
}