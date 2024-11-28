package com.example.enviaya;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReporteAdapter extends RecyclerView.Adapter<ReporteAdapter.ReporteViewHolder> {

    private List<Reporte> reporteList;

    public ReporteAdapter(List<Reporte> reporteList) {
        this.reporteList = reporteList;
    }

    @Override
    public ReporteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reporte, parent, false);
        return new ReporteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReporteViewHolder holder, int position) {
        Reporte reporte = reporteList.get(position);
        holder.idPaqueteTextView.setText("ID Paquete: " + reporte.getIdPaquete());
        holder.estadoTextView.setText("Estado: " + reporte.getEstado());
        holder.fechaTextView.setText("Fecha: " + reporte.getFecha());
        holder.horaTextView.setText("Hora: " + reporte.getHora());
    }

    @Override
    public int getItemCount() {
        return reporteList.size();
    }

    public static class ReporteViewHolder extends RecyclerView.ViewHolder {

        TextView idPaqueteTextView;
        TextView estadoTextView;
        TextView fechaTextView;
        TextView horaTextView;

        public ReporteViewHolder(View itemView) {
            super(itemView);
            idPaqueteTextView = itemView.findViewById(R.id.idPaqueteTextView);
            estadoTextView = itemView.findViewById(R.id.estadoTextView);
            fechaTextView = itemView.findViewById(R.id.fechaTextView);
            horaTextView = itemView.findViewById(R.id.horaTextView);
        }
    }
}