package com.limpygnome.parrot.component;

import org.springframework.stereotype.Component;

/**
 * A component with useful memory security operations.
 */
@Component
public class MemoryCompoonent
{

    /**
     * Wipes the specified array.
     *
     * @param array target to be zero-written
     */
    public void wipe(char[] array)
    {
        for (int i = 0; i < array.length; i++)
        {
            array[i] = 0;
        }
    }

}
