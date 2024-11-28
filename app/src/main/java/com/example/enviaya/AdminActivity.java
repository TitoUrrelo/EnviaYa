package com.example.enviaya;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
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
                    adminId = idAdministrador; // Guardamos el ID del administrador
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
                // Verificar si el usuario es administrador
                buscarIdAdministrador(adminEmail, new AdminEmailCallback() {
                    @Override
                    public void onCallback(String idAdministrador) {
                        if (idAdministrador != null) {
                            // El usuario es administrador, proceder con la creación del paquete
                            Intent intent = new Intent(AdminActivity.this, CreatePaqueteActivity.class);
                            startActivity(intent);
                        } else {
                            // El usuario no es administrador, mostrar mensaje
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
        paquetesRef.orderByChild("estado").equalTo("registrado").addValueEventListener(new ValueEventListener() {
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

            // Crear el reporte para cada paquete asignado
            crearReporte(paquete.getIdPaquete(), conductorId);
        }

        int numeroPaquetes = paquetesSeleccionados.size();

        if (adminId == null) {
            Toast.makeText(AdminActivity.this, "Error al obtener el ID del administrador", Toast.LENGTH_SHORT).show();
            return;
        }

        // Incluye el campo 'aceptado' con valor false
        AsignacionPaquetes asignacion = new AsignacionPaquetes(idListaPaquetesAsignados, paquetesIds, adminId, conductorId, numeroPaquetes, false);

        // Realiza la asignación de paquetes
        conductoresRef.child(conductorId).child("paquetesAsignados").child(idListaPaquetesAsignados).setValue(asignacion)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Cambiar la disponibilidad del conductor a false
                        conductoresRef.child(conductorId).child("disponibilidad").setValue(false);

                        Toast.makeText(AdminActivity.this, "Paquetes asignados correctamente", Toast.LENGTH_SHORT).show();
                        paquetesSeleccionados.clear();
                        paqueteAdapter.notifyDataSetChanged();

                        // Recargar la lista de conductores para reflejar el cambio
                        cargarConductores();
                    } else {
                        Toast.makeText(AdminActivity.this, "Error al asignar la lista de paquetes", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // Método para crear un reporte cuando se cambia el estado a "asignado"
    private void crearReporte(String idPaquete, String conductorId) {
        // Obtener la hora y la fecha actuales
        String fecha = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
        String hora = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());

        String idReporte = reportesRef.push().getKey(); // Genera una nueva ID para el reporte

        Reporte reporte = new Reporte(idReporte, conductorId, idPaquete, "asignado", hora, fecha);

        // Guardar el reporte en la base de datos
        reportesRef.child(idReporte).setValue(reporte)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AdminActivity.this, "Reporte creado correctamente para el paquete " + idPaquete, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AdminActivity.this, "Error al crear el reporte", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Callback para obtener el email del administrador y retornar el ID
    private void buscarIdAdministrador(String email, final AdminEmailCallback callback) {
        DatabaseReference usuariosRef = FirebaseDatabase.getInstance().getReference("Usuarios");
        usuariosRef.orderByChild("correo").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String adminId = snapshot.getKey(); // Obtener el ID del administrador
                        callback.onCallback(adminId); // Pasa el ID al callback
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

    // Interfaz para callback
    public interface AdminEmailCallback {
        void onCallback(String idAdministrador);
    }
}

