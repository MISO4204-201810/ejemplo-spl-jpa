package datos.utils;

import java.io.Serializable;
import java.util.List;

public interface GenericDAO<T, ID extends Serializable> {
	 
	// == busquedas
	
	T findById(ID id);
	T findById(ID id, boolean lock);
    List<T> findAll();
 
    // == CRUD
    
    T create(T entity);
    T update(T entity);    
    void delete(T entity);
    
    // == Transacciones
    
    public void beginTransaction();
    public void commit();
    public void rollback();
    
}