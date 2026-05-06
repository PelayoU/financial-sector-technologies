package com.cnebrera.uc3.tech.lesson1.simulator;

/**
 * Simulates an operation that require the given sleep time in milliseconds
 */
public class SyncOpSimulSleep extends BaseSyncOpSimulator
{
    final long sleepTime;

    public SyncOpSimulSleep(long sleepTime)
    {
        this.sleepTime = sleepTime;
    }



    //El metodo synchronized significa que solo un hilo puede estar dentro de este metodo al mismo tiempo.
    //Si otro hilo intenta llamar executeOp mientras uno ya lo esta ejecutando,
    //el segundo se bloquea y espera a que el primero termine.
    public synchronized void executeOp()
    {
        try
        {
            Thread.sleep(sleepTime);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
