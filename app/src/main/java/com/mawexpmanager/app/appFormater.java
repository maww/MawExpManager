package com.mawexpmanager.app;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * Created by maww on 2014/6/25.
 */
public class appFormater {
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
    public static SimpleDateFormat sdf_date=new SimpleDateFormat("yyyy/MM/dd");
    public static SimpleDateFormat sdf_day=new SimpleDateFormat("MM/dd");
    public static SimpleDateFormat sdf_week=new SimpleDateFormat("E");
    public static DecimalFormat df = new DecimalFormat("#,###,###");
}
