package com.app.aihealthapp.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;

import com.app.aihealthapp.core.helper.SharedPreferenceHelper;
import com.app.aihealthapp.core.helper.ToastyHelper;
import com.app.aihealthapp.core.helper.UserHelper;
import com.app.aihealthapp.ui.AppContext;
import com.app.aihealthapp.ui.WebActyivity;
import com.app.aihealthapp.ui.activity.home.DoctorListActivity;
import com.app.aihealthapp.ui.activity.home.HealthAskActivity;
import com.app.aihealthapp.ui.mvvm.view.WebTitleView;

/**
 * @Name：AiHealth
 * @Description：描述信息
 * @Author：Chen
 * @Date：2019/8/18 19:17
 * 修改人：Chen
 * 修改时间：2019/8/18 19:17
 */
public class ProgressWebView extends WebView {

    private WebViewProgressBar progressBar;//进度条的矩形（进度线）
    private Handler handler;
    private Context context;
    private WebTitleView mWebTitleView;

    public ProgressWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        //实例化进度条
        progressBar = new WebViewProgressBar(context);
        //设置进度条的size
        progressBar.setLayoutParams(new ViewGroup.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //刚开始时候进度条不可见
        progressBar.setVisibility(GONE);
        //把进度条添加到webView里面
        addView(progressBar);
        //初始化handle
        handler = new Handler();
        initSettings();
    }

    public void setWebTitleView(WebTitleView mWebTitleView){
        this.mWebTitleView = mWebTitleView;
    }
    private void initSettings() {
        // 初始化设置
        WebSettings mSettings = this.getSettings();
        mSettings.setJavaScriptEnabled(true);//开启javascript
        mSettings.setDomStorageEnabled(true);//开启DOM
        mSettings.setDefaultTextEncodingName("utf-8");//设置字符编码
        //设置web页面
        mSettings.setAllowFileAccess(true);//设置支持文件流
        mSettings.setSupportZoom(true);// 支持缩放
        mSettings.setBuiltInZoomControls(true);// 支持缩放
        //不显示webview缩放按钮
        mSettings.setDisplayZoomControls(false);
        mSettings.setUseWideViewPort(true);// 调整到适合webview大小
        mSettings.setLoadWithOverviewMode(true);// 调整到适合webview大小
        mSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);// 屏幕自适应网页,如果没有这个，在低分辨率的手机上显示可能会异常
        mSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        //提高网页加载速度，暂时阻塞图片加载，然后网页加载好了，在进行加载图片
        mSettings.setBlockNetworkImage(true);
        mSettings.setAppCacheEnabled(true);//开启缓存机制

        mSettings.setAllowFileAccessFromFileURLs(true);
        mSettings.setAllowUniversalAccessFromFileURLs(true);
        setWebViewClient(new MyWebClient());
        setWebChromeClient(new MyWebChromeClient());
        addJavascriptInterface(new WebAppInterface(), "jsAndroid");

        setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                    if (canGoBack()) {
                        goBack();
                        return true;

                    } else {
                        return false;
                    }
                }
                return  false;
            }
        });
    }
    public class WebAppInterface {
        @JavascriptInterface
        public String jsCallToken() {
            return SharedPreferenceHelper.getUserToken(AppContext.getContext());
        }
        @JavascriptInterface
        public int jsCallUId() {
            return UserHelper.getUserInfo().getId();
        }
    }

    /**
     * 自定义WebChromeClient
     */
    private class MyWebChromeClient extends WebChromeClient {
        /**
         * 进度改变的回掉
         *
         * @param view        WebView
         * @param newProgress 新进度
         */
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                progressBar.setProgress(100);
                handler.postDelayed(runnable, 200);//0.2秒后隐藏进度条

            } else if (progressBar.getVisibility() == GONE) {
                progressBar.setVisibility(VISIBLE);
            }
            //设置初始进度10，这样会显得效果真一点，总不能从1开始吧
            if (newProgress < 10) {
                newProgress = 10;
            }
            //不断更新进度
            progressBar.setProgress(newProgress);
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            Log.d("ANDROID_LAB", "TITLE=" + title);
            if (!TextUtils.isEmpty(title)){
                mWebTitleView.onTitleResult(title);
            }
        }
    }
    private class MyWebClient extends WebViewClient {
        /**
         * 加载过程中 拦截加载的地址url
         *
         * @param view
         * @param url  被拦截的url
         * @return
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {

            if (url.startsWith("navigation://question")){//立即咨询
                context.startActivity(new Intent(context, HealthAskActivity.class));
                return true;
            }else if (url.startsWith("navigation://doctor?cate_id=16")){//中医问诊
                context.startActivity(new Intent(context, DoctorListActivity.class).putExtra("cate_id",16));
                return true;
            }else if (url.startsWith("navigation://doctor?cate_id=10")){//疑难杂症
                context.startActivity(new Intent(context, DoctorListActivity.class).putExtra("cate_id",10));
                return true;
            }
//            // 如下方案可在非微信内部WebView的H5页面中调出微信支付
//            if (url.startsWith("weixin://wap/pay?")) {
//                //如果return false  就会先提示找不到页面，然后跳转微信
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_VIEW);
//                intent.setData(Uri.parse(url));
//                context.startActivity(intent);
//                return true;
//            }else if (url.startsWith("shopdetail://?")){//活动商户详情
//                context.startActivity(new Intent(context, MerChantDetailsActivity.class).putExtra("shop_id",Integer.parseInt(Utils.getValueByName(url,"id"))));
//                return true;
//
//            }else if (url.startsWith("gooddetail://?")){//活动产品详情
//                context.startActivity(new Intent(context,MerchantServiceDetailActivity.class).putExtra("goods_id",Integer.parseInt(Utils.getValueByName(url,"id"))));
//                return true;
//
//            }else if (url.startsWith("navigate://?")){//活动商户导航
//                CircleDialogHelper.ShowBottomDialog((AppCompatActivity) context, context.getResources().getStringArray(R.array.navigation), new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        if (position == 0) {
//                            if (AMapUtil.isInstallByRead("com.autonavi.minimap")) {
//                                AMapUtil.setUpGaodeAppByMine((Activity) context, Utils.getValueByName(url,"ycoordinate"), Utils.getValueByName(url,"xcoordinate"),Utils.getValueByName(url,"shopname"));
//                            } else {
//                                ToastyHelper.toastyNormal((Activity) context,"没有安装高德地图客户端");
//                            }
//                        } else {
//                            if (AMapUtil.isInstallByRead("com.baidu.BaiduMap")) {
//                                AMapUtil.setUpBaiduAPPByMine((Activity) context,Utils.getValueByName(url,"shopname"));
//                            } else {
//                                ToastyHelper.toastyNormal((Activity) context,"没有安装百度地图客户端");
//                            }
//                        }
//                    }
//                });
//                return true;
//            }else if (url.startsWith("tel:")){
//                if (new PermissionHelper().RequestPermisson(context, Permission.CALL_PHONE)){
//                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Utils.getValueByName(url,"tel")));
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    context.startActivity(intent);
//                }
//                return true;
//            }
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            // 关闭图片加载阻塞
            view.getSettings().setBlockNetworkImage(false);
        }


        /**
         * 页面加载过程中，加载资源回调的方法
         *
         * @param view
         * @param url
         */
        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        /**
         * 页面开始加载调用的方法
         *
         * @param view
         * @param url
         * @param favicon
         */
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);
            ProgressWebView.this.requestFocus();
            ProgressWebView.this.requestFocusFromTouch();
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
            //此方法是为了处理在5.0以上Htts的问题，必须加上
            handler.proceed();
        }
    }
    /**
     *刷新界面（此处为加载完成后进度消失）
     */
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            progressBar.setVisibility(View.GONE);
        }
    };
}

