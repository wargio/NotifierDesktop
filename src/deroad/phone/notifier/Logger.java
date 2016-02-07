package deroad.phone.notifier;

public class Logger {

    public enum Level {
        ERROR(0),
        WARNING(1),
        DEBUG(2),
        TRACE(3);

        private final int level;

        Level(int i) {
            level = i;
        }

        boolean cmp(Level l) {
            return l.level <= this.level;
        }
    };

    private String name = "";
    private static Level level = Level.ERROR;

    public Logger() {
    }

    public Logger(String name) {
        this.name = name;
    }

    public static void setLevel(Level l) {
        level = l;
        System.out.println("Level: " + l.toString());
    }

    public void log(String message, Level l) {
        if (level.cmp(l)) {
            System.out.println(name + ": " + message);
        }
    }

    public String toString() {
        return level.toString();
    }

}
