package com.app.aihealthapp.ui.activity.home;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.aihealthapp.R;
import com.app.aihealthapp.core.PickerView.TimePickerView;
import com.app.aihealthapp.core.helper.PickerViewHelper;
import com.app.aihealthapp.ui.AppContext;
import com.app.aihealthapp.ui.AppManager;
import com.app.aihealthapp.util.utils;
import com.crrepa.ble.CRPBleClient;
import com.crrepa.ble.conn.CRPBleConnection;
import com.crrepa.ble.conn.CRPBleDevice;
import com.crrepa.ble.conn.bean.CRPSleepInfo;
import com.crrepa.ble.conn.listener.CRPBleConnectionStateListener;
import com.crrepa.ble.conn.listener.CRPSleepChangeListener;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @Name：AiHealthApp
 * @Description：睡眠测量
 * @Author：Chen
 * @Date：2019/10/15 22:40
 * 修改人：Chen
 * 修改时间：2019/10/15 22:40
 */
public class SleepActivity extends Activity implements CRPSleepChangeListener{
    @BindView(R.id.img_back)
    ImageView img_back;
    @BindView(R.id.tv_title_bar)
    TextView tv_title_bar;

    @BindView(R.id.tv_sleep_total_time)
    TextView tv_sleep_total_time;
    @BindView(R.id.tv_sleep_restful_time)
    TextView tv_sleep_restful_time;
    @BindView(R.id.tv_sleep_light_time)
    TextView tv_sleep_light_time;
    @BindView(R.id.tv_sleep_sober_time)
    TextView tv_sleep_sober_time;

    @BindView(R.id.tv_sleep_quality)
    TextView tv_sleep_quality;
    @BindView(R.id.tv_date)
    TextView tv_date;
    @BindView(R.id.tv_check_time)
    TextView tv_check_time;

    private String Device_no;
    private CRPBleClient mCRPBleClient;
    private CRPBleDevice mBleDevice;
    private CRPBleConnection mBleConnection;

    private String hours;
    private String minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // 添加Activity到堆栈
        AppManager.getAppManager().addActivity(this);
        setContentView(R.layout.activity_sleep);
        // 添加Activity到堆栈
        AppManager.getAppManager().addActivity(this);
        // 通过注解绑定控件
        ButterKnife.bind(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        initView();
    }
    private void initView(){

        Device_no = getIntent().getStringExtra("Device_no");
        mCRPBleClient = AppContext.getBleClient();
        mBleDevice = mCRPBleClient.getBleDevice(Device_no);
        mBleConnection = mBleDevice.connect();
        mBleConnection.setConnectionStateListener(new CRPBleConnectionStateListener() {
            @Override
            public void onConnectionStateChange(int i) {
                if (i==CRPBleConnectionStateListener.STATE_CONNECTED){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mBleConnection.setSleepChangeListener(SleepActivity.this);
                            //获取今日睡眠时间
                            mBleConnection.syncSleep();

                        }
                    });
                }
            }
        });
        tv_sleep_total_time.setText(Html.fromHtml("<font color='#01d1b1'><big><big>--</big></big></font>小时<font color='#01d1b1'><big><big>--</big></big></font>分钟"));
        tv_sleep_restful_time.setText(Html.fromHtml("<font color='#01d1b1'><big><big>--</big></big></font>小时<font color='#01d1b1'><big><big>--</big></big></font>分钟"));
        tv_sleep_light_time.setText(Html.fromHtml("<font color='#01d1b1'><big><big>--</big></big></font>小时<font color='#01d1b1'><big><big>--</big></big></font>分钟"));
        tv_sleep_sober_time.setText(Html.fromHtml("<font color='#01d1b1'><big><big>--</big></big></font>小时<font color='#01d1b1'><big><big>--</big></big></font>分钟"));

        tv_date.setText(utils.parseDateToYearMonthDayWeek(new Date()));

    }
    @OnClick({R.id.img_back,R.id.tv_check_time})
    public void onClick(View v){
        if (v==img_back){
            finish();
        }else if (v==tv_check_time){
            new PickerViewHelper().TimePickerViewShow(this, TimePickerView.Type.YEAR_MONTH_DAY, new TimePickerView.OnTimeSelectListener() {
                @Override
                public void onTimeSelect(Date date, View v) {
                    String time = PickerViewHelper.getYesMonthDayTime(date);
                    tv_check_time.setText(time);
                    tv_date.setText(utils.parseDateToYearMonthDayWeek(date));
                }
            });
        }
    }

    @Override
    public void onSleepChange(final CRPSleepInfo crpSleepInfo) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //睡眠总时间
                int TotalTime = crpSleepInfo.getTotalTime();
                String total = String.format(getResources().getString(R.string.sleep_time), utils.MinuteToHours(TotalTime), utils.MinuteInHours(TotalTime));
                tv_sleep_total_time.setText(utils.SleepTime(SleepActivity.this,total));

                //深睡眠时间
                int restfulTime = crpSleepInfo.getRestfulTime();
                String restful = String.format(getResources().getString(R.string.sleep_time), utils.MinuteToHours(restfulTime), utils.MinuteInHours(restfulTime));
                tv_sleep_restful_time.setText(utils.SleepTime(SleepActivity.this,restful));

                //浅睡眠时间
                int lightTime = crpSleepInfo.getLightTime();
                String light = String.format(getResources().getString(R.string.sleep_time), utils.MinuteToHours(lightTime), utils.MinuteInHours(lightTime));
                tv_sleep_light_time.setText(utils.SleepTime(SleepActivity.this,light));
                //清醒时间
                int soberTime = crpSleepInfo.getSoberTime();
                String sober = String.format(getResources().getString(R.string.sleep_time), utils.MinuteToHours(soberTime), utils.MinuteInHours(soberTime));
                tv_sleep_sober_time.setText(utils.SleepTime(SleepActivity.this,sober));

                //睡眠质量分四个等级，主要参照深睡眠时间来确定睡眠质量：
                // 深睡眠时间大于等于2.2小时，睡眠质量为优；
                // 深度睡眠时间大于等于1.75小时，睡眠质量为良；
                // 深度睡眠时间大于等于1.3小时，睡眠质量为中，
                // 深度睡眠时间小于1.3小时，睡眠质量为差
                if (crpSleepInfo.getRestfulTime()>132){
                    tv_sleep_quality.setText("优");
                }else if (crpSleepInfo.getRestfulTime()>105&&crpSleepInfo.getRestfulTime()<=132){
                    tv_sleep_quality.setText("良");
                }else if (crpSleepInfo.getRestfulTime()>78&&crpSleepInfo.getRestfulTime()<=105){
                    tv_sleep_quality.setText("中");
                }else {
                    tv_sleep_quality.setText("差");
                }
            }
        });

    }

    @Override
    public void onPastSleepChange(int i, CRPSleepInfo crpSleepInfo) {

    }
}
