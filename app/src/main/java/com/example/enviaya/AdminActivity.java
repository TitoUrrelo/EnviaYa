package com.example.enviaya;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView paqueteRecyclerView;
    private PaqueteAdapter paqueteAdapter;
    private List<Paquete> paqueteList;
    private List<Paquete> paquetesSeleccionados;
    private Button asignarPaqueteButton;
    private Button btnCreatePaquete;
    private Button btnVerReportes;
    private Spinner conductorSpinner;

    private Spinner spinnerEstadoPaquetes;
    private String estadoSeleccionado = "registrado";

    private DatabaseReference paquetesRef;
    private DatabaseReference conductoresRef;
    private DatabaseReference reportesRef;

    private List<String> conductorIds;
    private List<String> conductorNombres;

    private String adminEmail;
    private String adminId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        spinnerEstadoPaquetes = findViewById(R.id.spinnerEstadoPaquetes);

        ArrayAdapter<String> estadoAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"registrado", "pendiente", "Devuelto"});
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstadoPaquetes.setAdapter(estadoAdapter);

        spinnerEstadoPaquetes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                estadoSeleccionado = parent.getItemAtPosition(position).toString();
                cargarPaquetes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnVerReportes = findViewById(R.id.btnVerReportes);
        btnVerReportes.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, ReportesActivity.class);
            startActivity(intent);
        });


        adminEmail = getIntent().getStringExtra("email");

        if (adminEmail != null) {
            buscarIdAdministrador(adminEmail, new AdminEmailCallback() {
                @Override
                public void onCallback(String idAdministrador) {
                    adminId = idAdministrador;
                }
            });
        }

        paquetesRef = FirebaseDatabase.getInstance().getReference("Paquetes");
        conductoresRef = FirebaseDatabase.getInstance().getReference("Usuarios");
        reportesRef = FirebaseDatabase.getInstance().getReference("Reportes");

        paqueteRecyclerView = findViewById(R.id.paqueteRecyclerView);
        asignarPaqueteButton = findViewById(R.id.asignarPaqueteButton);
        btnCreatePaquete = findViewById(R.id.btnAddPackage);
        conductorSpinner = findViewById(R.id.conductorSpinner);

        paqueteList = new ArrayList<>();
        paquetesSeleccionados = new ArrayList<>();
        conductorIds = new ArrayList<>();
        conductorNombres = new ArrayList<>();

        paqueteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        paqueteAdapter = new PaqueteAdapter(paqueteList, paquetesSeleccionados, this::mostrarDialogoDetallesPaquete);
        paqueteRecyclerView.setAdapter(paqueteAdapter);

        cargarPaquetes();
        cargarConductores();

        asignarPaqueteButton.setOnClickListener(v -> asignarPaquetesAConductor());

        btnCreatePaquete.setOnClickListener(v -> {
            if (adminEmail != null) {
                buscarIdAdministrador(adminEmail, new AdminEmailCallback() {
                    @Override
                    public void onCallback(String idAdministrador) {
                        if (idAdministrador != null) {
                            Intent intent = new Intent(AdminActivity.this, CreatePaqueteActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(AdminActivity.this, "Solo un administrador puede crear paquetes.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(AdminActivity.this, "No se ha encontrado el correo del administrador.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarPaquetes() {
        paquetesRef.orderByChild("estado").equalTo(estadoSeleccionado).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                paqueteList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Paquete paquete = dataSnapshot.getValue(Paquete.class);
                    if (paquete != null) {
                        paqueteList.add(paquete);
                    }
                }
                paqueteAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AdminActivity.this, "Error al cargar los paquetes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarConductores() {
        conductoresRef.orderByChild("tipoUsuario").equalTo("conductor").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                conductorIds.clear();
                conductorNombres.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String conductorId = dataSnapshot.getKey();
                    String nombreConductor = dataSnapshot.child("nombre").getValue(String.class);
                    Boolean disponibilidad = dataSnapshot.child("disponibilidad").getValue(Boolean.class);

                    if (conductorId != null && nombreConductor != null && disponibilidad != null && disponibilidad) {
                        conductorIds.add(conductorId);
                        conductorNombres.add(nombreConductor);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminActivity.this, android.R.layout.simple_spinner_item, conductorNombres);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                conductorSpinner.setAdapter(adapter);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AdminActivity.this, "Error al cargar conductores: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoDetallesPaquete(Paquete paquete) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Detalles del Paquete");
        String mensaje = "ID: " + paquete.getIdPaquete() +
                "\nEstado: " + paquete.getEstado() +
                "\nDirección de Entrega: " + paquete.getDireccionEntrega() +
                "\nPrioridad: " + paquete.getPrioridad() +
                "\nPeso: " + paquete.getPeso();
        builder.setMessage(mensaje);
        builder.setPositiveButton("Cerrar", null);
        builder.show();
    }

    private void asignarPaquetesAConductor() {
        int selectedPosition = conductorSpinner.getSelectedItemPosition();
        if (selectedPosition == -1) {
            Toast.makeText(this, "Por favor, selecciona un conductor", Toast.LENGTH_SHORT).show();
            return;
        }
        String conductorId = conductorIds.get(selectedPosition);
        String idListaPaquetesAsignados = paquetesRef.push().getKey(); // Genera una nueva ID para la lista
        if (paquetesSeleccionados.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos un paquete", Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> paquetesIds = new ArrayList<>();
        for (Paquete paquete : paquetesSeleccionados) {
            paquete.setEstado("asignado");
            paquetesIds.add(paquete.getIdPaquete());
            paquetesRef.child(paquete.getIdPaquete()).setValue(paquete);
            crearReporte(paquete.getIdPaquete(), conductorId);
        }
        int numeroPaquetes = paquetesSeleccionados.size();
        if (adminId == null) {
            Toast.makeText(AdminActivity.this, "Error al obtener el ID del administrador", Toast.LENGTH_SHORT).show();
            return;
        }
        AsignacionPaquetes asignacion = new AsignacionPaquetes(idListaPaquetesAsignados, paquetesIds, adminId, conductorId, numeroPaquetes, false);
        conductoresRef.child(conductorId).child("paquetesAsignados").child(idListaPaquetesAsignados).setValue(asignacion)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        conductoresRef.child(conductorId).child("disponibilidad").setValue(false);

                        Toast.makeText(AdminActivity.this, "Paquetes asignados correctamente", Toast.LENGTH_SHORT).show();
                        paquetesSeleccionados.clear();
                        paqueteAdapter.notifyDataSetChanged();
                        cargarConductores();
                    } else {
                        Toast.makeText(AdminActivity.this, "Error al asignar la lista de paquetes", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void crearReporte(String idPaquete, String conductorId) {
        String fecha = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
        String hora = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());

        String idReporte = reportesRef.push().getKey();

        Reporte reporte = new Reporte(idReporte, conductorId, idPaquete, "asignado", hora, fecha);
        reportesRef.child(idReporte).setValue(reporte)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AdminActivity.this, "Reporte creado correctamente para el paquete " + idPaquete, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AdminActivity.this, "Error al crear el reporte", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void buscarIdAdministrador(String email, final AdminEmailCallback callback) {
        DatabaseReference usuariosRef = FirebaseDatabase.getInstance().getReference("Usuarios");
        usuariosRef.orderByChild("correo").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String adminId = snapshot.getKey();
                        callback.onCallback(adminId);
                    }
                } else {
                    Toast.makeText(AdminActivity.this, "Administrador no encontrado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AdminActivity.this, "Error al obtener datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public interface AdminEmailCallback {
        void onCallback(String idAdministrador);
    }

    public void ir(View view) {
        if (esAdministrador()) {
            Intent intent = new Intent(this, CreateUserActivity.class);
            startActivity(intent);
        }
    }

    private boolean esAdministrador() {
        if (adminEmail != null && adminId != null) {
            return true;
        }
        return false;
    }
}

