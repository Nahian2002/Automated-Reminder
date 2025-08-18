package AutomatedReminder;

import javax.swing.*;

public abstract class Reminder{
    public static final long MINUTE_MILLIS = 60_000L;
    public static final long DAY_MILLIS = 24*60*MINUTE_MILLIS;
    public static final long WEEK_MILLIS = 7*DAY_MILLIS;

    protected final String folder;
    protected final long intervalMillis;
    private long lastTime;

    protected Reminder(String folder, long intervalMillis){
        this(folder, intervalMillis, System.currentTimeMillis());
    }

    protected Reminder(String folder, long intervalMillis, long lastTime){
        this.folder = folder;
        this.intervalMillis = intervalMillis;
        this.lastTime = lastTime;
    }

    public boolean isDue(long now){
        return now - lastTime >= intervalMillis;
    }

    public void reset(long now){
        this.lastTime = now;
    }

    public long getLastTime(){
        return lastTime;
    }

    public String getFolder(){
        return folder;
    }

    public long getIntervalMillis(){
        return intervalMillis;
    }

    public String getIntervalName(){
        if (intervalMillis == DAY_MILLIS)
            return "Daily";
        if (intervalMillis == WEEK_MILLIS)
            return "Weekly";
        long mins = intervalMillis/MINUTE_MILLIS;
        return mins + " min";
    }

    @Override
    public String toString(){
        return folder+" ("+getIntervalName()+")";
    }

    public abstract void showReminder(JFrame parent);
}