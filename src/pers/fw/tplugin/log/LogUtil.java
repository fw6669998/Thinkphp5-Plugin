package pers.fw.tplugin.log;

public class LogUtil {
    public static boolean isLogFile(String filename) {
        String ext = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
        if ("log".equals(ext))
            return true;
        else
            return false;
    }

    public static String getTimeStr(String str) {
        //处理时间行:
        if (str.startsWith("[ 2")) {
            int start = str.indexOf("[");
            int end = str.indexOf("]");
            String timeStr = str.substring(start + 2, end);
            if (timeStr != null) {
                timeStr = timeStr.trim();
                //e: 2018-07-18T12:47:03
                timeStr = timeStr.substring(0, 19);
                timeStr = timeStr.replace("T", " ");
                return timeStr;
            }
        }
        return "";
    }

    //  获取类型1;
    public static String getType1(String str) {
        String type = "";
        type = str.substring(1, str.indexOf("]"));
        return type;
    }

    //  获取类型2
    public static String getType2(String str) {
        String type = "";
        int i = str.indexOf("] [");
        int i1 = str.substring(i + 3).indexOf("]");
        type = str.substring(i + 3, i1);
        return type;
    }
}
