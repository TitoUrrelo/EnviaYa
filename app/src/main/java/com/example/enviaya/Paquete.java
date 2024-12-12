package com.example.enviaya;

public class Paquete {

    private String idPaquete;
    private String estado;
    private String direccionEntrega;
    private String prioridad;
    private double peso;
    private String fotoUrl;

    public Paquete() {
    }

    public Paquete(String idPaquete, String estado, String direccionEntrega, String prioridad, double peso, String fotoUrl) {
        this.idPaquete = idPaquete;
        this.estado = estado;
        this.direccionEntrega = direccionEntrega;
        this.prioridad = prioridad;
        this.peso = peso;
        this.fotoUrl = fotoUrl;
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

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }
}
