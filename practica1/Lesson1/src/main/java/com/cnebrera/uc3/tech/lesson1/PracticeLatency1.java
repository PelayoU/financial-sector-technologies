package com.cnebrera.uc3.tech.lesson1;

import com.cnebrera.uc3.tech.lesson1.simulator.BaseSyncOpSimulator;
import com.cnebrera.uc3.tech.lesson1.simulator.SyncOpSimulRndPark;
import org.HdrHistogram.Histogram;

import java.util.concurrent.TimeUnit;

/**
 * First practice, measure latency on a simple operation
 */
public class PracticeLatency1
{
    /**
     * Main method to run the practice
     * @param args command line arument
     */
    public static void main(String [] args)
    {
        runCalculations();
    }

    /**
     * Run the practice calculations
     */


    private static void runCalculations()
    {

        // TODO Create an empty Histogram

        int significantValueDigits = 3;
        long lowestDiscernibleValue = 100;

        //El maximo con autoresize me salió de 135 micros. Pondré 250 para dar algo de margen
        long highestTrackableValue = 250000L;

        //Histogram histogram = new Histogram(3);
        //histogram.setAutoResize(true);


        Histogram histogram = new Histogram(
                lowestDiscernibleValue,
                highestTrackableValue,
                significantValueDigits);



        // Create a random park time simulator
        BaseSyncOpSimulator syncOpSimulator = new SyncOpSimulRndPark(
                TimeUnit.NANOSECONDS.toNanos(100),
                TimeUnit.MICROSECONDS.toNanos(100)); //pasamos 100microsegundos a nano segundos 100 micro = 100000 (100 mil nanos)





        // Execute the operation lot of times
        for(int i = 0; i < 100000; i++)
        {
            // TODO Compute latency for each operation

            long startTime = System.nanoTime();
            syncOpSimulator.executeOp();


            //TODO Record latency into histogram
            histogram.recordValue(System.nanoTime()- startTime);
        }

        // TODO Show the percentile distribution of the latency calculation of each executeOp call

        //Escala de 1000 nanosegundos, que son 1 microsegundo.
        histogram.outputPercentileDistribution(System.out, 1000.0);

    }


}
