package com.example.enviaya;

import android.os.Bundle;
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

public class ReportesActivity extends AppCompatActivity {

    private RecyclerView reportesRecyclerView;
    private ReporteAdapter reporteAdapter;
    private List<Reporte> reporteList;
    private DatabaseReference reportesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportes);

        reportesRecyclerView = findViewById(R.id.reportesRecyclerView);
        reportesRef = FirebaseDatabase.getInstance().getReference("Reportes");

        reporteList = new ArrayList<>();
        reporteAdapter = new ReporteAdapter(reporteList);
        reportesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reportesRecyclerView.setAdapter(reporteAdapter);

        cargarReportes();
    }

    private void cargarReportes() {
        reportesRef.orderByChild("estado").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                reporteList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Reporte reporte = dataSnapshot.getValue(Reporte.class);
                    if (reporte != null) {
                        reporteList.add(reporte);
                    }
                }
                reporteAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ReportesActivity.this, "Error al cargar los reportes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
