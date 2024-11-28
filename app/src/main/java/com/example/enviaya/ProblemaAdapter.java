package com.example.enviaya;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProblemaAdapter extends RecyclerView.Adapter<ProblemaAdapter.ProblemaViewHolder> {

    private List<Problema> problemaList;

    // Constructor para inicializar la lista de problemas
    public ProblemaAdapter(List<Problema> problemaList) {
        this.problemaList = problemaList;
    }

    @Override
    public ProblemaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflar el diseño personalizado para cada elemento
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_problema, parent, false);
        return new ProblemaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProblemaViewHolder holder, int position) {
        // Obtener el problema actual
        Problema problema = problemaList.get(position);

        // Configurar los valores en los TextView
        holder.idPaqueteTextView.setText("ID Paquete: " + (problema.getIdPaquete() != null ? problema.getIdPaquete() : "N/A"));
        holder.descripcionTextView.setText("Descripción: " + problema.getDescripcion());
        holder.fechaTextView.setText("Fecha: " + problema.getFecha());
        holder.horaTextView.setText("Hora: " + problema.getHora());
    }

    @Override
    public int getItemCount() {
        return problemaList.size(); // Número total de elementos en la lista
    }

    // Clase interna para el ViewHolder
    public static class ProblemaViewHolder extends RecyclerView.ViewHolder {

        TextView idPaqueteTextView;
        TextView descripcionTextView;
        TextView fechaTextView;
        TextView horaTextView;

        public ProblemaViewHolder(View itemView) {
            super(itemView);
            // Vincular los TextView al diseño
            idPaqueteTextView = itemView.findViewById(R.id.idPaqueteTextView);
            descripcionTextView = itemView.findViewById(R.id.descripcionTextView);
            fechaTextView = itemView.findViewById(R.id.fechaTextView);
            horaTextView = itemView.findViewById(R.id.horaTextView);
        }
    }
}
