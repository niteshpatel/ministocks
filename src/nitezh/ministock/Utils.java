package nitezh.ministock;

public class Utils {

    public static final String VERSION = "45";
    public static final String BUILD = "45";

    public static int getField(String name) {
        try {
            return R.id.class.getField(name).getInt(R.class);

        } catch (Exception e) {
            return 0;
        }
    }

}
