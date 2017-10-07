package view;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import com.example.administrator.floatview360demo.R;

import engile.FloatViewManager;

/**
 * Created by April on 2017/10/6.
 *
 * 底部菜单栏的实现。
 * 继承了LinearLayout。实质也是一个View.
 * View就会有样式。View的样式可以通过代码（也就是在onDraw绘制），也可以通过xml文件绑定。
 * 在这个菜单栏View中的样式绑定了xml文件。也就是 addView()方法。
 *
 */

public class FloatMenuView extends LinearLayout {

    private TranslateAnimation animation;

    public FloatMenuView(Context context) {
        super(context);
        View root = View.inflate(getContext(), R.layout.float_menu_view,null);//找到xml文件样式
        LinearLayout linearLayout = root.findViewById(R.id.linearLayout);

        //实例化动画。从下到上面移入。RELATIVE_TO_SELF 其实就是参照物。这里是xy起点和终点的参照物。
        //在这里，其实就是以菜单栏自身的左上角为原点.
        animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,0,
                Animation.RELATIVE_TO_SELF,1.0f,Animation.RELATIVE_TO_SELF,0);
        animation.setDuration(1000);//动画时间
        animation.setFillAfter(true);//动画结束后，是否保留最后的状态
        linearLayout.setAnimation(animation);//绑定动画

        //触摸事件。隐藏菜单栏,显示悬浮球.
        //根据需求，当点击到菜单栏以外（也就是非灰色区域）的地方，才需要隐藏。
        //而在下面设置点击监听事件的时候，是替root，也就是整个最高层级的控件设置的监听。
        //这样的话，点击到了菜单栏也隐藏起来了，而实际点击菜单栏时是不需要隐藏的。
        //有个技巧：当替父控件设置了点击监听，那么整个父控件（包括子控件）都会给监听到。如果子控件不需要给监听到，
        //那么给子控件设置一个属性。clickable="true" 即可。
        root.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                FloatViewManager manager = FloatViewManager.getInstance(getContext());
                manager.hideFloatMenuView();//隐藏菜单栏
                manager.showFloatCircleView();//显示悬浮球
                return false;
            }
        });

        addView(root);//将xml样式文件添加到这个View。也就是绑定。
    }

    //开启动画
    public void startAnimation(){
        animation.start();
    }

}
