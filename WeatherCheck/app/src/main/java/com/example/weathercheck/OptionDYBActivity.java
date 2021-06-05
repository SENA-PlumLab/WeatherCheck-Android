package com.example.weathercheck;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OptionDYBActivity extends AppCompatActivity {
    JSONObject city = new JSONObject();
    JSONObject gu = new JSONObject();
    JSONObject nx = new JSONObject();
    JSONObject ny = new JSONObject();

    ArrayList<String> arrayListTop, arrayListMdl;
    ArrayAdapter<String> arrayAdapterTop, arrayAdapterMdl;
    TextView textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
        Context context = this;

        //https 리디렉션 연결에서 SSLHandshakeException 에러가 있음 20210113
        //final String url = "http://www.kma.go.kr/DFSROOT/POINT/DATA/top.json.txt";
        final Spinner spTop = (Spinner)findViewById(R.id.spinnerForTop);
        final Spinner spMdl = (Spinner)findViewById(R.id.spinnerForMdl);
        Button buttonOK = (Button)findViewById(R.id.buttonOptionOK);

        textview = (TextView)findViewById(R.id.textView01);

        //assets폴더의 파일을 가져오기 위해 창고관리자(assetManager) 불러오기
        AssetManager assetManager = getAssets();

        //도시 스피너 설정
        try{
            //  assets/XYTop.json 파일 읽기 위한 InputStream
            InputStream is = assetManager.open("jsons/XYTop.json");
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader bufReader = new BufferedReader(isr);

            StringBuffer buffer = new StringBuffer();
            String inputString = bufReader.readLine();

            while(inputString != null){
                buffer.append(inputString+"\n");
                inputString = bufReader.readLine();
            }

            String jsonData = buffer.toString();
            JSONArray jsonArray = new JSONArray(jsonData);

            arrayListTop = new ArrayList<>();
            arrayListTop.add("선택");

            for (int i = 0; i<jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                arrayListTop.add(jsonObject.getString("value"));

                city.put(jsonObject.getString("value"), jsonObject.getString("code"));

            }
            arrayAdapterTop = new ArrayAdapter<>(getApplicationContext(),
                    android.R.layout.simple_spinner_dropdown_item, arrayListTop);
            spTop.setAdapter(arrayAdapterTop);


        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }


        //도시 스피너 클릭시 이벤트
        final int[] now1 = new int[1];
        //첫번째 도시 스피너 클릭 이벤트
        spTop.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String s = parent.getItemAtPosition(position).toString();
                PreferenceManager.setString(context,"areaTop",s);
                now1[0]=position;
                //gu에 지역code 저장하기
                try {
                    InputStream is = assetManager.open("jsons/XYMdl"+city.getString(s)+".json");
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader bufReader = new BufferedReader(isr);

                    StringBuffer buffer = new StringBuffer();
                    String inputString = bufReader.readLine();

                    while (inputString != null) {
                        buffer.append(inputString + "\n");
                        inputString = bufReader.readLine();
                    }

                    String jsonData = buffer.toString();
                    JSONArray jsonArray = new JSONArray(jsonData);

                    //Mdl의 값을 저장할 JSONArray
                    arrayListMdl = new ArrayList<>();
                    arrayListMdl.add("선택");

                    //Mdl 정보를 두번째 구 스피너에 넣기
                    for (int i = 0; i<jsonArray.length(); i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        arrayListMdl.add(jsonObject.getString("value"));

                        gu.put(jsonObject.getString("value"), jsonObject.getString("code"));
                    }
                    arrayAdapterMdl = new ArrayAdapter<>(getApplicationContext(),
                            android.R.layout.simple_spinner_dropdown_item, arrayListMdl);
                    spMdl.setAdapter(arrayAdapterMdl);


                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }



            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //구 스피너 클릭시 이벤트 (두번째 스피너)
        final int[] now2 = new int[1];
        //두번째 구 스피너 클릭 이벤트
        spMdl.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //JSONArray Gun_jsonArray;


            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String s = parent.getItemAtPosition(position).toString();
                PreferenceManager.setString(context,"areaMdl",s);

                //gu에 저장된 지역code로 x, y 구하기
                try {
                    InputStream is = assetManager.open("jsons/MdlCodesHere.json");
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader bufReader = new BufferedReader(isr);

                    StringBuffer buffer = new StringBuffer();
                    String inputString = bufReader.readLine();

                    while (inputString != null) {
                        buffer.append(inputString + "\n");
                        inputString = bufReader.readLine();
                    }

                    String jsonData = buffer.toString();
                    JSONArray jsonArray = new JSONArray(jsonData);

                    //nx, ny에 좌표값 넣기
                    for (int i = 0; i<jsonArray.length(); i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        if(s.equals(jsonObject.getString("value"))){
                            nx.put("nx", jsonObject.getString("x"));
                            ny.put("ny", jsonObject.getString("y"));
                            PreferenceManager.setString(context,"nx",nx.getString("nx"));
                            PreferenceManager.setString(context,"ny",ny.getString("ny"));
                            textview.setText(PreferenceManager.getString(context, "areaTop")+", "
                                    +PreferenceManager.getString(context, "areaMdl")+"\n");
                            textview.append("그리드 좌표: "+PreferenceManager.getString(context, "nx")+", "
                                    +PreferenceManager.getString(context, "ny")+"로 설정 되었습니다.\n");
                            break;
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }



    public void setAreaTop(String s){
        PreferenceManager.setString(this,"areaTop", s);
    }


}





