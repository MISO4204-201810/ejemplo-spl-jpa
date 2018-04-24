package datos.dao;

import javax.persistence.EntityManager;

import static datos.utils.FluentMap.Map;
import static datos.utils.FluentMap.entry;
import datos.utils.GenericJpaDAO;
import datos.modelo.AbstractUsuario;

public class DaoUsuario extends GenericJpaDAO<AbstractUsuario, String>{

	// == constructores
	
	public DaoUsuario() { }
	
	public DaoUsuario(EntityManager em) {
		super(em);
	}
	
	// == consultas
	
	@SuppressWarnings("unchecked")
	public AbstractUsuario buscarPorNumDocumento(String numDocumento) {
		return executeSingleResultNamedQuery("usuario.buscarPorNumDocumento", 
				Map( entry("numDocumento", numDocumento) ));
	}
	
}
