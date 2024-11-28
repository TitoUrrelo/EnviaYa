package com.example.enviaya;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PaqueteAdapter extends RecyclerView.Adapter<PaqueteAdapter.PaqueteViewHolder> {

    private List<Paquete> paqueteList;
    private List<Paquete> paquetesSeleccionados;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Paquete paquete);
    }

    public PaqueteAdapter(List<Paquete> paqueteList, List<Paquete> paquetesSeleccionados, OnItemClickListener onItemClickListener) {
        this.paqueteList = paqueteList;
        this.paquetesSeleccionados = paquetesSeleccionados;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public PaqueteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_paquete, parent, false);
        return new PaqueteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaqueteViewHolder holder, int position) {
        Paquete paquete = paqueteList.get(position);
        holder.bind(paquete);
    }

    @Override
    public int getItemCount() {
        return paqueteList.size();
    }

    class PaqueteViewHolder extends RecyclerView.ViewHolder {
        TextView idPaqueteTextView;
        TextView estadoTextView;
        CheckBox selectCheckBox;

        PaqueteViewHolder(View itemView) {
            super(itemView);
            idPaqueteTextView = itemView.findViewById(R.id.idPaqueteTextView);
            estadoTextView = itemView.findViewById(R.id.estadoTextView);
            selectCheckBox = itemView.findViewById(R.id.selectCheckBox);

            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(paqueteList.get(getAdapterPosition()));
                }
            });
        }

        void bind(Paquete paquete) {
            idPaqueteTextView.setText(paquete.getIdPaquete());
            estadoTextView.setText(paquete.getPrioridad());

            selectCheckBox.setOnCheckedChangeListener(null);
            selectCheckBox.setChecked(paquetesSeleccionados.contains(paquete));

            selectCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    paquetesSeleccionados.add(paquete);
                } else {
                    paquetesSeleccionados.remove(paquete);
                }
            });
        }
    }
}
