package uk.ac.ucl.util.core;

import java.time.Duration;
import java.time.Instant;

/**
 * TimeUtil aims to fidn the interval between two instants in millisecond.
 */
public class TimeUtil {
    Instant start;
    public TimeUtil(){
        start = Instant.now();
    }

    public long interval(){
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        return duration.toMillis();
    }

}
