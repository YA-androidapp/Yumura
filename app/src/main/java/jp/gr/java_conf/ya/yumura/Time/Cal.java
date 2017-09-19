package jp.gr.java_conf.ya.yumura.Time; // Copyright (c) 2013-2017 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import java.util.Calendar;
import java.util.StringTokenizer;

public class Cal {
    public static Calendar toCalendar(String strDate) {
        strDate = format(strDate);
        Calendar cal = Calendar.getInstance();
        cal.setLenient(false);

        int yyyy = Integer.parseInt(strDate.substring(0, 4));
        int MM = Integer.parseInt(strDate.substring(5, 7));
        int dd = Integer.parseInt(strDate.substring(8, 10));
        int HH, mm, ss, SSS;
        cal.clear();
        cal.set(yyyy, MM - 1, dd);
        int len = strDate.length();
        switch (len) {
            case 10:
                break;
            case 16: // yyyy/MM/dd HH:mm
                HH = Integer.parseInt(strDate.substring(11, 13));
                mm = Integer.parseInt(strDate.substring(14, 16));
                cal.set(Calendar.HOUR_OF_DAY, HH);
                cal.set(Calendar.MINUTE, mm);
                break;
            case 19: //yyyy/MM/dd HH:mm:ss
                HH = Integer.parseInt(strDate.substring(11, 13));
                mm = Integer.parseInt(strDate.substring(14, 16));
                ss = Integer.parseInt(strDate.substring(17, 19));
                cal.set(Calendar.HOUR_OF_DAY, HH);
                cal.set(Calendar.MINUTE, mm);
                cal.set(Calendar.SECOND, ss);
                break;
            case 23: //yyyy/MM/dd HH:mm:ss.SSS
                HH = Integer.parseInt(strDate.substring(11, 13));
                mm = Integer.parseInt(strDate.substring(14, 16));
                ss = Integer.parseInt(strDate.substring(17, 19));
                SSS = Integer.parseInt(strDate.substring(20, 23));
                cal.set(Calendar.HOUR_OF_DAY, HH);
                cal.set(Calendar.MINUTE, mm);
                cal.set(Calendar.SECOND, ss);
                cal.set(Calendar.MILLISECOND, SSS);
                break;
            default:
                return null;
        }
        return cal;
    }

    private static String format(String str) {
        if (str == null || str.trim().length() < 8)
            return "";

        str = str.trim();
        String yyyy, MM, dd, HH, mm, ss, SSS;

        if (str.indexOf("/") == -1 && str.indexOf("-") == -1) {
            if (str.length() == 8) {
                yyyy = str.substring(0, 4);
                MM = str.substring(4, 6);
                dd = str.substring(6, 8);
                return yyyy + "/" + MM + "/" + dd;
            }
            yyyy = str.substring(0, 4);
            MM = str.substring(4, 6);
            dd = str.substring(6, 8);
            HH = str.substring(9, 11);
            mm = str.substring(12, 14);
            ss = str.substring(15, 17);
            return yyyy + "/" + MM + "/" + dd + " " + HH + ":" + mm + ":" + ss;
        }
        StringTokenizer token = new StringTokenizer(str, "_/-:. ");
        StringBuffer result = new StringBuffer();
        for (int i = 0; token.hasMoreTokens(); i++) {
            String temp = token.nextToken();
            switch (i) {
                case 0:// 年
                    yyyy = fillString(temp, "L", "20", 4);
                    result.append(yyyy);
                    break;
                case 1:// 月
                    MM = fillString(temp, "L", "0", 2);
                    result.append("/" + MM);
                    break;
                case 2:// 日
                    dd = fillString(temp, "L", "0", 2);
                    result.append("/" + dd);
                    break;
                case 3:// 時
                    HH = fillString(temp, "L", "0", 2);
                    result.append(" " + HH);
                    break;
                case 4:// 分
                    mm = fillString(temp, "L", "0", 2);
                    result.append(":" + mm);
                    break;
                case 5:// 秒
                    ss = fillString(temp, "L", "0", 2);
                    result.append(":" + ss);
                    break;
                case 6:// ミリ秒
                    SSS = fillString(temp, "R", "0", 3);
                    result.append("." + SSS);
                    break;
            }
        }
        return result.toString();
    }

    private static String fillString(String str, String position, String addStr, int len) {
        if (str.length() > len)
            return "";

        return fillString(str, position, len, addStr);
    }

    private static String fillString(String str, String position, int len, String addStr) {
        if (addStr == null || addStr.length() == 0)
            return "";

        if (str == null)
            str = "";

        StringBuffer buffer = new StringBuffer(str);
        while (len > buffer.length()) {
            if (position.equalsIgnoreCase("l")) {
                int sum = buffer.length() + addStr.length();
                if (sum > len) {
                    addStr = addStr.substring
                            (0, addStr.length() - (sum - len));
                    buffer.insert(0, addStr);
                } else {
                    buffer.insert(0, addStr);
                }
            } else {
                buffer.append(addStr);
            }
        }

        if (buffer.length() == len)
            return buffer.toString();

        return buffer.toString().substring(0, len);
    }
}
