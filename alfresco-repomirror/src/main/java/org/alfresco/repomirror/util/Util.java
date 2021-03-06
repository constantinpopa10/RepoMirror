/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repomirror.util;

/**
 * 
 * @author sglover
 *
 */
public class Util
{
    /**
     * Checks if argument is not null.
     * 
     * @param argument_p
     *            (Object) argument to validate
     * @param argumentName_p
     *            (String) name of original argument in calling method
     * 
     * @throws IllegalArgumentException
     */
    public static void checkArgumentNotNull(Object argument_p, String argumentName_p) throws IllegalArgumentException
    {
        checkStringNotNullOrEmpty(argumentName_p, "argumentName_p");

        if (null == argument_p)
        {
            throw new IllegalArgumentException("Argument '" + argumentName_p + "' is mandataory.");
        }
    }

    /**
     * Checks string argument not to be null or empty
     * 
     * @param argument_p
     *            (String) argument to validate
     * @param argumentName_p
     *            (String) name of original argument in calling method
     * 
     * @throws IllegalArgumentException
     */
    public static void checkStringNotNullOrEmpty(String argument_p, String argumentName_p)
            throws IllegalArgumentException
    {
        if (null == argument_p || argument_p.isEmpty())
        {
            throw new IllegalArgumentException("Argument '" + argumentName_p + "' is mandataory.");
        }
    }
}
