package com.app.aihealthapp.ui;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.widget.Toast;

import com.app.aihealthapp.R;
import com.app.aihealthapp.confing.AppConfig;
import com.app.aihealthapp.core.base.BaseActivity;
import com.app.aihealthapp.core.base.BaseFragmentPageAdapter;
import com.app.aihealthapp.core.helper.SharedPreferenceHelper;
import com.app.aihealthapp.core.helper.ToastyHelper;
import com.app.aihealthapp.core.selectlibrary.CitySelect;
import com.app.aihealthapp.core.selectlibrary.Province;
import com.app.aihealthapp.core.tablayout.CommonTabLayout;
import com.app.aihealthapp.core.tablayout.listener.CustomTabEntity;
import com.app.aihealthapp.core.tablayout.listener.OnTabSelectListener;
import com.app.aihealthapp.ui.activity.forum.ForumFragment;
import com.app.aihealthapp.ui.activity.home.HomeFragment;
import com.app.aihealthapp.ui.activity.manage.ManageFragment;
import com.app.aihealthapp.ui.activity.mine.MineFragment;
import com.app.aihealthapp.ui.activity.shop.ShopFragment;
import com.app.aihealthapp.ui.bean.TabEntityBean;
import com.app.aihealthapp.view.NoScrollViewPager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;

public class MainActivity extends BaseActivity implements OnTabSelectListener, ViewPager.OnPageChangeListener {
    @BindView(R.id.vpage_main)
    NoScrollViewPager vpageMain;
    @BindView(R.id.tab_main)
    CommonTabLayout tabMain;
    private BaseFragmentPageAdapter mBaseFragmentPageAdapter;
    private ArrayList<Fragment> mFragments;
    private ArrayList<CustomTabEntity> mTabEntities;

    private String[] mTitles;
    private int[] mIconUnselectIds = {
            R.mipmap.home_icon_normal, R.mipmap.management_icon_normal,
            R.mipmap.mall_icon_normal,R.mipmap.forum_icon_normal,
            R.mipmap.ming_icon_normal};
    private int[] mIconSelectIds = {
            R.mipmap.home_icon_select, R.mipmap.management_icon_select,
            R.mipmap.mall_icon_select,R.mipmap.forum_icon_select,
            R.mipmap.mind_icon_select};
    //极光推送
    public static boolean isForeground = false;
    private  Dialog dialog;
    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public String returnToolBarTitle() {
        return null;
    }

    @Override
    public void initView() {
        mFragments = new ArrayList<>();
        mTabEntities = new ArrayList<>();

        IntentFilter intentFilter =new IntentFilter();
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        intentFilter.addAction("action.intent_home");
        intentFilter.addAction("action.intent_mine");
        registerReceiver(mRefreshBroadcastReceiver, intentFilter);

        Bundle bundle = getIntent().getBundleExtra("EXTRA_BUNDLE");
        if(bundle != null){
            //如果bundle存在，取出其中的参数，启动DetailActivity
            String url = bundle.getString("url");
            String title = bundle.getString("title");
            startActivity(new Intent(this,WebActyivity.class).putExtra("url",url).putExtra("title",title));
        }

    }
    @Override
    public void initData() {
        mTitles = getResources().getStringArray(R.array.tab);
        mFragments.add(HomeFragment.getInstance(mTitles[0]));
        mFragments.add(ManageFragment.getInstance(mTitles[1]));
        mFragments.add(ShopFragment.getInstance(mTitles[2]));
        mFragments.add(ForumFragment.getInstance(mTitles[3]));
        mFragments.add(MineFragment.getInstance(mTitles[4]));

        for (int i = 0; i < mTitles.length; i++) {
            mTabEntities.add(new TabEntityBean(mTitles[i], mIconSelectIds[i], mIconUnselectIds[i]));
        }
        mBaseFragmentPageAdapter = new BaseFragmentPageAdapter(getSupportFragmentManager(), mFragments);
        vpageMain.setOffscreenPageLimit(5);
        vpageMain.setNoScroll(true);//设置viewpage 是否可以左右滑动
        vpageMain.setAdapter(mBaseFragmentPageAdapter);
        tabMain.setTabData(mTabEntities);
        tabMain.setOnTabSelectListener(this);
        vpageMain.addOnPageChangeListener(this);
        vpageMain.setCurrentItem(0);
        initCitySelect();
    }

    @Override
    public void onTabSelect(int position) {
        vpageMain.setCurrentItem(position, false);

    }

    @Override
    public void onTabReselect(int position) {

    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int position) {
        tabMain.setCurrentTab(position);

    }

    private void initCitySelect(){
        dialog = new CitySelect(this)
                .setMainColor(Color.RED)
                .listener(new CitySelect.OnSelectListener() {
                    @Override
                    public void onSelect(Province province, Province.City city, Province.Area area) {
//                        Log.e("399",province + "  " + city + "  " + area);
//                        tvPresentCity.setText("您正在看："+city.name);
//                        String area_id;
//                        if (area!=null){
//                            area_id = area.code;
////                            tv_location.setText(area.name);
//                            SharedPreferenceHelper.setArea(mContext,area.name);
//                        }else {
//                            area_id = AppConfig.AREA_ID_DEF;
////                            tv_location.setText(city.name);
//                            SharedPreferenceHelper.setArea(mContext,"");
//                        }

                        SharedPreferenceHelper.setProvince(mContext,province.name);
                        SharedPreferenceHelper.setCity(mContext,city.name);
                        SharedPreferenceHelper.setCityId(mContext,city.code);
                        SharedPreferenceHelper.setSelect(mContext,true);
                        SharedPreferenceHelper.setAreaId(mContext,area==null?AppConfig.AREA_ID_DEF:area.code);
                        SharedPreferenceHelper.setArea(mContext,area==null?"":area.name);

                        Intent intent =new Intent();
                        intent.setAction("action.check.location");
                        sendBroadcast(intent);
                    }
                }).dialog();
    }

    public Dialog getDialog(){
        if (dialog==null){
            initCitySelect();
        }
        return dialog;
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }
    //记录用户首次点击返回键的时间
    private long firstTime = 0;

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() == 0){
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 2000) {
                ToastyHelper.toastyNormal(MainActivity.this, "再按一次退出程序");
                firstTime = secondTime;
            } else {
                AppManager.getAppManager().AppExit(this);
                super.onBackPressed();
            }
        }else{
            getSupportFragmentManager().popBackStack(); //fragment 出栈
        }
    }

    public void StartIntent(int pos){
        vpageMain.setCurrentItem(pos, false);
    }

    private BroadcastReceiver mRefreshBroadcastReceiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("action.intent_home")){
                StartIntent(0);
            }else if (action.equals("action.intent_mine")){
                StartIntent(4);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mRefreshBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        isForeground = true;

        super.onResume();

    }

    @Override
    protected void onPause() {
        isForeground = false;

        super.onPause();
    }
}
