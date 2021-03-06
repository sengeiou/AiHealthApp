package com.app.aihealthapp.ui.activity.forum;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.aihealthapp.R;
import com.app.aihealthapp.confing.AppConfig;
import com.app.aihealthapp.core.base.BaseFragment;
import com.app.aihealthapp.core.eventbus.Event;
import com.app.aihealthapp.core.eventbus.EventCode;
import com.app.aihealthapp.core.helper.GsonHelper;
import com.app.aihealthapp.core.helper.SharedPreferenceHelper;
import com.app.aihealthapp.core.helper.ToastyHelper;
import com.app.aihealthapp.core.helper.UserHelper;
import com.app.aihealthapp.core.network.api.ApiUrl;
import com.app.aihealthapp.core.selectlibrary.CitySelect;
import com.app.aihealthapp.core.selectlibrary.Province;
import com.app.aihealthapp.ui.AppContext;
import com.app.aihealthapp.ui.MainActivity;
import com.app.aihealthapp.ui.WebActyivity;
import com.app.aihealthapp.ui.adapter.GridviewAreaAdapter;
import com.app.aihealthapp.ui.bean.CountryCityBean;
import com.app.aihealthapp.ui.mvvm.view.WebTitleView;
import com.app.aihealthapp.util.UrlParseUtil;
import com.app.aihealthapp.util.utils;
import com.app.aihealthapp.view.FragmentProgressWebView;
import com.app.aihealthapp.view.MyPopWindow;

import org.apache.http.util.EncodingUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @Name：AiHealth
 * @Description：健康商圈
 * @Author：Chen
 * @Date：2019/7/24 22:07
 * 修改人：Chen
 * 修改时间：2019/7/24 22:07
 */
public class ForumFragment extends BaseFragment implements WebTitleView {

    @BindView(R.id.toolbar)
    RelativeLayout toolbar;
    @BindView(R.id.ll_location)
    LinearLayout ll_location;
    @BindView(R.id.tv_location)
    TextView tv_location;
    @BindView(R.id.tv_title_bar)
    TextView tv_title_bar;
    @BindView(R.id.webview)
    FragmentProgressWebView webview;

    private MyPopWindow window_city;
    private View popView;
    private View view_empty;
    TextView tvPresentCity;
    TextView tvCheckArea;
    GridView mGridView;
    private List<CountryCityBean> mCountryCityBean;
    private List<CountryCityBean.CityListBean.AreaListBean> AreaList = new ArrayList<>();
    private GridviewAreaAdapter mGridviewAreaAdapter;

//    private Dialog dialog;

    public static ForumFragment getInstance(String title) {
        ForumFragment hf = new ForumFragment();
        hf.mTitle = title;
        return hf;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_forum;
    }

    @Override
    public void initView(View view, Bundle savedInstanceState) {

        ll_location.setVisibility(View.VISIBLE);

        webview.setWebTitleView(this);
        webview.setFocusable(true);//设置有焦点
        webview.setFocusableInTouchMode(true);//设置可触摸


    }

    @Override
    public void initData() {
    }

    /*
     * 初始化城市
     * */
    private void initCity() {
        mGridviewAreaAdapter = new GridviewAreaAdapter(mActivity, AreaList);

        mCountryCityBean = GsonHelper.GsonToList(utils.InitAssetsData(mActivity, "city.json"), CountryCityBean.class, "city");
        popView = getLayoutInflater().inflate(R.layout.layout_popupwindow, null);
        view_empty = popView.findViewById(R.id.view_empty);
        tvPresentCity = popView.findViewById(R.id.tv_present_city);
        tvCheckArea = popView.findViewById(R.id.tv_check_area);
        mGridView = popView.findViewById(R.id.gridview_area);
        view_empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window_city.dismiss();
            }
        });
        tvCheckArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window_city.dismiss();
                ((MainActivity)getActivity()).getDialog().show();
//                dialog.show();
            }
        });
        for (int i = 0; i < mCountryCityBean.size(); i++) {
            if (SharedPreferenceHelper.getProvince(AppContext.getContext()).equals(mCountryCityBean.get(i).getName())) {
                for (int j = 0; j < mCountryCityBean.get(i).getCityList().size(); j++) {
                    if (SharedPreferenceHelper.getCity(AppContext.getContext()).equals(mCountryCityBean.get(i).getCityList().get(j).getName())) {
                        AreaList.addAll(mCountryCityBean.get(i).getCityList().get(j).getAreaList());
                        mGridviewAreaAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
        mGridView.setAdapter(mGridviewAreaAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tv_location.setText(AreaList.get(position).getName());
                SharedPreferenceHelper.setSelect(mActivity, true);
                SharedPreferenceHelper.setAreaId(mActivity, AreaList.get(position).getCode());
                SharedPreferenceHelper.setArea(mActivity, AreaList.get(position).getName());
                window_city.dismiss();

                Intent intent = new Intent();
                intent.setAction("action.check.location");
                mActivity.sendBroadcast(intent);
            }
        });
        window_city = new MyPopWindow(popView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    }

    @Override
    protected boolean isRegisterEventBus() {
        return true;
    }

    @Override
    protected void receiveEvent(Event event) {
        super.receiveEvent(event);
        if (event.getCode() == EventCode.Code.LOGIN_SUCCESS) {
            tv_location.setText(AppConfig.CITY_DEF);
            tvPresentCity.setText("您正在看："+AppConfig.CITY_DEF);
            AreaList.clear();
            mGridviewAreaAdapter.notifyDataSetChanged();
            WebLoadUrl();
        }
    }

    private BroadcastReceiver mRefreshBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("action.check.location")) {
                tvPresentCity.setText("您正在看："+SharedPreferenceHelper.getCity(mActivity));
                String area_id = SharedPreferenceHelper.getAreaId(mActivity);
                if (TextUtils.equals(area_id,AppConfig.AREA_ID_DEF)){
                    tv_location.setText(SharedPreferenceHelper.getCity(AppContext.getContext()));
                }else {
                    tv_location.setText(SharedPreferenceHelper.getArea(AppContext.getContext()));
                }
                for (int i = 0; i < mCountryCityBean.size(); i++) {
                    if (SharedPreferenceHelper.getProvince(AppContext.getContext()).equals(mCountryCityBean.get(i).getName())) {
                        for (int j = 0; j < mCountryCityBean.get(i).getCityList().size(); j++) {
                            if (SharedPreferenceHelper.getCity(AppContext.getContext()).equals(mCountryCityBean.get(i).getCityList().get(j).getName())) {
                                AreaList.clear();
                                AreaList.addAll(mCountryCityBean.get(i).getCityList().get(j).getAreaList());
                                mGridviewAreaAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
                WebLoadUrl();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActivity.unregisterReceiver(mRefreshBroadcastReceiver);
    }

    @Override
    public void loadingData() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("action.check.location");
        mActivity.registerReceiver(mRefreshBroadcastReceiver, intentFilter);
        initCity();
        WebLoadUrl();
//        dialog = new CitySelect(mActivity)
//                .setMainColor(Color.RED)
//                .listener(new CitySelect.OnSelectListener() {
//                    @Override
//                    public void onSelect(Province province, Province.City city,Province.Area area) {
//                        tvPresentCity.setText("您正在看：" + city.name);
//                        if (area!=null){
//                            tv_location.setText(area.name);
//                            SharedPreferenceHelper.setAreaId(mActivity, area.code);
//                            SharedPreferenceHelper.setArea(mActivity,area.name);
//                        }else {
//                            tv_location.setText(city.name);
//                            SharedPreferenceHelper.setAreaId(mActivity, AppConfig.AREA_ID_DEF);
//                            SharedPreferenceHelper.setArea(mActivity,"");
//                        }
//                        SharedPreferenceHelper.setProvince(mActivity, province.name);
//                        SharedPreferenceHelper.setCity(mActivity, city.name);
//                        SharedPreferenceHelper.setCityId(mActivity, city.code);
//                        SharedPreferenceHelper.setSelect(mActivity, true);
//                        Intent intent = new Intent();
//                        intent.setAction("action.check.location");
//                        mActivity.sendBroadcast(intent);
//                    }
//                }).dialog();
    }


    private void WebLoadUrl() {

        tvPresentCity.setText("您正在看：" + SharedPreferenceHelper.getCity(mActivity));
        if (SharedPreferenceHelper.getSelect(AppContext.getContext())) {
            if (TextUtils.equals(SharedPreferenceHelper.getAreaId(AppContext.getContext()),AppConfig.AREA_ID_DEF)){
                tv_location.setText(SharedPreferenceHelper.getCity(AppContext.getContext()));
            }else {
                tv_location.setText(SharedPreferenceHelper.getArea(AppContext.getContext()));
            }

        } else {
            tv_location.setText(SharedPreferenceHelper.getCity(AppContext.getContext()));
        }
        boolean isSlect = SharedPreferenceHelper.getSelect(mActivity);
        String city_code = SharedPreferenceHelper.getCityId(AppContext.getContext());
        String area_code = SharedPreferenceHelper.getAreaId(AppContext.getContext());
        //post访问需要提交的参数
        String postDate;
        if (isLogin()) {
            if (isSlect) {
                postDate = "uid=" + UserHelper.getUserInfo().getId() + "&city_code=" + city_code + "&area_code=" + area_code;
            } else {
                postDate = "uid=" + UserHelper.getUserInfo().getId() + "&city_code=" + city_code + "&area_code=" + 0;
            }
        } else {
            if (isSlect) {
                postDate = "city_code=" + city_code + "&area_code=" + area_code;
            } else {
                postDate = "city_code=" + city_code + "&area_code=" + area_code;
            }
        }
        webview.postUrl(ApiUrl.WebApi.Index, EncodingUtils.getBytes(postDate, "BASE64"));

    }

    @OnClick({R.id.ll_location})
    public void onClick(View v) {
        if (v == ll_location) {
            /*Android N上Popwindow显示位置不正确问题*/
            if (Build.VERSION.SDK_INT >= 24) {
                Rect visibleFrame = new Rect();
                toolbar.getGlobalVisibleRect(visibleFrame);
                int height = toolbar.getResources().getDisplayMetrics().heightPixels - visibleFrame.bottom;
                window_city.setHeight(height);
                window_city.showAsDropDown(toolbar, 0, 0);
            } else {
                window_city.showAsDropDown(toolbar, 0, 0);
            }
        }
    }

    @Override
    public void onTitleResult(String title) {
        tv_title_bar.setText(title);
    }

}
