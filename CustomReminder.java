package AutomatedReminder;

import javax.swing.*;

public class CustomReminder extends Reminder{
    public CustomReminder(String folder, int minutes){
        super(folder, minutes * MINUTE_MILLIS);
    }

    public CustomReminder(String folder, int minutes, long lastTime){
        super(folder, minutes * MINUTE_MILLIS, lastTime);
    }

    @Override
    public void showReminder(JFrame parent){
        JOptionPane.showMessageDialog(parent,
                "Custom reminder to backup: " + folder,
                "Backup Reminder",
                JOptionPane.INFORMATION_MESSAGE);
    }
}