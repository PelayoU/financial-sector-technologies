package com.cnebrera.uc3.tech.lesson3;

import java.util.concurrent.TimeUnit;

public final class Constants {

    private Constants() {
        // restrict instantiation
    }


    /*BATERIA 1


    static final int MAX_EXECUTIONS_PER_SECOND = 1000;
    static final long EXPECTED_TIME_BETWEEN_CALLS = TimeUnit.SECONDS.toMillis(1) / MAX_EXECUTIONS_PER_SECOND;
    static final int NUM_MESSAGES = 20000;
    */

    /*BATERIA 2
    static final int MAX_EXECUTIONS_PER_SECOND = 50000;
    static final long EXPECTED_TIME_BETWEEN_CALLS = TimeUnit.SECONDS.toMillis(1) / MAX_EXECUTIONS_PER_SECOND;
    static final int NUM_MESSAGES = 20000;

    */
    //BATERIA 3

    static final int MAX_EXECUTIONS_PER_SECOND = 100000;
    static final long EXPECTED_TIME_BETWEEN_CALLS = TimeUnit.SECONDS.toMillis(1) / MAX_EXECUTIONS_PER_SECOND;
    static final int NUM_MESSAGES = 200000;


    //ipc channel = "aeron:ipc"
    //multicast CHANNEL = "aeron:udp?endpoint=224.0.1.1:40456"
    //unicast CHANNEL = "aeron:udp?endpoint=localhost:40456"
    static final String CHANNEL = "aeron:ipc";
    static final int STREAM_ID = 1001;



}
