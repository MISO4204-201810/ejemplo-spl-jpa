package datos.modelo.pruebas;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import datos.modelo.Usuario;

public class DatosPrueba {

	public static void borrarDatosPrueba() {

		initDB();
		
		EntityTransaction tx = manager.getTransaction();
		
		tx.begin();
		manager.createQuery( "DELETE FROM Usuario" ).executeUpdate();
		tx.commit();
				
	}

	public static void crearUsuario( String codigo, String numDocumento, String nombre, String direccion ) {
		
		EntityTransaction tx = manager.getTransaction();
		tx.begin();
	
		
		Usuario usuario = new Usuario();
		usuario.setCodigo(codigo);
		usuario.setNumDocumento(numDocumento);
		usuario.setNombre(nombre);
		usuario.setDireccion(direccion);
		manager.persist(usuario);
		
		tx.commit();
		
	}

	
	// --

	static EntityManager manager;
	
	public static void initDB() {
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("test_PU");
		manager = factory.createEntityManager();
	}
	
	public static void crearDatosPrueba() {

		initDB();
		
		crearUsuario("100-01", "101", "jose", 	"");
		crearUsuario("100-02", "102", "jaime", 	"");
		crearUsuario("200-01", "201", "jorge",	"");
		crearUsuario("200-02", "202", "juan", 	"");

	}	
	
}
