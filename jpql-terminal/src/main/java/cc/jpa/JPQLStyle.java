/*
 * JPQLStyle.java
 *
 * Copyright (c) 2008-2011 Edward Rayl. All rights reserved.
 *
 * JPQL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPQL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPQL.  If not, see <http://www.gnu.org/licenses/>.
 */

/* 
 Sample output:

 Assignment[
 id=1, 
 employee=John Gonzales, 
 project=cc.hr.entity.Project@6f03de90, 
 startDate=1939-10-30, 
 endDate=1984-08-15
 ]

 */

package cc.jpa;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * JPQLStyle is used by Apache's ReflectionToStringBuilder to format query output. In particular,
 * JPQLStyle contains all formatting choices that require special handling for proper display in
 * JPQL.
 * 
 * @author Edward Rayl
 */
@SuppressWarnings("serial")
public class JPQLStyle
    extends ToStringStyle
{
    private final static ToStringStyle instance = new JPQLStyle();

    /**
     * JPQLStyle makes calls to ToStringStyle methods to modify defaults.
     */
    public JPQLStyle()
    {
        this.setArrayContentDetail(true);
        this.setUseShortClassName(true);
        this.setUseIdentityHashCode(false);
        this.setFieldSeparator(SystemUtils.LINE_SEPARATOR + "  ");
        this.setContentStart("[" + SystemUtils.LINE_SEPARATOR + "  ");
        this.setContentEnd(SystemUtils.LINE_SEPARATOR + "]");
    }

    /**
     * Return a JPA Style instance.
     * 
     * @return JPQLStyle instance.
     */
    public static ToStringStyle getInstance()
    {
        return instance;
    };

    /*
     * (non-Javadoc)
     * 
     * Fix Calendar date display
     * 
     * @see org.apache.commons.lang.builder.ToStringStyle#appendDetail(java.lang. StringBuffer,
     * java.lang.String, java.lang.Object)
     */
    @Override
    protected void appendDetail(StringBuffer buffer, String fieldName, Object value)
    {
        if (value instanceof Date)
        {
            value = new SimpleDateFormat("yyyy-MM-dd").format(value);
        }
        else if (value instanceof GregorianCalendar)
        {
            value = new SimpleDateFormat("yyyy-MM-dd").format(((GregorianCalendar) value).getTime());
        }
        buffer.append(value);
    }
}
