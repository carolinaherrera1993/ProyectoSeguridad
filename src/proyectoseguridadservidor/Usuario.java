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
    
    
    
}
