/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyectoseguridadservidor;

/**
 *
 * @author carol
 */
public class Usuario {
    
    private String nombreUsuario;
    private Integer salt; 
    private String password; 
    private String activo; 
    private String ip; 
    private String claveGeneradaServidor; 

    public String getClaveGeneradaServidor() {
        return claveGeneradaServidor;
    }

    public void setClaveGeneradaServidor(String claveGeneradaServidor) {
        this.claveGeneradaServidor = claveGeneradaServidor;
    }

    public String getActivo() {
        return activo;
    }

    public void setActivo(String activo) {
        this.activo = activo;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Usuario(String nombreUsuario, Integer salt, String password) {
        this.nombreUsuario = nombreUsuario;
        this.salt = salt;
        this.password = password;
    }

    
    
    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public Integer getSalt() {
        return salt;
    }

    public void setSalt(Integer salt) {
        this.salt = salt;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Usuario(String nombreUsuario, Integer salt, String password, String activo, String ip, String claveGeneradaServidor) {
        this.nombreUsuario = nombreUsuario;
        this.salt = salt;
        this.password = password;
        this.activo = activo;
        this.ip = ip;
        this.claveGeneradaServidor = claveGeneradaServidor;
    }
    
    
    
}
