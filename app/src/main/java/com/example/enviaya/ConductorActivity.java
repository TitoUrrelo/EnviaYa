package com.example.enviaya;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConductorActivity extends AppCompatActivity {

    private RecyclerView asignacionPaqueteRecyclerView;
    private AsignacionPaqueteAdapter asignacionPaqueteAdapter;
    private List<Paquete> listaPaquetes;
    private DatabaseReference asignacionesRef;
    private DatabaseReference paquetesRef;
    private DatabaseReference conductoresRef;
    private DatabaseReference reportesRef;  // Nueva referencia para los reportes

    private String conductorEmail;
    private String conductorId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conductor);

        conductorEmail = getIntent().getStringExtra("email");

        if (conductorEmail != null) {
            buscarIdConductor(conductorEmail, new ConductorEmailCallback() {
                @Override
                public void onCallback(String idConductor) {
                    if (idConductor != null) {
                        conductorId = idConductor;
                        cargarPaquetesAsignados();
                    } else {
                        Toast.makeText(ConductorActivity.this, "Conductor no encontrado", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Correo de conductor no proporcionado", Toast.LENGTH_SHORT).show();
        }

        // Referencias a Firebase
        asignacionesRef = FirebaseDatabase.getInstance().getReference("paquetesAsignados");
        paquetesRef = FirebaseDatabase.getInstance().getReference("Paquetes");
        conductoresRef = FirebaseDatabase.getInstance().getReference("Usuarios");
        reportesRef = FirebaseDatabase.getInstance().getReference("Reportes"); // Nueva referencia para los reportes

        // Configuración del RecyclerView
        asignacionPaqueteRecyclerView = findViewById(R.id.asignacionPaqueteRecyclerView);
        asignacionPaqueteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listaPaquetes = new ArrayList<>();
        asignacionPaqueteAdapter = new AsignacionPaqueteAdapter(this, listaPaquetes);
        asignacionPaqueteRecyclerView.setAdapter(asignacionPaqueteAdapter);
    }

    private void cargarPaquetesAsignados() {
        DatabaseReference paquetesAsignadosRef = conductoresRef.child(conductorId).child("paquetesAsignados");
        paquetesAsignadosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot asignacionSnapshot : dataSnapshot.getChildren()) {
                        AsignacionPaquetes asignacion = asignacionSnapshot.getValue(AsignacionPaquetes.class);
                        if (asignacion != null) {
                            if (!asignacion.isAceptado()) {
                                mostrarDialogoAceptacion(asignacionSnapshot.getKey(), asignacion);
                            } else {
                                List<String> idsPaquetes = asignacion.getIdPaquete();
                                cargarDetallesPaquetes(idsPaquetes);
                            }
                        }
                    }
                } else {
                    Toast.makeText(ConductorActivity.this, "No tienes paquetes asignados", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ConductorActivity.this, "Error al cargar los paquetes asignados", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoAceptacion(String idAsignacion, AsignacionPaquetes asignacion) {
        StringBuilder paqueteDetalles = new StringBuilder();
        int numPaquetes = asignacion.getIdPaquete().size();
        final int[] paquetesProcesados = {0};

        for (String paqueteId : asignacion.getIdPaquete()) {
            paquetesRef.child(paqueteId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Paquete paquete = dataSnapshot.getValue(Paquete.class);
                    if (paquete != null) {
                        paqueteDetalles.append("Paquete: ").append(paquete.getIdPaquete())
                                .append("\nDirección: ").append(paquete.getDireccionEntrega())
                                .append("\nPeso: ").append(paquete.getPeso()).append(" kg\n\n");
                    }

                    paquetesProcesados[0]++;
                    if (paquetesProcesados[0] == numPaquetes) {
                        TextView detallesTextView = new TextView(ConductorActivity.this);
                        detallesTextView.setText(paqueteDetalles.toString());
                        detallesTextView.setPadding(16, 16, 16, 16);

                        ScrollView scrollView = new ScrollView(ConductorActivity.this);
                        scrollView.addView(detallesTextView);

                        new AlertDialog.Builder(ConductorActivity.this)
                                .setTitle("Aceptar Asignación")
                                .setMessage("¿Aceptas esta lista de paquetes para entregar?\n\n" +
                                        "Número de paquetes: " + numPaquetes + "\n\n")
                                .setView(scrollView)
                                .setPositiveButton("Aceptar", (dialog, which) -> {
                                    // Actualizar los estados de todos los paquetes
                                    for (String paqueteId : asignacion.getIdPaquete()) {
                                        paquetesRef.child(paqueteId).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Paquete paquete = dataSnapshot.getValue(Paquete.class);
                                                if (paquete != null) {
                                                    paquete.setEstado("en tránsito");
                                                    paquetesRef.child(paquete.getIdPaquete()).setValue(paquete)
                                                            .addOnCompleteListener(task -> {
                                                                if (task.isSuccessful()) {
                                                                    generarReporteTransito(paquete);
                                                                    Toast.makeText(ConductorActivity.this,
                                                                            "Estado de paquete actualizado a 'en tránsito'",
                                                                            Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    Toast.makeText(ConductorActivity.this,
                                                                            "Error al actualizar el estado del paquete",
                                                                            Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Toast.makeText(ConductorActivity.this,
                                                        "Error al cargar detalles del paquete",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    // Actualizar el estado de aceptación en Firebase
                                    conductoresRef.child(conductorId).child("paquetesAsignados")
                                            .child(idAsignacion).child("aceptado").setValue(true)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(ConductorActivity.this,
                                                            "Asignación aceptada",
                                                            Toast.LENGTH_SHORT).show();

                                                    // Actualizar la lista de paquetes después de aceptar la asignación
                                                    cargarPaquetesAsignados();  // Recargar la lista
                                                } else {
                                                    Toast.makeText(ConductorActivity.this,
                                                            "Error al actualizar la asignación",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                })
                                .setNegativeButton("Rechazar", (dialog, which) -> {
                                    eliminarAsignacionYActualizarEstado(idAsignacion, asignacion);
                                })
                                .setCancelable(false)
                                .show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(ConductorActivity.this,
                            "Error al cargar los detalles del paquete",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void cargarDetallesPaquetes(List<String> idsPaquetes) {
        paquetesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listaPaquetes.clear();
                for (String paqueteId : idsPaquetes) {
                    DataSnapshot paqueteSnapshot = dataSnapshot.child(paqueteId);
                    Paquete paquete = paqueteSnapshot.getValue(Paquete.class);
                    if (paquete != null && "en tránsito".equals(paquete.getEstado())) {
                        listaPaquetes.add(paquete);
                    }
                }
                asignacionPaqueteAdapter.notifyDataSetChanged();

                // Verificar si no hay paquetes y mostrar el botón de confirmación
                verificarPaquetesDisponibles();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ConductorActivity.this, "Error al cargar los detalles de los paquetes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verificarPaquetesDisponibles() {
        Button btnConfirmarEntregas = findViewById(R.id.btnConfirmarEntregas); // Asegúrate de tener este botón en el layout
        if (listaPaquetes.isEmpty()) {
            btnConfirmarEntregas.setVisibility(View.VISIBLE);
            btnConfirmarEntregas.setOnClickListener(v -> confirmarEntregasTerminadas());
        } else {
            btnConfirmarEntregas.setVisibility(View.GONE);
        }
    }
    private void confirmarEntregasTerminadas() {
        // Cambiar disponibilidad del conductor
        conductoresRef.child(conductorId).child("disponibilidad").setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ConductorActivity.this, "Disponibilidad actualizada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ConductorActivity.this, "Error al actualizar disponibilidad", Toast.LENGTH_SHORT).show();
                    }
                });

        // Eliminar paquetes asignados
        conductoresRef.child(conductorId).child("paquetesAsignados").removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ConductorActivity.this, "Paquetes asignados eliminados", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ConductorActivity.this, "Error al eliminar paquetes asignados", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void aceptarPaquetes() {

        int totalPaquetes = listaPaquetes.size();
        final int[] paquetesProcesados = {0};

        for (Paquete paquete : listaPaquetes) {
            paquete.setEstado("en tránsito");

            paquetesRef.child(paquete.getIdPaquete()).setValue(paquete)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            listaPaquetes.get(listaPaquetes.indexOf(paquete)).setEstado("en tránsito");
                            asignacionPaqueteAdapter.notifyItemChanged(listaPaquetes.indexOf(paquete));
                            generarReporteTransito(paquete);
                            Toast.makeText(ConductorActivity.this, "Estado actualizado a 'en tránsito' y reporte generado", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ConductorActivity.this, "Error al actualizar el estado", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void eliminarAsignacionYActualizarEstado(String idAsignacion, AsignacionPaquetes asignacion) {
        // Eliminar la asignación del conductor
        conductoresRef.child(conductorId).child("paquetesAsignados").child(idAsignacion).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ConductorActivity.this, "Asignación eliminada", Toast.LENGTH_SHORT).show();

                        // Cambiar el estado de todos los paquetes a "pendiente"
                        for (String paqueteId : asignacion.getIdPaquete()) {
                            paquetesRef.child(paqueteId).child("estado").setValue("pendiente")
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            // Generar el reporte de cambio de estado a "pendiente"
                                            generarReportePendiente(paqueteId);
                                        } else {
                                            Toast.makeText(ConductorActivity.this, "Error al actualizar estado", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(ConductorActivity.this, "Error al eliminar la asignación", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void generarReportePendiente(String paqueteId) {
        // Obtener la fecha y hora actuales con el formato solicitado
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaActual = dateFormat.format(new Date());

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String horaActual = timeFormat.format(new Date());

        // Crear un nuevo reporte
        String reporteId = reportesRef.push().getKey(); // Generar un ID único para el reporte
        Reporte reporte = new Reporte(
                reporteId,                      // ID del reporte
                conductorId,                    // ID del conductor
                paqueteId,                      // ID del paquete
                "pendiente",                    // Estado actualizado
                horaActual,                     // Hora
                fechaActual                     // Fecha
        );

        // Guardar el reporte en Firebase
        reportesRef.child(reporteId).setValue(reporte)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ConductorActivity.this, "Reporte generado exitosamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ConductorActivity.this, "Error al generar el reporte", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void generarReporteTransito(Paquete paquete) {
        // Obtener la fecha y hora actuales con el formato solicitado
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaActual = dateFormat.format(new Date());

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String horaActual = timeFormat.format(new Date());

        // Crear un nuevo reporte
        String reporteId = paquetesRef.push().getKey(); // Generar un ID único para el reporte
        Reporte reporte = new Reporte(
                reporteId,                      // ID del reporte
                conductorId,                    // ID del conductor
                paquete.getIdPaquete(),         // ID del paquete
                "en tránsito",                  // Estado actualizado
                horaActual,                     // Hora
                fechaActual                     // Fecha
        );

        // Guardar el reporte en Firebase
        DatabaseReference reportesRef = FirebaseDatabase.getInstance().getReference("Reportes");
        reportesRef.child(reporteId).setValue(reporte)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ConductorActivity.this, "Reporte generado exitosamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ConductorActivity.this, "Error al generar el reporte", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void buscarIdConductor(String conductorEmail, final ConductorEmailCallback callback) {
        DatabaseReference usuariosRef = FirebaseDatabase.getInstance().getReference("Usuarios");
        usuariosRef.orderByChild("correo").equalTo(conductorEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String conductorId = snapshot.getKey(); // Obtiene el ID del conductor
                        callback.onCallback(conductorId); // Pasa el ID al callback
                    }
                } else {
                    Toast.makeText(ConductorActivity.this, "Conductor no encontrado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ConductorActivity.this, "Error al obtener datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public interface ConductorEmailCallback {
        void onCallback(String idConductor);
    }
}