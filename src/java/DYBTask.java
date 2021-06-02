package com.example.weathercheck;

import android.content.Context;
import android.os.AsyncTask;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import static com.example.weathercheck.MainActivity.mMinute;
import static com.example.weathercheck.MainActivity.mHour;
import static com.example.weathercheck.MainActivity.mDay;
import static com.example.weathercheck.MainActivity.mMonth;
import static com.example.weathercheck.MainActivity.mYear;
import static com.example.weathercheck.MainActivity.textViewDYB;

public class DYBTask extends AsyncTask<String, Void, String> {

    Context mContext;
    String nx;//위도
    String ny; //경도
    CalDateTime cdt = new CalDateTime();
    String type = "xml"; //타입 xml, json 등등

    public DYBTask(String xx, String yy, Context context){
       nx= xx;
       ny= yy;
       mContext = context;
    }

    @Override
    protected String doInBackground(String... urls) {
        StringBuilder urlBuilder = new StringBuilder(MainActivity.DYB_URL); /*URL*/
        //HttpURLConnection conn = null;
        //BufferedReader rd = null;
        //StringBuilder sb = null;
        try {
            urlBuilder.append("?" + URLEncoder.encode("ServiceKey","UTF-8") + "=" + MainActivity.SERVICE_KEY); /*Service Key*/
            urlBuilder.append("&"+URLEncoder.encode("nx", "UTF-8")+"="+URLEncoder.encode(nx, "UTF-8"));
            urlBuilder.append("&"+URLEncoder.encode("ny","UTF-8")+"="+URLEncoder.encode(ny, "UTF-8"));
            urlBuilder.append("&"+URLEncoder.encode("base_date","UTF-8")+"="+URLEncoder.encode(cdt.getDYBBaseDate(), "UTF-8"));
            urlBuilder.append("&"+URLEncoder.encode("base_time","UTF-8")+"="+URLEncoder.encode(cdt.getDYBBaseTime(), "UTF-8"));
            urlBuilder.append("&"+URLEncoder.encode("dataType","UTF-8")+"="+URLEncoder.encode(type, "UTF-8"));
            urlBuilder.append("&"+URLEncoder.encode("numOfRows","UTF-8")+"="+URLEncoder.encode("200", "UTF-8"));

            String txt = (String) downloadUrl(urlBuilder.toString());
            return txt;
        }
        catch (IOException ex) {
            return "다운로드 실패";
        }
    }


    //문서 다운로드 후 자동 호출:XML 문서 파싱
    @Override
    protected void onPostExecute(String result) {
        boolean bSet_category = false; //자료구분
        boolean bSet_fcstDate = false; //예보일자
        boolean bSet_fcstTime = false; //예보시각
        boolean bSet_fcstValue = false; //예보 값
        boolean bSet_PTY = false; //PTY(겅수형태)는 코드에 따라 맞는 String 출력
        boolean bSet_SKY = false; //SKY(하늘상태)는 코드에 따라 맞는 String 출력
        boolean IsCategorySaved = false;
        boolean IsFcstDateSaved = false;
        boolean IsFcstTimeSaved = false;
        boolean HasPrinted_T1H = false;
        boolean HasPrinted_RN1 = false;
        boolean HasPrinted_SKY = false;
        boolean HasPrinted_REH = false;
        boolean HasPrinted_PTY = false;

        String category = "";
        String value = "";
        String tag_name = "";

        //설정했던 날짜를 FcstDate로 설정한다
        String fcstDate = String.format("%4d%02d%02d",mYear,mMonth+1,mDay);
        //설정했던 날짜와 시간을 인자로 넘겨주고, 가장 근접한 FcstTime을 구한다.
        String fcstTime = String.format("%02d",mHour)+"00";

        //화면 초기화
        textViewDYB.setText("");
        textViewDYB.append("<----"+PreferenceManager.getString(mContext,"areaTop")
                            +" "+PreferenceManager.getString(mContext,"areaMdl")+"---->\n");
        textViewDYB.append("<----"+fcstDate+" "+fcstTime+" 날씨---->\n");
        textViewDYB.append("(발표:"+cdt.getDYBBaseDate()+" "+cdt.getDYBBaseTime()+")\n");

        try {
            //XML Pull Parser 객체 생성
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            //파싱할 문서 설정
            xpp.setInput(new StringReader((result)));

            //현재 이벤트 유형 반환
            //(START_DOCUMENT, START_TAG, TEXT, END_TAG, END_DOCUMENT)
            int eventType = xpp.getEventType();
            //이벤트 유형이 문서 마지막이 될 떄까찌 반복
            while (eventType != XmlPullParser.END_DOCUMENT) {
                //문서 시작인 경우
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    ;
                }

                //START_TAG이면 태그 이름 확인
                else if (eventType == XmlPullParser.START_TAG) {
                    tag_name = xpp.getName();
                    //태그 이름이 <category>인 경우
                    if (bSet_category == false && tag_name.equals("category")) {
                        bSet_category = true;
                    }
                    //태그 이름이 <fcstDate>인 경우
                    else if (IsCategorySaved && tag_name.equals("fcstDate")) {
                        bSet_fcstDate = true;
                    }
                    //태그 이름이 <fcstTime>인 경우
                    else if (IsFcstDateSaved && tag_name.equals("fcstTime")) {
                        bSet_fcstTime = true;
                    }
                    //태그 이름이 <fcstValue>인 경우
                    else if (IsFcstTimeSaved && tag_name.equals("fcstValue")) {
                        bSet_fcstValue = true;
                    }
                    else {
                        tag_name = "";
                    }
                }

                //태그 사이의 문자 확인
                else if (eventType == XmlPullParser.TEXT) {

                    //태그 이름이 <category>(자료구분문자) 였으면
                    if (bSet_category && (!IsCategorySaved)) {
                        category = xpp.getText();
                        // 자료구분문자 값
                        if (category.equals("T1H") && !HasPrinted_T1H) {
                            textViewDYB.append("예상 기온:");
                            HasPrinted_T1H=true;
                            IsCategorySaved = true; //이후의 값을들 출력하기로 한다.
                        } else if (category.equals("RN1") && !HasPrinted_RN1) {
                            textViewDYB.append("강수량:");
                            HasPrinted_RN1=true;
                            IsCategorySaved = true; //이후의 값을들 출력하기로 한다.
                        } else if (category.equals("SKY") && !HasPrinted_SKY) {
                            textViewDYB.append("하늘상태:");
                            HasPrinted_SKY=true;
                            bSet_SKY = true;
                            IsCategorySaved = true; //이후의 값을들 출력하기로 한다.
                        } else if (category.equals("REH") && !HasPrinted_REH) {
                            textViewDYB.append("습도:");
                            HasPrinted_REH = true;
                            IsCategorySaved = true;
                        } else if (category.equals("PTY") && !HasPrinted_PTY) {
                            textViewDYB.append("강수형태:");
                            HasPrinted_PTY=true;
                            bSet_PTY = true;
                            IsCategorySaved = true;
                        }
                        bSet_category = false;
                    }
                    //출력하기로 한 자료의 값을 출력한다.

                    //태그 이름이 <fcstDate> 일 때
                    if (bSet_fcstDate && (!IsFcstDateSaved)) {
                            /*
                            if (bSet_fcstDate) {
                                value = xpp.getText();
                                textViewDYB.append("예보일자(시각): "+value);
                                bSet_fcstDate = false;
                            } else if (bSet_fcstTime) {
                                value = xpp.getText();
                                textViewDYB.append(" ("+value + ")\n");
                                bSet_fcstTime = false;
                            }
                            else */
                        value = xpp.getText();
                        if (value.equals(fcstDate)) {
                            IsFcstDateSaved = true;
                        }
                        else{
                            value="";
                        }
                        bSet_fcstDate = false;
                    } else { bSet_fcstDate = false;}

                    //태그 이름이 <fcstTime> 일 때
                    if (bSet_fcstTime && (!IsFcstTimeSaved)) {
                        value = xpp.getText();
                        if (value.equals(fcstTime)) {
                            IsFcstTimeSaved = true;
                        }
                        else{
                            value="";
                        }
                        bSet_fcstTime = false;
                    }else { bSet_fcstTime = false;}

                    //태그 이름이 <fcstValue> 일 때
                    if (bSet_fcstValue && IsFcstTimeSaved) {
                        value = xpp.getText();
                        if (bSet_PTY) {
                            switch(Integer.parseInt(value)){
                                case 0: textViewDYB.append("없음\n"); break;
                                case 1: textViewDYB.append("비\n"); break;
                                case 2: textViewDYB.append("비와 눈\n"); break;
                                case 3: textViewDYB.append("눈\n"); break;
                                case 4: textViewDYB.append("소나기\n"); break;
                                case 5: textViewDYB.append("빗방울\n"); break;
                                case 6: textViewDYB.append("진눈개비\n");break;
                                case 7: textViewDYB.append("눈날림\n");break;
                                default:textViewDYB.append("값 없음\n");break;
                            }
                            bSet_PTY = false;
                        }
                        else if(bSet_SKY){
                            switch(Integer.parseInt(value)){
                                case 1: textViewDYB.append("맑음\n"); break;
                                //구름조금(2)는 삭제됨 (2019.06.04)
                                //case 2: textViewDYB.append("구름 조금\n"); break;
                                case 3: textViewDYB.append("구름 많음\n"); break;
                                case 4: textViewDYB.append("흐림\n"); break;
                                default: textViewDYB.append("값 없음\n");break;
                            }
                            bSet_SKY = false;
                        }
                        else{
                            textViewDYB.append(value +"\n");
                        }
                        IsCategorySaved = false;
                        IsFcstDateSaved = false;
                        IsFcstTimeSaved = false;
                        bSet_fcstValue = false;
                    }else { bSet_fcstValue = false;}
                }

                //마침 태그인 경우
                else if (eventType == XmlPullParser.END_TAG) {
                }

                //다음 이벤트 유형 할당
                eventType = xpp.next();
            }


        } catch (Exception e) {            }
    }

    private String downloadUrl (String api) throws IOException {
        HttpURLConnection conn = null;

        try {
            //문서를 읽어 텍스트 단위로 버퍼에 저장
            URL url = new URL(api);
            conn = (HttpURLConnection)url.openConnection();
            BufferedInputStream buf = new BufferedInputStream(conn.getInputStream());
            BufferedReader bufreader = new BufferedReader(new InputStreamReader(buf, "UTF-8"));

            //줄 단위로 읽어 문자로 저장
            String line = null;
            String page = "";
            while ((line = bufreader.readLine()) != null) {
                page += line;
            }

            buf.close();
            bufreader.close();
            return page;

        } finally {
            conn.disconnect();
        }



    }

}