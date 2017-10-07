package engile;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.lang.reflect.Field;

import view.FloatCircleView;
import view.FloatMenuView;

/**
 * Created by Administrator on 2017/10/4.
 * 单例设计。
 * 通过当前上下文，取得Windows的管理者。
 * 管理者控制 自定义view。包括显示隐藏，触摸事件，移动等。
 */

public class FloatViewManager {
    private Context context;
    private WindowManager windowManager;//windowManager控制悬浮窗的显示和隐藏。

    private static FloatViewManager instance;

    private WindowManager.LayoutParams params;

    //悬浮球view
    private FloatCircleView floatCircleView;

    //悬浮球的起始坐标。
    private float startX ;
    private float startY ;

    //悬浮球的起始坐标。这一坐标用来解决小球触摸和点击事件的冲突。
    private float startX0 ;
    private float startY0 ;

    //菜单栏View
    private FloatMenuView floatMenuView;

    //监听的内部类
    private View.OnTouchListener circleViewOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN://当按下的时候，获取相对屏幕密度的xy坐标
                    startX = motionEvent.getRawX();
                    startY = motionEvent.getRawY();

                    startX0 = motionEvent.getRawX();
                    startY0 = motionEvent.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE://移动的时候，悬浮球跟着移动
                    //获取移动中的坐标
                    float x = motionEvent.getRawX();
                    float y = motionEvent.getRawY();
                    //偏移量
                    float dx = x - startX;
                    float dy = y - startY;
                    //获取布局参数对象。重新设置悬浮球的xy位置
                    params.x += dx;
                    params.y += dy;
                    //移动的过程中，改变样式。通知在移动。
                    floatCircleView.setDrawState(true);
                    //刷新界面。指定用新的 布局参数params 刷新 floatCircleView在界面的显示
                    windowManager.updateViewLayout(floatCircleView,params);
                    //起始位置变化为移动后的位置
                    startX = x;
                    startY = y;
                    break;
                case MotionEvent.ACTION_UP://当抬起时，悬浮球附着在两旁
                    float endX = motionEvent.getRawX();//获取最后的X坐标
                    //进行判断.当在屏幕中间线右边或者左边的时，往两边靠拢
                    if (endX > getScreenWidth()/2){
                        params.x = getScreenWidth() - floatCircleView.width;
                    } else {
                        params.x = 0;
                    }
                    //当抬起时，通知移动状态停止。并且悬浮球会重新绘制自身样式
                    floatCircleView.setDrawState(false);
                    //刷新界面。刷新的是悬浮球floatCircleView在界面的布局参数。而悬浮球样式的改变在其内部。
                    windowManager.updateViewLayout(floatCircleView,params);

                    //解决可能有的 触摸事件和点击事件 的冲突.
                    //如果移动后的X坐标 大于 起始的X坐标6个单位距离，则认为是触摸事件，要终止点击事件的执行
                    if (Math.abs(endX - startX0) > 6){//取绝对值
                        return true;//返回true的时候，则不会继续往下执行到 onClick 事件。
                    } else {
                        return  false;
                    }
                default:
                    break;
            }
            return false;
        }
    };

    //获取屏幕宽度
    public int getScreenWidth(){
        return windowManager.getDefaultDisplay().getWidth();
    }

    //获取屏幕高度
    public int getScreenHeight(){
        return windowManager.getDefaultDisplay().getHeight();
    }

    //获取状态栏的高度
    public  int getStatusHeigt (){
        //通过反射的方法获取状态栏的高度
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");//反射.class获取类
            Object o = c.newInstance();//实例化这个类，得到一个具体的对象
            Field field = c.getField("status_bar_height");//获取这个类的field（域）。这个域的对象类型是 这个类里面的一个属性
            int x = (Integer)field.get(o);//再从具体对象的一个属性的值
            return context.getResources().getDimensionPixelSize(x);//返回。值转换成px
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    //构造方法为私有的。也就是进行单例设计
    private FloatViewManager(final Context context) {
        this.context = context;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        //实例化悬浮球View
        floatCircleView = new FloatCircleView(context);
        //注册触摸事件监听器
        floatCircleView.setOnTouchListener(circleViewOnTouchListener);
        //注册点击事件监听器
        floatCircleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //隐藏悬浮球，显示菜单栏  开启动画
                windowManager.removeView(floatCircleView);//隐藏悬浮球
                showFloatMenuView();//显示菜单栏
                floatMenuView.startAnimation();//开启动画
            }
        });

        //实例化菜单View
        floatMenuView = new FloatMenuView(context);
    }

    //显示菜单栏
    private void showFloatMenuView() {
        WindowManager.LayoutParams params2 = new WindowManager.LayoutParams();
        params2.width = getScreenWidth();
        params2.height = getScreenHeight() - getStatusHeigt();//高度为全屏。
        params2.gravity = Gravity.BOTTOM | Gravity.LEFT;
        params2.x = 0;
        params2.y = 0;
        params2.type = WindowManager.LayoutParams.TYPE_PHONE;//布局参数的类型为手机类型。意味着 在所有页面的上面。
        params2.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;//不和其他应用抢焦点
        params2.format = PixelFormat.RGBA_8888;//设置透明度

        windowManager.addView(floatMenuView, params2);//通过manager添加视图到窗口。第一个参数为要添加的view，第二个为view的布局参数
    }

    public static FloatViewManager getInstance(Context context) {
        if (instance == null) {
            synchronized (FloatViewManager.class) {//同步锁。在instance未进行第一次实例化时，防止同时实例化。
                if (instance == null) {
                    instance = new FloatViewManager(context);
                }
            }
        }
        return instance;
    }

     //显示浮动球
    public void showFloatCircleView() {
        //悬浮球的布局参数
        if (params == null){//如果为空，才需要new。因为当点击菜单栏时，重新显示悬浮球，悬浮球的位置需要在原来的位置。只需要add即可。
            params = new WindowManager.LayoutParams();
            params.width = floatCircleView.width;
            params.height = floatCircleView.height;
            params.gravity = Gravity.TOP | Gravity.LEFT;
            params.x = 0;
            params.y = 0;
            params.type = WindowManager.LayoutParams.TYPE_PHONE;//布局参数的类型为手机类型。意味着 悬浮窗在所有页面的上面。
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;//不和其他应用抢焦点
            params.format = PixelFormat.RGBA_8888;//设置透明度
        }
        windowManager.addView(floatCircleView, params);//通过manager添加视图到窗口。第一个参数为要添加的view，第二个为view的布局参数
    }

    //隐藏 菜单栏
    public void hideFloatMenuView() {
        windowManager.removeView(floatMenuView);
    }
}
