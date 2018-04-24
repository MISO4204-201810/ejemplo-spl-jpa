/*
 * JPQLCommon.java
 *
 * Copyright (c) 2008-2012 Edward Rayl. All rights reserved.
 *
 * JPQLCommon is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPQLCommon is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPQL.  If not, see <http://www.gnu.org/licenses/>.
 */

package cc.jpa;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isInterface;
import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isProtected;
import static java.lang.reflect.Modifier.isPublic;
import static cc.jpa.JPQLIO.println;
import static cc.jpa.JPQLIO.print;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;

/**
 * Common helper methods for the JPQL and JPQLe tools.
 * 
 * @author Edward Rayl
 */
public class JPQLCommon
{
    /**
     * Describe a given entity.
     */
    static void printDescription(String name, boolean all, Set<ManagedType<?>> managedClasses) 
        throws ClassNotFoundException
    {
        Class<?> className = null;
        for (final ManagedType<?> entity : managedClasses)
        {
            if (entity.getJavaType().getSimpleName().equals(name))
                className = entity.getJavaType();
        }
    
        if (className == null)
            throw new ClassNotFoundException();
    
        final String canonicalName = className.getCanonicalName();
        final String packageName = className.getPackage().getName();
    
        if (all)
        {
            // Package
            println("package " + packageName + ";");
            println();
    
            // Class annotations
            Annotation[] annotations = null;
            String s = null;
            annotations = className.getAnnotations();
            if (all && annotations.length > 0)
                for (final Annotation annotation : annotations)
                {
                    s = annotation.toString();
                    s = s.replaceAll("javax.validation.constraints.", "");
                    s = s.replaceAll("javax.persistence.", "");
                    s = s.replaceAll("\\s*?\\w*?=,\\s*?", "");
                    s = s.replaceAll("\\w*?=\\)", ")");
                    s = s.replaceAll(",\\s\\)", ")");
                    if (s.startsWith("@NamedQueries"))
                        s = s.replaceAll("@NamedQuery", "\n  @NamedQuery");
                    println(s);
                }
        }
    
        // Class name
        final int modifiers = className.getModifiers();
        if (isPublic(modifiers))
            print("public ");
        if (isFinal(modifiers))
            print("final ");
        if (isAbstract(modifiers))
            print("abstract ");
        if (isInterface(modifiers))
            print("interface ");
        print("class " + className.getSimpleName());
        Class<?> superclass = className.getSuperclass();
        String superclassName = superclass.getSimpleName();
        if (!superclassName.equals("Object"))
            print(" extends " + superclassName);
        println();
    
        // Field annotations
        Field[] fields = className.getDeclaredFields();
        if (fields.length > 0)
            printMembers("Fields:", fields, packageName, canonicalName, all);
    
        // Superclass field annotations
        while (!superclassName.equals("Object"))
        {
            fields = superclass.getDeclaredFields();
            if (fields.length > 0)
                printMembers(superclass.getSimpleName() + " Fields:", fields,
                    superclass.getPackage().getName(), superclassName, all);
            superclass = superclass.getSuperclass();
            superclassName = superclass.getSimpleName();
        }
    
        if (all)
        {
            // Constructor annotations
            final Constructor<?>[] constructors = className.getDeclaredConstructors();
            if (constructors.length > 0)
                printMembers("Constructors:", constructors, packageName, canonicalName, all);
    
            // Method annotations
            Method[] methods = className.getDeclaredMethods();
            if (methods.length > 0)
                printMembers("Methods:", methods, packageName, canonicalName, all);
    
            // Superclass method annotations
            superclass = className.getSuperclass();
            superclassName = superclass.getSimpleName();
            while (!superclassName.equals("Object"))
            {
                methods = superclass.getDeclaredMethods();
                if (methods.length > 0)
                    printMembers(superclass.getSimpleName() + " Methods:", methods,
                        superclass.getPackage().getName(), superclassName, all);
                superclass = superclass.getSuperclass();
                superclassName = superclass.getSimpleName();
            }
        }
    }

    /**
     * Produce a formatted list of the given members.
     */
    private static void printMembers(String name, AccessibleObject[] members, String packageName, String canonicalName, boolean all)
    {
        println(name);
        for (final AccessibleObject member : members)
        {
            if (all && (member instanceof Field || member instanceof Method))
            {
                String s = null;
                final Annotation[] annotations = member.getAnnotations();
                if (annotations.length > 0)
                    for (final Annotation annotation : annotations)
                    {
                        s = annotation.toString();
                        s = s.replaceAll("javax.persistence.", "");
                        s = s.replaceAll("javax.validation.constraints.", "");
                        s = s.replaceAll("\\s*?\\w*?=,\\s*?", "");
                        s = s.replaceAll("\\w*?=\\)", ")");
                        s = s.replaceAll(",\\s\\)", ")");
                        println("  " + s);
                    }
            }
    
            if (member instanceof Field)
            {
                String s = "  ";
                s += ((Field) member).toGenericString();
                // Remove the package name from the field type
                if (!((Field) member).getType().isPrimitive())
                    s = s.replaceFirst(((Field) member).getType().getPackage().getName() + "\\.", "");
                s = s.replaceAll(canonicalName + "\\.", "");
                s = s.replaceAll(packageName + "\\.", "");
                println(s);
            }
            if (all)
            {
                if (member instanceof Method)
                {
                    // Class name
                    print("  ");
                    final int modifiers = ((Method) member).getModifiers();
                    if (isPublic(modifiers))
                        print("public ");
                    if (isPrivate(modifiers))
                        print("private ");
                    if (isProtected(modifiers))
                        print("protected ");
                    print(((Method) member).getName() + "(");
                    // Parameters
                    Class<?>[] types = ((Method) member).getParameterTypes();
                    for (int i = 0; i < types.length; i++)
                    {
                        if (types[i] != null && !types[i].isPrimitive())
                        {
                            String className = types[i].getSimpleName();
                            print(className);
                            if (i < types.length - 1)
                                print(", ");
                        }
                    }
                    println(")");
                }
                else if (member instanceof Constructor)
                {
                    // Class name
                    print("  ");
                    final int modifiers = ((Constructor<?>) member).getModifiers();
                    if (isPublic(modifiers))
                        print("public ");
                    if (isPrivate(modifiers))
                        print("private ");
                    if (isProtected(modifiers))
                        print("protected ");
                    print(((Constructor<?>) member).getName().replaceAll(packageName + "\\.", "") + "(");
                    // Parameters
                    Class<?>[] types = ((Constructor<?>) member).getParameterTypes();
                    for (int i = 0; i < types.length; i++)
                    {
                        if (types[i] != null && !types[i].isPrimitive())
                        {
                            String className = types[i].getSimpleName();
                            print(className);
                            if (i < types.length - 1)
                                print(", ");
                        }
                    }
                    println(")");
                }
            }
        }
        println();
    }

    /**
     * Format a list of given managed classes.
     */
    static void printClasses(boolean showPackage, Set<ManagedType<?>> managedClasses)
    {
        final SortedSet<String> names = new TreeSet<String>();
    
        for (final ManagedType<?> entity : managedClasses)
        {
            if (entity instanceof EntityType)
            {
                if (showPackage)
                    names.add(entity.getJavaType().getCanonicalName());
                else
                    names.add(entity.getJavaType().getSimpleName());
    
                for (final Attribute<?, ?> attr : entity.getAttributes())
                {
                    if (attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED)
                        if (showPackage)
                            names.add(entity.getJavaType().getCanonicalName() + "." + attr.getName() + " (@Embedded)");
                        else
                            names.add(entity.getJavaType().getSimpleName() + "." + attr.getName() + " (@Embedded)");
                }
            }
            else if (entity instanceof EmbeddableType)
            {
                if (showPackage)
                    names.add(entity.getJavaType().getCanonicalName() + " (@Embeddable)");
                else
                    names.add(entity.getJavaType().getSimpleName() + " (@Embeddable)");
            }
    
        }
    
        for (final String name : names)
        {
            println(name);
        }
        println();
    }

    /**
     * Produce a user-friendly representation of the underlying cause
     * of a given exception.
     */
    static void printCause(Exception e)
    {
        final Throwable c = e.getCause();
        if (c != null)
        {
            String cause = c.getMessage();
            if (cause.contains("Unknown entity type") ||
                cause.contains("is not mapped"))
            {
                println(c.getMessage());
                println("Possible misspelling of entity name.");
                println("Please make sure all entities are on the classpath.");
            }
            // Hibernate specific
            else if (cause.equals("could not execute query") ||
                     cause.startsWith("could not resolve property"))
            {
                println("Hibernate SQL grammar exception: " + c.getMessage());
                println();
            }
            else
            {
                cause = cause.substring(cause.indexOf("Exception Description:") + 22);
                final int start = cause.indexOf("column ") + 7;
                final int end = cause.indexOf(":", start);
                if (start > 6 && end > start)
                {
                    final String location = cause.substring(start, end);
                    final int column = Integer.parseInt(location) + cause.indexOf('[') + 1;
                    String spaces = "";
                    for (int i = 1; i < column; i++)
                        spaces += " ";
                    println(cause.substring(0, cause.indexOf(':')));
                    println(spaces + "*");
                    println(cause.substring(cause.indexOf(':') + 2));
                }
                else
                {
                    cause = cause.replaceAll(":", "\n");
                    println(cause);
                    println();
                }
            }
        }
        else
            println(e.getMessage());
    }
}
