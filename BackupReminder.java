package AutomatedReminder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class BackupReminder extends JFrame{
    private final DefaultListModel<Reminder> model = new DefaultListModel<>();
    private final JList<Reminder> list = new JList<>(model);

    private final JTextField folderInput = new JTextField(24);
    private final JComboBox<String> intervalInput = new JComboBox<>(new String[]{"Daily", "Weekly", "Custom"});
    private final JLabel minutesLabel = new JLabel("Minutes:");
    private final JTextField customMinutesInput = new JTextField(6);
    private final JButton chooseFolderBtn = new JButton("Browse…");
    private final JButton addBtn = new JButton("Add");
    private final JButton delBtn = new JButton("Delete");

    private final File store = new File("reminders.txt");

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());

    public BackupReminder(){
        super("Backup Reminder");

        buildUI();
        wireEvents();
        loadRemindersSafe();

        scheduler.scheduleAtFixedRate(this::tick,5,60,TimeUnit.SECONDS);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(560, 420);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter(){
            @Override public void windowClosing(WindowEvent e){
                scheduler.shutdownNow();
                try {saveRemindersSafe();} 
                catch (Exception ignored) {}
            }
        });
    }

    private void buildUI(){
        JPanel root = new JPanel(new BorderLayout(10,10));
        root.setBorder(new EmptyBorder(12,12,12,12));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row1.add(new JLabel("Folder:"));
        row1.add(folderInput);
        row1.add(chooseFolderBtn);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row2.add(new JLabel("Interval:"));
        row2.add(intervalInput);
        row2.add(minutesLabel);
        row2.add(customMinutesInput);

        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row3.add(addBtn);
        row3.add(delBtn);

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(row1);
        top.add(row2);
        top.add(row3);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(list);

        root.add(top, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        setContentPane(root);

        updateCustomVisibility();
    }

    private void wireEvents(){
        intervalInput.addActionListener(e -> updateCustomVisibility());

        chooseFolderBtn.addActionListener(e ->{
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Select a folder to back up");
            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION){
                File dir = chooser.getSelectedFile();
                if (dir != null){
                    folderInput.setText(dir.getAbsolutePath());
                }
            }
        });

        addBtn.addActionListener(e -> SwingUtilities.invokeLater(this::addReminderSafe));
        delBtn.addActionListener(e -> SwingUtilities.invokeLater(this::deleteSelectedReminderSafe));
    }

    private void updateCustomVisibility(){
        boolean custom = Objects.equals(intervalInput.getSelectedItem(),"Custom");
        minutesLabel.setVisible(custom);
        customMinutesInput.setVisible(custom);
        if (!custom) customMinutesInput.setText("");
        revalidate();
        repaint();
    }

    private void tick(){
        long now = System.currentTimeMillis();
        SwingUtilities.invokeLater(() -> {
            try {
                for (int i = 0; i < model.size(); i++){
                    Reminder r = model.get(i);
                    if (r.isDue(now)){
                        try {
                            r.showReminder(this);
                        } catch (Throwable t){
                            showError("Failed to show reminder:\n" + t.getMessage());
                        }
                        r.reset(now);
                    }
                }
            } catch (Throwable t){
                System.err.println("Tick error: " + t);
            }
        });
    }

    private void addReminderSafe(){
        String folder = folderInput.getText().trim();
        if (folder.isEmpty()){
            showError("Folder path cannot be empty.");
            return;
        }
        File dir = new File(folder);
        if (!dir.isDirectory()){
            showError("The path does not exist or is not a directory.");
            return;
        }

        String sel = String.valueOf(intervalInput.getSelectedItem());
        Reminder newR;
        switch (sel){
            case "Daily":
                newR = new DailyReminder(folder);
                break;
            case "Weekly":
                newR = new WeeklyReminder(folder);
                break;
            default:
                int minutes;
                try{
                    minutes = Integer.parseInt(customMinutesInput.getText().trim());
                } catch (NumberFormatException ex){
                    showError("Please enter a valid integer number of minutes.");
                    return;
                }
                if (minutes <= 0){
                    showError("Minutes must be greater than zero.");
                    return;
                }
                newR = new CustomReminder(folder, minutes);
        }

        for (int i = 0; i < model.size(); i++){
            Reminder existing = model.get(i);
            if (existing.getFolder().equalsIgnoreCase(newR.getFolder())
                    && existing.getIntervalMillis() == newR.getIntervalMillis()){
                showError("That reminder already exists.");
                return;
            }
        }

        model.addElement(newR);
        folderInput.setText("");
        customMinutesInput.setText("");
        saveRemindersSafe();
    }

    private void deleteSelectedReminderSafe(){
        int idx = list.getSelectedIndex();
        if (idx < 0){
            showError("Select a reminder to delete.");
            return;
        }
        model.remove(idx);
        saveRemindersSafe();
    }

    private void showError(String msg){
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void saveRemindersSafe(){
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(store), StandardCharsets.UTF_8))){
            for (int i = 0; i < model.size(); i++){
                Reminder r = model.get(i);
                String safeFolder = r.getFolder().replace("|", "%7C");
                bw.write(safeFolder+"|"+r.getIntervalMillis()+"|"+r.getLastTime());
                bw.newLine();
            }
        } catch (IOException ex){
            showError("Failed to save reminders:\n" + ex.getMessage());
        }
    }

    private void loadRemindersSafe(){
        if (!store.isFile()) return;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(store), StandardCharsets.UTF_8))){
            String line;
            int lineNo = 0;
            while ((line = br.readLine()) != null){
                lineNo++;
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length < 3){
                    if (parts.length == 2){
                        String folder = parts[0].replace("%7C", "|");
                        long iv;
                        try {iv = Long.parseLong(parts[1]);}
                        catch (NumberFormatException nfe) {continue;}
                        Reminder r = fromInterval(folder, iv, System.currentTimeMillis());
                        if (r != null) model.addElement(r);
                    }
                    continue;
                }
                try{
                    String folder = parts[0].replace("%7C", "|");
                    long interval = Long.parseLong(parts[1]);
                    long last = Long.parseLong(parts[2]);
                    Reminder r = fromInterval(folder, interval, last);
                    if (r != null) model.addElement(r);
                } catch (Exception ex){
                    System.err.println("Skipping bad line "+lineNo+": "+ex);
                }
            }
        } catch (IOException ex){
            showError("Failed to load reminders:\n"+ex.getMessage());
        }
    }

    private Reminder fromInterval(String folder, long interval, long lastTime) {
        if (interval == Reminder.DAY_MILLIS) 
            return new DailyReminder(folder, lastTime);
        if (interval == Reminder.WEEK_MILLIS) 
            return new WeeklyReminder(folder, lastTime);
        long mins = Math.max(1, interval / Reminder.MINUTE_MILLIS);
        return new CustomReminder(folder, (int) Math.min(Integer.MAX_VALUE, mins), lastTime);
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new BackupReminder().setVisible(true));
    }

    private static class DaemonThreadFactory implements ThreadFactory{
        @Override public Thread newThread(Runnable r){
            Thread t = new Thread(r, "ReminderScheduler");
            t.setDaemon(true);
            return t;
        }
    }
}