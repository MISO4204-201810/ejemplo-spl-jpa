/*
 * JPAUtil.java
 *
 * Copyright (c) 2008-2012 Edward Rayl. All rights reserved.
 *
 * JPAUtil is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPAUtil is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPAUtil.  If not, see <http://www.gnu.org/licenses/>.
 */

package cc.jpa;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

/**
 * Utility for detecting the JPA provider. 
 * 
 * @author Edward Rayl
 */
public class JPAUtil
{
    /**
     * Derives a set of all managed types for a given persistence unit.
     */
    public static Set<ManagedType<?>> getManagedClassTypes(EntityManagerFactory emf)
    {
        final Metamodel metamodel = emf.getMetamodel();
        final Set<ManagedType<?>> managedClasses = new HashSet<ManagedType<?>>();
        final Set<EntityType<?>> entityTypes = metamodel.getEntities();
        final Set<EmbeddableType<?>> embeddableTypes = metamodel.getEmbeddables();
        for (final EntityType<?> entityType : entityTypes)
            managedClasses.add(entityType);
    
        for (final EmbeddableType<?> embeddableType : embeddableTypes)
            managedClasses.add(embeddableType);
    
        return managedClasses;
    }

    /**
     * Identifies the JPA provider implementing a given entity manager.
     */
    public static ProviderType getProvider(EntityManager em)
    {
        String provider = em.getDelegate().getClass().getName();
        for(ProviderType value : ProviderType.values())
        {
            if (provider.equals(value.getProviderString()))
                return value;
        }
        return ProviderType.UNKNOWN;
    }    
}
