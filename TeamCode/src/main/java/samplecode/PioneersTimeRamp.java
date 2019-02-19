package samplecode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;

/**
 * Created by phurley on 10/27/17.
 */
@Disabled()
public class PioneersTimeRamp {
    private double start, finish, range;
    private long duration;
    private PioneersRampable target;
    private long start_time, stop_time;
    private Runnable at_finish;

    public PioneersTimeRamp(double start, double finish, long ms_duration, PioneersRampable target) {
        start_time = System.currentTimeMillis();
        this.target = target;
        this.at_finish = null;

        target.tick(start);

        this.start = start;
        this.finish = finish;
        this.duration = ms_duration;
        if (duration == 0) {
            duration = 1;
        }
        stop_time = start_time + ms_duration;
        range = finish - start;
    }

    public void setAtFinish(Runnable action) {
        at_finish = action;
    }

    public void loop() {
        long now = System.currentTimeMillis();

        if (now >= stop_time) {
            target.tick(finish);
            if (at_finish != null) {
                at_finish.run();
                at_finish = null;
            }
        } else {
            long elapse = now - start_time;
            double percent = ((double) elapse) / ((double) duration);
            target.tick(start + (range * percent));
        }
    }

    public void reset(double start, double finish, long ms_duration) {
        start_time = System.currentTimeMillis();
        target.tick(start);
        at_finish = null;

        this.start = start;
        this.finish = finish;
        this.duration = ms_duration;
        stop_time = start_time + ms_duration;
        range = finish - start;
    }

    public boolean finished() {
        return System.currentTimeMillis() >= stop_time;
    }
}