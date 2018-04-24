package datos.modelo;

import javax.persistence.Entity;

@Entity
public class Usuario extends AbstractUsuario {

	public Usuario() {

	}

	String email;
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	
}
