package datos.modelo.pruebas;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import datos.modelo.AbstractUsuario;


public class VerTodosLosUsuarios {

	public static void main(String[] args) {

		// == Cargar los datos de prueba
		
		DatosPrueba.borrarDatosPrueba();
		DatosPrueba.crearDatosPrueba();
		
		// == Conectar a la base de datos  		
		
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("test_PU");
		EntityManager manager = factory.createEntityManager();
		
		// == Consultas de prueba
					
		String jpql = "select u from Usuario u";
		TypedQuery<AbstractUsuario> consulta = manager.createQuery( jpql, AbstractUsuario.class );
		
		List<AbstractUsuario> listaUsuarios = consulta.getResultList();
		
		for ( AbstractUsuario usuario : listaUsuarios) {
			System.out.println( usuario.getNombre() );			
		}
		
		System.out.println(".. done");
	}

}
