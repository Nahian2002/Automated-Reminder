package AutomatedReminder;

import javax.swing.*;

public class WeeklyReminder extends Reminder{
    public WeeklyReminder(String folder){
        super(folder, WEEK_MILLIS);
    }

    public WeeklyReminder(String folder, long lastTime){
        super(folder, WEEK_MILLIS, lastTime);
    }

    @Override
    public void showReminder(JFrame parent){
        JOptionPane.showMessageDialog(parent,
                "Weekly reminder to backup: " + folder,
                "Backup Reminder",
                JOptionPane.INFORMATION_MESSAGE);
    }
}