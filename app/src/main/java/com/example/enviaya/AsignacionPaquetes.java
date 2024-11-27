package com.example.enviaya;

import java.util.ArrayList;
import java.util.List;

public class AsignacionPaquetes {

    private String idListaPaquetesAsignados;
    private List<String> idPaquete;
    private String idAdministrador;
    private String idConductor;
    private int numeroPaquetes;
    private boolean aceptado; // Nuevo campo

    public AsignacionPaquetes() {
        this.idPaquete = new ArrayList<>();
        this.aceptado = false; // Valor predeterminado
    }

    public AsignacionPaquetes(String idListaPaquetesAsignados, List<String> idPaquete, String idAdministrador, String idConductor, int numeroPaquetes) {
        this.idListaPaquetesAsignados = idListaPaquetesAsignados;
        this.idPaquete = idPaquete != null ? idPaquete : new ArrayList<>();
        this.idAdministrador = idAdministrador;
        this.idConductor = idConductor;
        this.numeroPaquetes = numeroPaquetes;
        this.aceptado = false; // Valor predeterminado
    }

    public AsignacionPaquetes(String idListaPaquetesAsignados, List<String> idPaquete, String idAdministrador, String idConductor, int numeroPaquetes, boolean aceptado) {
        this.idListaPaquetesAsignados = idListaPaquetesAsignados;
        this.idPaquete = idPaquete != null ? idPaquete : new ArrayList<>();
        this.idAdministrador = idAdministrador;
        this.idConductor = idConductor;
        this.numeroPaquetes = numeroPaquetes;
        this.aceptado = aceptado; // Permite establecer el valor explícitamente
    }

    public String getIdListaPaquetesAsignados() {
        return idListaPaquetesAsignados;
    }

    public void setIdListaPaquetesAsignados(String idListaPaquetesAsignados) {
        this.idListaPaquetesAsignados = idListaPaquetesAsignados;
    }

    public List<String> getIdPaquete() {
        return idPaquete;
    }

    public void setIdPaquete(List<String> idPaquete) {
        this.idPaquete = idPaquete != null ? idPaquete : new ArrayList<>();
    }

    public String getIdAdministrador() {
        return idAdministrador;
    }

    public void setIdAdministrador(String idAdministrador) {
        this.idAdministrador = idAdministrador;
    }

    public String getIdConductor() {
        return idConductor;
    }

    public void setIdConductor(String idConductor) {
        this.idConductor = idConductor;
    }

    public int getNumeroPaquetes() {
        return numeroPaquetes;
    }

    public void setNumeroPaquetes(int numeroPaquetes) {
        this.numeroPaquetes = numeroPaquetes;
    }

    public boolean isAceptado() {
        return aceptado;
    }

    public void setAceptado(boolean aceptado) {
        this.aceptado = aceptado;
    }
}
