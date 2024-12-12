package com.example.enviaya;

public class Usuario {

    private String id;
    private String nombre;
    private String correo;
    private String telefono;
    private String tipoUsuario; // "admin" o "conductor"

    private String matriculaVehiculo;
    private String tipoLicencia;
    private Boolean disponibilidad;

    public Usuario() {
    }

    //administradores
    public Usuario(String id, String nombre, String correo, String telefono, String tipoUsuario) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.tipoUsuario = tipoUsuario;
        this.matriculaVehiculo = null; // No aplica a administradores
        this.tipoLicencia = null; // No aplica a administradores
        this.disponibilidad = null; // No aplica a administradores
    }

    // conductores
    public Usuario(String id, String nombre, String correo, String telefono, String tipoUsuario,
                   String matriculaVehiculo, String tipoLicencia, Boolean disponibilidad) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.tipoUsuario = tipoUsuario;
        this.matriculaVehiculo = matriculaVehiculo;
        this.tipoLicencia = tipoLicencia;
        this.disponibilidad = disponibilidad;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public String getMatriculaVehiculo() {
        return matriculaVehiculo;
    }

    public void setMatriculaVehiculo(String matriculaVehiculo) {
        this.matriculaVehiculo = matriculaVehiculo;
    }

    public String getTipoLicencia() {
        return tipoLicencia;
    }

    public void setTipoLicencia(String tipoLicencia) {
        this.tipoLicencia = tipoLicencia;
    }

    public Boolean getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(Boolean disponibilidad) {
        this.disponibilidad = disponibilidad;
    }
}
