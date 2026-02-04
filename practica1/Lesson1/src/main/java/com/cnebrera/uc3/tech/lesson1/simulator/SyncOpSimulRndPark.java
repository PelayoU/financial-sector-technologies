package com.cnebrera.uc3.tech.lesson1.simulator;

import java.util.Random;

/**
 * Simulates an operation that takes a random number of time between the given values in nanoseconds
 */
public class SyncOpSimulRndPark extends BaseSyncOpSimulator
{
    final Random rnd = new Random();
    final long minParkTimeNanos;
    final long maxParkTimeNanos;

    public SyncOpSimulRndPark(long minParkTimeNanos, long maxParkTimeNanos)
    {
        this.minParkTimeNanos = minParkTimeNanos;
        this.maxParkTimeNanos = maxParkTimeNanos;
    }

    public void executeOp()
    {
        // Get a random park time
        final long newParkTime = Math.abs(rnd.nextLong() % maxParkTimeNanos) + minParkTimeNanos;
        //rnad.nextlon() %maxParkTimeNanos me saca un numero aleatorio en el intervalo [0, maxParkTimeNanos-1]
        //al sumarle + minParkTimeNanos me sale un numero aleatorio logicamente entre [minParkTimeNanos, maxParkTImeNanos-1]


        // Park
        long startTime = System.nanoTime(); //Tiempo actual en nanosegundos (desde un punto fijo)

        while(System.nanoTime() - startTime < newParkTime);
        //Esta linea es un bucle infinito, cada vez que la cpu ejectua esta linea toma tiempo, en el momento en el que
        //haya pasado mas tiempo que el newParkTIme parara. Es decir, todo este progrma sirve para que al ejecutar esta funcion
        //tome un tiempo aleatorio ejecutarla

    }
}
