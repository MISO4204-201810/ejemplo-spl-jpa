/*
Copyright 2012 Will Provost.
All rights reserved by Capstone Courseware, LLC.
*/

package cc.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
Utility to determine the JPA provider in use at runtime.
The technique is to determine the class of the entity manager's "delegate,"
and translate that to an {@link cc.jpa.ProviderType enumerated value}.
This is naturally a bit fragile as the class names may change over time.
Currently working with EclipseLink 2.3 and Hibernate 4.0.

@author Will Provost
*/
public class ProviderUtil
{
    /**
    Returns a short name identifying the JPA provider for the given 
    entity manager.
    */
    public static ProviderType getProvider (EntityManager em)
    {
        return JPAUtil.getProvider (em);
    }
        
    /**
    Returns a short name identifying the JPA provider for the given 
    persistence unit name. This involves creating an entity manager factory
    and an entity manager and closing them again.
    */
    public static ProviderType getProvider (String persistenceUnitName)
    {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        
        try
        {
            emf = Persistence.createEntityManagerFactory (persistenceUnitName);
            em = emf.createEntityManager ();
            return getProvider (em);
        }
        catch (Exception ex) {}
        finally
        {
            if (em != null)
                em.close ();
            if (emf != null)
                emf.close ();
        }
        
        return null;
    }

    /**
    Writes a diagnostic to standard output identifying the JPA provider.
    */
    public static void reportProvider (EntityManager em)
    {
        System.out.println ("Using " + getProvider (em) + ".");
        System.out.println ();
    }
        
    /**
    Writes a diagnostic to standard output identifying the JPA provider.
    */
    public static void reportProvider (String persistenceUnitName)
    {
        System.out.println ("Using " + getProvider (persistenceUnitName) +
            " for persistence unit \"" + persistenceUnitName + "\".");
        System.out.println ();
    }
        
    /**
    An application wrapper around {@link #reportProvider reportProvider}.
    */
    public static void main (String[] args)
    {
        if (args.length == 0)
        {
            System.out.println 
                ("Usage: java cc.jpa.ProviderUtil <persistence-unit-name>");
            System.exit (-1);
        }
    
        reportProvider (args[0]);
    }
}
