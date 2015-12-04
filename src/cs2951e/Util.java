package cs2951e;

public class Util {
    public static String generateError(String msg) {
        return "{'error' : true, 'msg' : " + msg + "}";
    }

    public static String generateSuccess() {
        return "{'error' : false}";
    }
}
