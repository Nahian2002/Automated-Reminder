package AutomatedReminder;

import javax.swing.*;

public class DailyReminder extends Reminder{
    public DailyReminder(String folder){
        super(folder, DAY_MILLIS);
    }

    public DailyReminder(String folder, long lastTime){
        super(folder, DAY_MILLIS, lastTime);
    }

    @Override
    public void showReminder(JFrame parent){
        JOptionPane.showMessageDialog(parent,
                "Daily reminder to backup: " + folder,
                "Backup Reminder",
                JOptionPane.INFORMATION_MESSAGE);
    }
}