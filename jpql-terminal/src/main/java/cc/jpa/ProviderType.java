/*
 * ProviderType.java
 *
 * Copyright (c) 2008-2012 Edward Rayl. All rights reserved.
 *
 * ProviderType is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProviderType is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProviderType.  If not, see <http://www.gnu.org/licenses/>.
 */

package cc.jpa;

/**
 * Supported ProviderTypes are EclipseLink, Hibernate, and OpenJPA. Others can be added as
 * necessary.
 * 
 * @author Edward Rayl
 * 
 */
public enum ProviderType
{
    /**
     * Unknown provider
     */
    UNKNOWN("Unknown", ""),
    
    /**
     * EclipseLink
     */
    ECLIPSELINK("EclipseLink", "org.eclipse.persistence.internal.jpa.EntityManagerImpl"),

    /**
     * OpenJPA
     */
    OPENJPA("OpenJPA", "org.apache.openjpa.persistence.EntityManagerImpl"),

    /**
     * Hibernate 3
     */
    HIBERNATE_3("Hibernate", "org.hibernate.Session"),

    /**
     * Hibernate 4
     */
    HIBERNATE_4("Hibernate", "org.hibernate.internal.SessionImpl");
    
    private String providerName;
    private String providerString;
    
    private ProviderType(String providerName, String providerString)
    {
        this.providerName = providerName;
        this.providerString = providerString;
    }
    
    public String getProviderString()
    {
        return providerString;
    }
    
    @Override
    public String toString()
    {
        return providerName;
    }
}
