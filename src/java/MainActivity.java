package com.example.weathercheck;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.TextView;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class MainActivity extends AppCompatActivity {
    static final String DSH_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService/getUltraSrtNcst"; //초단기실황조회
    static final String DYB_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService/getUltraSrtFcst"; //초단기예보조회
    //인증키
    static final String SERVICE_KEY = "mgYRmJHGp%2BRVUyaGAcvyx0kg3x47%2BrlGbkEBhuaqczFYixAzBIVrr0kDn%2BF3cNUs0H%2FLCNHL8vR2pjJnLMD8Mw%3D%3D";
    CalDateTime cdt = new CalDateTime();
    String type = "xml"; //타입 xml, json 등등

    //초단기실황
    DSHTask DSHWeatherTask;
    //초단기예보
    DYBTask DYBWeatherTask;

    static TextView textViewDSH;
    static TextView textViewDYB;
    TextView mTxtDate;
    TextView mTxtTime;


    static int mYear, mMonth, mDay, mHour, mMinute;


    //TimePicker dialog
    public void TimeOnClickHandler(View v){
        switch (v.getId()){
            case R.id.textView_Date:
                new DatePickerDialog(MainActivity.this, mDateSetListener, mYear, mMonth, mDay).show();
                break;
            case R.id.textView_Time:
                new TimePickerDialog(MainActivity.this, mTimeSetListener, mHour, mMinute, false).show();
                break;
        }
    }
    DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;

            UpdateDateTimeTextView();
        }
    };
    TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHour = hourOfDay;
            mMinute = minute;
            UpdateDateTimeTextView();
        }
    };
    void UpdateDateTimeTextView(){
        mTxtDate.setText(String.format("%d년 %d월 %d일",mYear,mMonth+1,mDay));
        mTxtTime.setText(String.format("%d시 %d분",mHour,mMinute));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //https 리디렉트 연결에서 SSLHandshakeException이 발생하므로
        //Security.insertProviderAt(Conscrypt.newProvider(),1);

        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.HOUR_OF_DAY, +3); //3시간 후로 설정
        mYear = cal.get(Calendar.YEAR);
        mMonth = cal.get(Calendar.MONTH);
        mDay = cal.get(Calendar.DAY_OF_MONTH);
        mHour = cal.get(Calendar.HOUR_OF_DAY);
        mMinute = cal.get(Calendar.MINUTE);
/*
        PreferenceManager.setString(this,"areaTop","서울특별시");
        PreferenceManager.setString(this,"areaMdl","용산구");
        PreferenceManager.setString(this,"nx","60");
        PreferenceManager.setString(this,"ny","127");
*/


        textViewDSH = (TextView)findViewById(R.id.textView_DSH);
        Button buttonDSH = (Button)findViewById(R.id.button_DSH);
        buttonDSH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDSHWeatherInfo();
            }
        });

        mTxtDate = (TextView)findViewById(R.id.textView_Date);
        mTxtTime = (TextView)findViewById(R.id.textView_Time);
        UpdateDateTimeTextView();

        textViewDYB = (TextView)findViewById(R.id.textView_DYB);
        Button buttonDYB = (Button)findViewById(R.id.button_DYB);
        buttonDYB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Date yesterday;
                Date now, hour6later, mDate;
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                try{
                    //어제 날짜시간 구하기
                    //cal.add(Calendar.DATE, -1);
                    //yesterday=f.parse(simpleDateFormat.format(cal.getTime()));

                    //지금 날짜시간 구하기
                    now = f.parse(simpleDateFormat.format(cal.getTime()));
                    //여섯시간 뒤 구하기
                    cal.add(Calendar.HOUR, +6);
                    hour6later=f.parse(simpleDateFormat.format(cal.getTime()));
                    //설정한 날짜 구하기
                    mDate = new GregorianCalendar(mYear,mMonth,mDay,mHour,mMinute,00).getTime();
                    Toast.makeText(getApplicationContext(), mDate.toString(), Toast.LENGTH_SHORT).show();
                    if((mDate.compareTo(hour6later)>0) || (mDate.compareTo(now)<0)){
                        //mDate가 hour6later보다 크다   //설정 날짜가 여섯시간 뒤 보다 크다.
                        //mDate가 now보다 작다  //설정 날짜가 지금보다 과거이다.
                        //Toast.makeText(getApplicationContext(), "현재시각+ 6시간 이내만 가능합니다", Toast.LENGTH_SHORT).show();
                        textViewDYB.setText("최대 +6시간 까지만 조회 가능합니다.");
                        return;
                    }
                    else {
                        getDYBWeatherInfo();
                    }

                } catch (Exception e){
                    e.printStackTrace();
                }


            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            //case R.id.menu_DSHOption:
            //    break;
            case R.id.menu_DYBOption:
                Intent DYBoptionintent = new Intent(this, OptionDYBActivity.class );
                startActivity(DYBoptionintent);
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    //초단기실황 조회 (현재날씨)
    private void getDSHWeatherInfo() {
        if(DSHWeatherTask != null) {
            DSHWeatherTask.cancel(true);
        }
        DSHWeatherTask = new DSHTask();
        DSHWeatherTask.execute();
    }
    //초단기예보 조회 (날씨예보)
    private void getDYBWeatherInfo() {
        if(DYBWeatherTask != null) {
            DYBWeatherTask.cancel(true);
        }
        DYBWeatherTask = new DYBTask(PreferenceManager.getString(this, "nx"),
                PreferenceManager.getString(this, "ny"),
                this );
        DYBWeatherTask.execute();
    }


}


