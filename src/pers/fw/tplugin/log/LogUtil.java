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
    public static String[] getContent(String str) {
        String[] arr = new String[3];
        String type = "";
        int l = str.indexOf("[");
        int r = str.indexOf("]");
        if (l != -1 && r != -1 && r > 1)
            type = str.substring(l + 1, r);

        String type2 = "";
        int l1 = str.indexOf("] [");
        int r1 = str.substring(l1 + 3).indexOf("]") + l1 + 3;
        if (l1 != -1 && r1 != -1 && r1 > l1)
            type2 = str.substring(l1 + 3, r1);

        String content = "";
        if (r1 != -1 && l1 != -1)
            content = str.substring(r1 + 1);
        else if (r != -1 && r < 5)
            content = str.substring(r + 1);
        else
            content = str;

        arr[0] = type.trim();
        arr[1] = type2.trim();
        arr[2] = content;
        return arr;
    }

    //  获取类型2
    public static String getType2(String str) {
        String type2 = "";
        int l1 = str.indexOf("] [");
        if (l1 != -1) return type2;
        int r1 = str.substring(l1 + 3).indexOf("]");
        if (r1 == -1) return type2;
        type2 = str.substring(l1 + 3, r1);
        String content = "";
        content = str.substring(r1 + 1);
        return type2;
    }
}
