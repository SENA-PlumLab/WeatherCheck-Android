package com.example.weathercheck;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class CalDateTime {
    final int DSH_BASE_mm = 40; //초단기실황 API제공시간은 매 시각 40분
    final int DYB_BASE_mm = 45; //초단기예보 API제공시간은 매 시각 45분

    Date date_now = new Date(System.currentTimeMillis());

    //날짜 baseDate 구하기 (그대로 사용)
    SimpleDateFormat date_eight = new SimpleDateFormat("yyyyMMdd");
    //시간 baseTime 구하기 (Api 제공시간 nn시40분을 기준으로 설정)
    SimpleDateFormat time_hour = new SimpleDateFormat("HH");
    SimpleDateFormat time_min = new SimpleDateFormat("mm");
    SimpleDateFormat time_four = new SimpleDateFormat("HHmm");


    /***초단기 실황 데이터***/
    public String getDSHBaseTime (){
        // baseTime 구하기 (Api 제공시간 nn시40분: 초단기실황)

        String basetime_hour = time_hour.format(date_now);
        String basetime_min = time_min.format(date_now);

        if (Integer.parseInt(basetime_min)>=DSH_BASE_mm){
            return basetime_hour+"00"; //40분 이상일 경우 지금 시각 그대로 리턴
        }
        else {
           return String.format("%02d",(Integer.parseInt(basetime_hour)+23)%24)+"00"; //40분 미만일 경우 1시간 전 리턴
        }
    }
    public String getDSHBaseDate (){
        //날짜 baseDate 구하기 (00시40분 미만일 경우 어제날짜로 변경 = 어제날짜+baseTime2300이 되어야 함)
        if (Integer.parseInt(time_four.format(date_now)) <DSH_BASE_mm){
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            return date_eight.format(cal.getTime()); //어제 날짜 리턴
        }
        else {
            return date_eight.format(date_now); //오늘 날짜 리턴
        }
    }



    /***초단기 예보 데이터***/
    public String getDYBBaseTime (){
        // baseTime 구하기 (Api 제공시간 nn시45분: 초단기예보)

        String basetime_hour = time_hour.format(date_now);
        String basetime_min = time_min.format(date_now);

        if (Integer.parseInt(basetime_min)>=DYB_BASE_mm){
            return basetime_hour+"30"; //45분 이상일 경우 지금 시각 그대로 리턴
        }
        else {
            return String.format("%02d",(Integer.parseInt(basetime_hour)+23)%24)+"30"; //45분 미만일 경우 1시간 전 리턴
        }
    }
    public String getDYBBaseDate (){
        //날짜 baseDate 구하기 (00시45분 미만일 경우 어제날짜로 변경 = 어제날짜+baseTime2300이 되어야 함)
        if (Integer.parseInt(time_four.format(date_now)) <DYB_BASE_mm){
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            return date_eight.format(cal.getTime()); //어제 날짜 리턴
        }
        else {
            return date_eight.format(date_now); //오늘 날짜 리턴
        }
    }
    public String getDYBfsctTime (Date mDate){
        return time_four.format(mDate)+"00";


    }


}
