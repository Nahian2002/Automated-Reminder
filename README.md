# Automated Backup Reminder

A small Java Swing desktop app that **reminds you when to back up selected folders**.

> **Important:** This application does **not** copy, upload, or back up files automatically. It only displays reminder dialogs. You are responsible for making and verifying your backups.

## Requirements

- A Java Development Kit (JDK) with `javac` and `java` available on your PATH (Java 8 or newer).
- Windows, macOS, or Linux with a graphical desktop session. The application uses Swing and will not work in a headless terminal.

No external libraries or build tools are required.

## Build and run

Clone the repository, then open a terminal in its root folder:

```sh
git clone https://github.com/Nahian2002/Automated-Reminder.git
cd Automated-Reminder
javac -d out *.java
java -cp out AutomatedReminder.BackupReminder
```

The `-d out` option is required because the source files declare the `AutomatedReminder` Java package even though they are stored in the repository root.

**Windows PowerShell:** Run the same `git clone` and `cd` commands above, followed by:

```powershell
javac -d out (Get-ChildItem -Name *.java)
java -cp out AutomatedReminder.BackupReminder
```

If `javac` is not found, install a JDK and reopen your terminal. If the app cannot open a window, run it in a graphical desktop environment.

## Using the app

1. Select an existing folder using **Browse…**, or enter its full path.
2. Choose **Daily**, **Weekly**, or **Custom**. For Custom, enter a positive whole number of minutes.
3. Click **Add**. The reminder appears in the list.
4. When a reminder becomes due, the app displays a popup naming the folder.
5. To remove a reminder, select it in the list and click **Delete**.

Identical folder-and-interval entries are rejected. The app checks reminders approximately once a minute; reminders are not guaranteed to appear at an exact second. Keep the program running to receive notifications.

## Saved reminders and limitations

- Reminder settings are saved in `reminders.txt` in the **current working directory** (the directory from which you launch `java`), not necessarily next to the source files.
- The file is loaded when the app starts and is updated when reminders are added or removed and when the window closes normally.
- To preserve your reminder list, launch the app from the same working directory each time, and retain a copy of `reminders.txt` before moving installations.
- The app does not run as a background service after you close its window. It does not schedule operating-system notifications, verify folder contents, or create actual backups.

## Project structure

- `BackupReminder.java`: Swing user interface, periodic checks, and persistence
- `Reminder.java`: common reminder state and timing logic
- `DailyReminder.java`, `WeeklyReminder.java`, `CustomReminder.java`: interval-specific reminder dialogs
