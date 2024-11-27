package com.example.enviaya;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AsignacionPaqueteAdapter extends RecyclerView.Adapter<AsignacionPaqueteAdapter.AsignacionPaqueteViewHolder> {

    private List<Paquete> listaPaquetes;
    private Context context;

    // Modificación del constructor para recibir Context y la lista de Paquetes
    public AsignacionPaqueteAdapter(Context context, List<Paquete> listaPaquetes) {
        this.context = context;
        this.listaPaquetes = listaPaquetes;
    }

    @Override
    public AsignacionPaqueteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asignacion_paquete, parent, false);
        return new AsignacionPaqueteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AsignacionPaqueteViewHolder holder, int position) {
        Paquete paquete = listaPaquetes.get(position);
        holder.idPaqueteTextView.setText("ID: " + paquete.getIdPaquete());
        holder.estadoTextView.setText("Estado: " + paquete.getEstado());
        holder.direccionEntregaTextView.setText("Dirección: " + paquete.getDireccionEntrega());

        holder.itemView.setOnClickListener(v ->{
            Intent intent = new Intent(context, TomarFotoActivity.class);
            intent.putExtra("idPaquete", paquete.getIdPaquete());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaPaquetes.size();
    }

    public static class AsignacionPaqueteViewHolder extends RecyclerView.ViewHolder {

        TextView idPaqueteTextView;
        TextView estadoTextView;
        TextView direccionEntregaTextView;

        public AsignacionPaqueteViewHolder(View itemView) {
            super(itemView);
            idPaqueteTextView = itemView.findViewById(R.id.idAsignacionPaqueteTextView);
            estadoTextView = itemView.findViewById(R.id.estadoAsignacionTextView);
            direccionEntregaTextView = itemView.findViewById(R.id.direccionAsignacionTextView);
        }
    }
}
