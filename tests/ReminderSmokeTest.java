package AutomatedReminder;

/**
 * Dependency-free smoke checks for reminder timing and display labels.
 * Run with:
 *   javac -d out *.java tests/ReminderSmokeTest.java
 *   java -Djava.awt.headless=true -cp out AutomatedReminder.ReminderSmokeTest
 */
public final class ReminderSmokeTest {
    private ReminderSmokeTest() {}

    private static void check(boolean actual, String message) {
        if (!actual) {
            throw new AssertionError(message);
        }
    }

    public static void main(String[] args) {
        long start = 1_000_000_000L;

        Reminder daily = new DailyReminder("Documents", start);
        check(daily.getLastTime() == start, "Daily reminder should restore its last time");
        check(daily.getIntervalMillis() == Reminder.DAY_MILLIS, "Daily interval mismatch");
        check("Daily".equals(daily.getIntervalName()), "Daily label mismatch");
        check(!daily.isDue(start), "Daily reminder should not be due immediately");
        check(!daily.isDue(start + Reminder.DAY_MILLIS - 1), "Daily reminder due too early");
        check(daily.isDue(start + Reminder.DAY_MILLIS), "Daily reminder should be due at boundary");

        long resetAt = start + Reminder.DAY_MILLIS;
        daily.reset(resetAt);
        check(daily.getLastTime() == resetAt, "Reset did not update the last time");
        check(!daily.isDue(resetAt), "Reset reminder should no longer be due");

        Reminder weekly = new WeeklyReminder("Photos", start);
        check(weekly.getIntervalMillis() == Reminder.WEEK_MILLIS, "Weekly interval mismatch");
        check("Weekly".equals(weekly.getIntervalName()), "Weekly label mismatch");
        check(weekly.isDue(start + Reminder.WEEK_MILLIS), "Weekly reminder due boundary mismatch");

        Reminder custom = new CustomReminder("Downloads", 90, start);
        check(custom.getIntervalMillis() == 90L * Reminder.MINUTE_MILLIS,
                "Custom interval should be expressed in milliseconds");
        check("90 min".equals(custom.getIntervalName()), "Custom label mismatch");
        check(!custom.isDue(start + 89L * Reminder.MINUTE_MILLIS), "Custom reminder due too early");
        check(custom.isDue(start + 90L * Reminder.MINUTE_MILLIS),
                "Custom reminder should be due at boundary");
        check("Downloads (90 min)".equals(custom.toString()), "Reminder list label mismatch");

        System.out.println("Reminder smoke tests passed");
    }
}
