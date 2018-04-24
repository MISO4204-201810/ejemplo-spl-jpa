package datos.modelo;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;


@MappedSuperclass
@NamedQueries({
	@NamedQuery( name="usuario.buscarPorNumDocumento", 
			query="select u from Usuario u where u.numDocumento = :numDocumento" )
})
public class AbstractUsuario {

	@Id
	private String codigo;
	
	private String numDocumento;
	
	private String nombre;
	
	private String direccion;
	
	public AbstractUsuario() {
		super();
	}

	public void setCodigo(String value) {
		this.codigo = value;
	}

	public String getCodigo() {
		return this.codigo;
	}

	public void setNumDocumento(String value) {
		this.numDocumento = value;
	}

	public String getNumDocumento() {
		return this.numDocumento;
	}

	public void setNombre(String value) {
		this.nombre = value;
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setDireccion(String value) {
		this.direccion = value;
	}

	public String getDireccion() {
		return this.direccion;
	}

}