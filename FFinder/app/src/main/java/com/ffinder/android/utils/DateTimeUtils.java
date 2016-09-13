package com.ffinder.android.utils;

import android.content.Context;
import android.content.Intent;
import com.ffinder.android.R;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by SiongLeng on 2/9/2016.
 */
public class DateTimeUtils {

    public static final Map<String, Long> times = new LinkedHashMap<>();

    static {
        times.put("year", TimeUnit.DAYS.toMillis(365));
        times.put("month", TimeUnit.DAYS.toMillis(30));
        times.put("week", TimeUnit.DAYS.toMillis(7));
        times.put("day", TimeUnit.DAYS.toMillis(1));
        times.put("hour", TimeUnit.HOURS.toMillis(1));
        times.put("minute", TimeUnit.MINUTES.toMillis(1));
        times.put("second", TimeUnit.SECONDS.toMillis(1));
    }

    public static String toRelative(long duration, int maxLevel) {
        StringBuilder res = new StringBuilder();
        int level = 0;
        for (Map.Entry<String, Long> time : times.entrySet()){
            long timeDelta = duration / time.getValue();
            if (timeDelta > 0){
                res.append(timeDelta)
                        .append(" ")
                        .append(time.getKey())
                        .append(timeDelta > 1 ? "s" : "")
                        .append(", ");
                duration -= time.getValue() * timeDelta;
                level++;
            }
            if (level == maxLevel){
                break;
            }
        }
        if ("".equals(res.toString())) {
            return "one moment ago";
        } else {
            res.setLength(res.length() - 2);
            res.append(" ago");
            return res.toString();
        }
    }

    public static String toRelative(long duration) {
        return toRelative(duration, times.size());
    }

    public static String toRelative(Date start, Date end){
        return toRelative(end.getTime() - start.getTime());
    }

    public static String toRelative(Date start, Date end, int level){
        return toRelative(end.getTime() - start.getTime(), level);
    }

    public static String convertUnixMiliSecsToDateTimeString(Context context, long unixMiliSecs){

        if(unixMiliSecs == 0){
            return context.getString(R.string.never);
        }


        Date date = new Date();
        date.setTime(unixMiliSecs);

        Date currentDate = new Date();
        long difference = currentDate.getTime() - date.getTime();

        String result = "";

        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        String formattedPrevious = fmt.format(date);
        String formattedCurrent = fmt.format(currentDate);
        if(formattedPrevious.equals(formattedCurrent)){
            DateFormat writeFormat = new SimpleDateFormat("hh:mm aa");
            result = writeFormat.format(date);
        }
        else if(Integer.valueOf(formattedCurrent) - Integer.valueOf(formattedPrevious) == 1){
            result = context.getString(R.string.yesterday);
        }
        else{
            Format dateFormat = android.text.format.DateFormat.getDateFormat(context);
            String pattern = ((SimpleDateFormat) dateFormat).toLocalizedPattern();
            DateFormat writeFormat = new SimpleDateFormat(pattern);
            result = writeFormat.format(date);
        }

        return result;
    }

    public static long getDifferenceInSecs(long beforeMili, long afterMili){
        return (afterMili - beforeMili) / 1000;
    }



}
