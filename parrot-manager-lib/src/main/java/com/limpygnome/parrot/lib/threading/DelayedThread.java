package com.limpygnome.parrot.lib.threading;

/**
 * Manages executing and cancelling delayed threads.
 */
public class DelayedThread
{
    private Thread thread;

    /**
     * Starts thread with delay.
     *
     * If another thread is currently executing, that thread is interrupted to cancel execution.
     *
     * @param runnable execution for after delay period
     * @param delay delay period in milliseconds
     */
    public synchronized void start(Runnable runnable, long delay)
    {
        // Cancel previous thread
        cancel();

        // Start new thread
        thread = new Thread(() ->
        {
            try
            {
                // Sleep for delay
                Thread.sleep(delay);

                // Perform actual execution
                runnable.run();

                // Dispose self
                synchronized (this)
                {
                    thread = null;
                }
            }
            catch (InterruptedException e)
            {
                // Expected and can be unhandled
            }
        });

        thread.start();
    }

    /**
     * Cancels the the thread currently running.
     *
     * This can be safely invoked if no thread is running.
     */
    public synchronized void cancel()
    {
        if (thread != null)
        {
            thread.interrupt();
            thread = null;
        }
    }


}
