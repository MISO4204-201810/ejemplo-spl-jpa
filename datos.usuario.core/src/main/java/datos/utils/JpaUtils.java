package datos.utils;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

public class JpaUtils {
	
	private static EntityManagerFactory factory;
	
	public static EntityManager getEntityManager(String unitName) {
		
		// captura los errores al conectars a la base de datos 
		try {
			// inicia la conexión
			factory = Persistence.createEntityManagerFactory( unitName );
			EntityManager em = factory.createEntityManager();
			
			return em;
			
		// no se pudo conectar ??
		} catch (Exception e) {
			// error al crear la conexión
			System.out.println("Error creando conexión : " + e.getMessage());
			e.printStackTrace();
			
			throw new RuntimeException("Error creando conexión : " + e.getMessage(), e);
		}
	}
	
	public static EntityManagerFactory getEntityManagerFactory() {
		return factory;
	}
	
	public static Object executeSingleResultNamedQuery(EntityManager em, String namedQuery) {
		Query consulta = em.createNamedQuery(namedQuery);
		Object resultado = consulta.getSingleResult();
		return resultado;	
	}

	public static Object executeSingleResultNamedQuery(EntityManager em, String namedQuery, Object... params) {
		Query consulta = em.createNamedQuery(namedQuery);
		int paramNumber = 0;
		for(Object param: params) {
			consulta.setParameter(paramNumber++, param);
		}
		Object resultado = consulta.getSingleResult();
		return resultado;	
	}

	public static Object executeSingleResultNamedQuery(EntityManager em, String namedQuery, Map<String, Object> params) {
		Query consulta = em.createNamedQuery(namedQuery);
		for(String paramName: params.keySet()) {
			consulta.setParameter(paramName, params.get(paramName));
		}
		Object resultado = consulta.getSingleResult();
		return resultado;	
	}
	
	
	@SuppressWarnings( "rawtypes" )
	public static List executeListResultNamedQuery(EntityManager em, String namedQuery) {
		Query consulta = em.createNamedQuery(namedQuery);
		List resultado = consulta.getResultList();
		return resultado;	
	}

	@SuppressWarnings( "rawtypes" )
	public static List executeListResultNamedQuery(EntityManager em, String namedQuery, Object... params) {
		Query consulta = em.createNamedQuery(namedQuery);
		int paramNumber = 0;
		for(Object param: params) {
			consulta.setParameter(paramNumber++, param);
		}
		List resultado = consulta.getResultList();
		return resultado;	
	}

	@SuppressWarnings( "rawtypes" )
	public static List executeListResultNamedQuery(EntityManager em, String namedQuery, Map<String, Object> params) {
		Query consulta = em.createNamedQuery(namedQuery);
		for(String paramName: params.keySet()) {
			consulta.setParameter(paramName, params.get(paramName));
		}
		List resultado = consulta.getResultList();
		return resultado;	
	}

	
	public static Object executeSingleResultQuery(EntityManager em, String query) {
		Query consulta = em.createQuery(query);
		Object resultado = consulta.getSingleResult();
		return resultado;	
	}

	public static Object executeSingleResultQuery(EntityManager em, String query, Object... params) {
		Query consulta = em.createQuery(query);
		int paramNumber = 0;
		for(Object param: params) {
			consulta.setParameter(paramNumber++, param);
		}
		Object resultado = consulta.getSingleResult();
		return resultado;	
	}

	public static Object executeSingleResultQuery(EntityManager em, String query, Map<String, Object> params) {
		Query consulta = em.createNamedQuery(query);
		for(String paramName: params.keySet()) {
			consulta.setParameter(paramName, params.get(paramName));
		}
		Object resultado = consulta.getSingleResult();
		return resultado;	
	}	
	
	@SuppressWarnings( "rawtypes" )
	public static List executeListResultQuery(EntityManager em, String query) {
		Query consulta = em.createQuery(query);
		List resultado = consulta.getResultList();
		return resultado;	
	}

	@SuppressWarnings( "rawtypes" )
	public static List executeListResultQuery(EntityManager em, String query, Object... params) {
		Query consulta = em.createQuery(query);
		int paramNumber = 0;
		for(Object param: params) {
			consulta.setParameter(paramNumber++, param);
		}
		List resultado = consulta.getResultList();
		return resultado;	
	}
	
	@SuppressWarnings( "rawtypes" )
	public static List executeListResultQuery(EntityManager em, String query, Map<String, Object> params) {
		Query consulta = em.createNamedQuery(query);
		for(String paramName: params.keySet()) {
			consulta.setParameter(paramName, params.get(paramName));
		}
		List resultado = consulta.getResultList();
		return resultado;	
	}
}
