package jp.gr.java_conf.ya.yumura.Time; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class Time {
    public static int differenceDays(String strDate1,String strDate2)
            throws ParseException {
        Date date1 = DateFormat.getDateInstance().parse(strDate1);
        Date date2 = DateFormat.getDateInstance().parse(strDate2);
        return differenceDays(date1,date2);
    }
    public static int differenceDays(Date date1,Date date2) {
        long datetime1 = date1.getTime();
        long datetime2 = date2.getTime();
        long oneDateTime = 1000 * 60 * 60 * 24;
        long diffDays = (datetime1 - datetime2) / oneDateTime;
        return (int)Math.floor(diffDays);
    }

    public static int differenceMinutes(Date date2) {
        return differenceMinutes(new Date(),date2);
    }

    public static int differenceMinutes(Date date1,Date date2) {
        long datetime1 = date1.getTime();
        long datetime2 = date2.getTime();
        long oneMinuteTime = 1000 * 60;
        long diffMinutes = (datetime1 - datetime2) / oneMinuteTime;
        return (int)Math.floor(diffMinutes);
    }

}
