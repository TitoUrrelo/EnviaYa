package com.example.enviaya;

public class Problema {
    private String idProblema;
    private String idConductor;
    private String idPaquete;
    private String descripcion;
    private String hora;
    private String fecha;

    public Problema() {
    }

    public Problema(String idProblema, String idConductor, String idPaquete, String descripcion, String hora, String fecha) {
        this.idProblema = idProblema;
        this.idConductor = idConductor;
        this.idPaquete = idPaquete;
        this.descripcion = descripcion;
        this.hora = hora;
        this.fecha = fecha;
    }

    public String getIdProblema() {
        return idProblema;
    }

    public void setIdProblema(String idProblema) {
        this.idProblema = idProblema;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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
