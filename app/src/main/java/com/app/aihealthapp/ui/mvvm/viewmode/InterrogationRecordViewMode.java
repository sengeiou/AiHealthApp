package com.app.aihealthapp.ui.mvvm.viewmode;

import com.app.aihealthapp.core.base.BaseMode;
import com.app.aihealthapp.ui.bean.InterrogationRecordBean;
import com.app.aihealthapp.ui.mvvm.view.InterrogationRecordView;

import java.util.ArrayList;
import java.util.List;

/**
 * @Name：AiHealth
 * @Description：描述信息
 * @Author：Chen
 * @Date：2019/8/19 22:24
 * 修改人：Chen
 * 修改时间：2019/8/19 22:24
 */
public class InterrogationRecordViewMode {

    private InterrogationRecordView mInterrogationRecordView;
    private BaseMode mBaseMode;

    public InterrogationRecordViewMode(InterrogationRecordView mInterrogationRecordView) {
        this.mInterrogationRecordView = mInterrogationRecordView;
        mBaseMode = new BaseMode();
    }

    public List<InterrogationRecordBean> getDatas(){
        List<InterrogationRecordBean> datas = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            InterrogationRecordBean bean = new InterrogationRecordBean();
            datas.add(bean);
        }
        return datas;
    }
}
