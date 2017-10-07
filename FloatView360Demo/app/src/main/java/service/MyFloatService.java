package service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import engile.FloatViewManager;

/**
 * Created by Administrator on 2017/10/4.
 */

public class MyFloatService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        FloatViewManager manager = FloatViewManager.getInstance(this);
        manager.showFloatCircleView();//弹出一个窗口，需要权限。>=23，需要动态申请。
        super.onCreate();
    }
}
