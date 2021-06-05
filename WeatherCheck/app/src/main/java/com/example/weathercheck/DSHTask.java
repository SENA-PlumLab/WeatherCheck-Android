package com.example.weathercheck;

import android.os.AsyncTask;
import android.widget.TextView;

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

public class DSHTask extends AsyncTask<String, Void, String> {

    String nx = "60"; //용산구 위도
    String ny = "125"; //용산구 경도
    CalDateTime cdt = new CalDateTime();
    String type = "xml"; //타입 xml, json 등등

    @Override
    protected String doInBackground(String... urls) {
        StringBuilder urlBuilder = new StringBuilder(MainActivity.DSH_URL); /*URL*/

        try {
            urlBuilder.append("?" + URLEncoder.encode("ServiceKey","UTF-8") + "=" + MainActivity.SERVICE_KEY); /*Service Key*/
            urlBuilder.append("&"+URLEncoder.encode("nx","UTF-8")+"="+URLEncoder.encode(nx, "UTF-8"));
            urlBuilder.append("&"+URLEncoder.encode("ny","UTF-8")+"="+URLEncoder.encode(ny, "UTF-8"));
            urlBuilder.append("&"+URLEncoder.encode("base_date","UTF-8")+"="+URLEncoder.encode(cdt.getDSHBaseDate(), "UTF-8"));
            urlBuilder.append("&"+URLEncoder.encode("base_time","UTF-8")+"="+URLEncoder.encode(cdt.getDSHBaseTime(), "UTF-8"));
            urlBuilder.append("&"+URLEncoder.encode("dataType","UTF-8")+"="+URLEncoder.encode(type, "UTF-8"));
            urlBuilder.append("&"+URLEncoder.encode("numOfRows","UTF-8")+"="+URLEncoder.encode("16", "UTF-8"));

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
        //super.onPostExecute(result);
        //textView.setText(result);


        boolean bSet_category = false; //자료구분
        boolean bSet_obsrValue = false; //실황 값
        boolean bSet_PTY = false; //강수형태인지 확인 후, 적절한 String으로 출력해야 함
        boolean bSet_print = false; //자료구분 확인 후 출력할 지 결정


        String category = "";
        String value = "";
        String tag_name = "";

        //화면 초기화
        MainActivity.textViewDSH.setText("");
        MainActivity.textViewDSH.append("<----서울특별시 용산구---->\n");
        MainActivity.textViewDSH.append("<----현재 날씨---->\n");
        MainActivity.textViewDSH.append("발표:"+cdt.getDSHBaseDate()+" "+cdt.getDSHBaseTime()+"\n");

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
                    //태그 이름이 <obseValue>인 경우
                    else if (bSet_obsrValue == false && tag_name.equals("obsrValue")) {
                        bSet_obsrValue = true;
                    }
                }

                //태그 사이의 문자 확인
                else if (eventType == XmlPullParser.TEXT) {
                    //태그 이름이 <category>(자료구분문자) 였으면
                    if (bSet_category) {
                        category = xpp.getText();
                        // 자료구분문자 값이 TMN 혹은 TMX 일 때
                        if (category.equals("T1H")){
                            MainActivity.textViewDSH.append("현재 기온:");
                            bSet_print = true; //이후의 값을들 출력하기로 한다.
                        }
                        else if (category.equals("RN1")){
                            MainActivity.textViewDSH.append("강수량:");
                            bSet_print = true; //이후의 값을들 출력하기로 한다.
                        }
                        else if (category.equals("REH")){
                            MainActivity.textViewDSH.append("습도:");
                            bSet_print = true; //이후의 값을들 출력하기로 한다.
                        }
                        else if (category.equals("PTY")){
                            bSet_PTY = true;
                            MainActivity.textViewDSH.append("강수형태:");
                            bSet_print = true; //이후의 값을들 출력하기로 한다.
                        }
                        bSet_category = false;
                    }
                    //출력하기로 한 자료의 값을 출력한다.
                    else if (bSet_print) {
                        if (bSet_obsrValue) {
                            if (bSet_PTY){ //PTY의 obserValue일 경우
                                value = xpp.getText();
                                switch(Integer.parseInt(value)){
                                    case 0: MainActivity.textViewDSH.append("없음\n"); break;
                                    case 1: MainActivity.textViewDSH.append("비\n"); break;
                                    case 2: MainActivity.textViewDSH.append("비와 눈\n"); break;
                                    case 3: MainActivity.textViewDSH.append("눈\n"); break;
                                    case 4: MainActivity.textViewDSH.append("소나기\n"); break;
                                    case 5: MainActivity.textViewDSH.append("빗방울\n"); break;
                                    case 6: MainActivity.textViewDSH.append("진눈개비\n");break;
                                    case 7: MainActivity.textViewDSH.append("눈날림\n");break;
                                }
                                bSet_PTY=false;
                            } else{
                                value = xpp.getText();
                                MainActivity.textViewDSH.append(value + "\n");
                            }
                            bSet_obsrValue = false;
                            bSet_print = false;
                        }
                    }
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

