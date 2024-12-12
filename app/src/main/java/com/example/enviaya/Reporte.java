package com.example.enviaya;

public class Reporte {
    private String idReporte;
    private String idConductor;
    private String idPaquete;
    private String estado;
    private String hora;
    private String fecha;

    public Reporte() {}

    public Reporte(String idReporte, String idConductor, String idPaquete, String estado, String hora, String fecha) {
        this.idReporte = idReporte;
        this.idConductor = idConductor;
        this.idPaquete = idPaquete;
        this.estado = estado;
        this.hora = hora;
        this.fecha = fecha;
    }

    public String getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(String idReporte) {
        this.idReporte = idReporte;
    }

    public String getIdConductor() {
        return idConductor;
    }

    public void setIdConductor(String idConductor) {
        this.idConductor = idConductor;
    }

    public String getIdPaquete() {
        return idPaquete;
    }

    public void setIdPaquete(String idPaquete) {
        this.idPaquete = idPaquete;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}


