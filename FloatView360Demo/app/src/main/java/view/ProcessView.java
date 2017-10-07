package view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/10/5.
 *
 * 自定义控件
 * 绘制进度球。效果类似于 水不断在注入一个球体
 * 技巧：利用 handler.postDelayed 方延迟执行线程法，在其里面调用自身，这样就成为了一个定时器
 */

public class ProcessView extends View {
    private int width = 250;
    private int height = 250;

    //根据要实现的效果，需要三个画笔
    private Paint circlePaint;//圆形
    private Paint progressPaint;//进度
    private Paint textPaint;//文本

    //绘制的效果都先统一绘制在这个bitmap的画布上
    private Bitmap bitmap;
    private Canvas bitmapCanvas;

    //path用来绘制复杂的图形。在这个项目中，用来绘制水波纹的进度
    private Path path = new Path();

    //进度
    private int progress = 50;//目标进度
    private int max_progress = 100;
    private int current_progress = 0;

    //手势监听
    private GestureDetector detector;

    //次数统计。单击时，重绘次数
    private int count = 50;

    //判断是否单击。主要用于在onDraw时进行判断。单击或者双击有不用的动画效果
    private boolean isSingleTab = false;

    public ProcessView(Context context) {
        super(context);
        init();//进行初始化。主要是画笔
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    public ProcessView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();//进行初始化。主要是画笔
    }

    public ProcessView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();//进行初始化。主要是画笔
    }


    //初始化相关控件和参数
    private void init() {
        //绘制球体
        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);//抗锯齿
        circlePaint.setColor(Color.argb(0xff,0x3a,0x8c,0x6c));

        //绘制进度（水样式）
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setColor(Color.argb(0xff,0x4e,0xc9,0x63));
        //设置过渡模式，也就是通常说的图像混合模式。当两个画笔在同一区域绘制，设置两个图像的显示。
        //下面这句代码则是 设置了progressPaint画笔绘制的图像，只显示重叠部分
        progressPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        // 绘制文本
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(70);

        //自己画图。创建一个空的bitmap，并且在这bitmap上传入一个画布才能进行绘制。
        bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);

        //进度球手势监听
        detector = new GestureDetector(new MyGestureDetector());
        //进度球触摸监听
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //触摸和手势是有些区别的。这里将触摸事件的处理都交给了手势进行处理。
                return detector.onTouchEvent(event);
            }
        });
        setClickable(true);//父控件的点击事件，不会触发到子控件。
    }

    //手势监听-内部类实现
    class MyGestureDetector extends android.view.GestureDetector.SimpleOnGestureListener{
        //双击手势监听
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            isSingleTab = false;
            current_progress = 0;//根据要实现的效果，当前进度设置为0。
            startDoubleTapAnimation();//开启双击动画:水不断注入,水波纹不断平缓,最终变成直线
            return super.onDoubleTap(e);
        }

        //开启双击动画
        private void startDoubleTapAnimation() {
            //利用handler执行一个延迟50毫秒的线程。
            //每次线程首先将当前进度++，并且判断。
            //如果当前进度没有达到指定进度，先重新绘制，然后再延迟50毫秒后执行同样的线程
            //如果达到了，则让当前进度为0，并且关闭。
            //本质的效果像开了个定时器，每隔50毫秒去执行，当达到一定条件停止执行
            handler.postDelayed(new DoubleTabRunable(),50);
        }

        //双击时，需要执行的线程。刷新数据，重绘界面。
        class DoubleTabRunable implements Runnable {
            @Override
            public void run() {
                current_progress++;
                if (current_progress <= progress){
                    invalidate();//重新绘制，调用onDraw方法。因为current_progress是变化的，所以重新绘制会使得进度条有变化。
                    handler.postDelayed(this,50);//再次调用自己
                } else {
                    current_progress = 0;
                    handler.removeCallbacks(this);
                }
            }

        }

        //单击手势监听
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            isSingleTab = true;
            current_progress = progress;//根据单击动画的实现效果，当前的进度等于目标进度
            startSingleTapAnimation();//实现单击动画：进度不变化，出现水波纹震荡，最后平缓。
            return super.onSingleTapConfirmed(e);
        }
    }

    //单击动画
    private void startSingleTapAnimation() {
        handler.postDelayed(new SingleTapRuanble(),200);
    }

    //单击时，需要执行的线程
    class SingleTapRuanble implements Runnable {
        @Override
        public void run() {
            count--;
            if (count >= 0){
                invalidate();
                handler.postDelayed(this,200);
            }else {
                count = 50;
                handler.removeCallbacks(this);
            }
        }

    }

    //确定这个view在绘制显示时的大小
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(width,height);
    }

    //在这进行绘制.
    @Override
    protected void onDraw(Canvas canvas) {
        //画圆
        bitmapCanvas.drawCircle(width/2,height/2,width/2,circlePaint);

        //绘制水波纹进度。基本思想是绘制一个矩形。不过矩形除了上面的边使用贝塞尔曲线外，其他都是直线。
        path.reset();//重置path的所有属性
        //起始点定义。重新规定路径的起始坐标，默认为（0,0）。moveTo()移动画笔。
        //这里将起始点的x移动到右边，y则是变化的。也就是矩形的右上角的点
        float y = (1 - (float)current_progress/max_progress) * height;
        path.moveTo(width,y);
        //第二个点定义。右下角，绘制直线。也就是右面的边
        path.lineTo(width,height);
        //第三个点定义。左下角，绘制直线。也就是下面的边
        path.lineTo(0,height);
        //第四个点定义。左上角，绘制直线。也就是左面的边
        path.lineTo(0,y);
        //绘制上面的边。也就是贝塞尔曲线。
        //根据进度球宽度，设定需要的贝塞尔曲线的周期。如：250的宽度，那么7个周期为40的即可。
        //绘制贝赛尔曲线需要起始点，控制点和结束点。rQuadTo方法只需要两个参数，起始点默认为 未闭合路径的最后一个点。
        //一个循环意味着一个周期的贝塞尔曲线
        if (!isSingleTab){//双击
            //双击效果：贝塞尔曲线逐渐变得平缓，最后成为直线的效果。
            //其实就是控制了贝赛尔曲线的振幅。也就是参数的控制点的y坐标。使得y坐标逐渐变成0。也就是rQuadTo中的第二个参数
            //在这里设定了振幅为10的话，也就是控制点的y坐标，随着当前进度不断增加到接近目标进度，那么百分比就会不断增加
            //这时用1减去他们的百分比，就会不断减小。用这结果去乘振幅。就能使得振幅不断减小.
            float d = (1 - ((float)current_progress / progress)) * 10;
            for (int i = 0; i < 7 ; i++){
                path.rQuadTo(10,d,20,0);
                path.rQuadTo(10,-d,20,0);
            }
        } else {//单击
            //单击效果：水不断震荡，最后便于平缓。
            //其实就是贝塞尔曲线每次周期不断取反，也就是高的变低，低的变高.
            //这里是水波纹波动50次，也就是50个周期.为什么要模2？
            //因为count是每次减1。如果模2的结果是0，那么为一个周期。再减1，模2的结果不是0.则为另外一个周期，就可以去实现相反的效果。
            //要使得贝赛尔曲线逐渐变得平缓，和上面写的一样道理。使得振幅不断减小.注意count是每次-1

            //第一解决贝塞尔曲线不断取反。第二解决贝塞尔曲线振幅也就是控制点的y坐标不断减小。
            //以后凡是遇到类似这种，需要多个来配合的，那么先解决一个，再解决下一个。
            float d = (float)count/50 * 10;
            if (count%2 == 0){
                for (int i = 0; i < 7 ; i++){
                    path.rQuadTo(20,d,40,0);
                    path.rQuadTo(20,-d,40,0);
                }
            } else {
                for (int i = 0; i < 7 ; i++){
                    path.rQuadTo(20,-d,40,0);
                    path.rQuadTo(20,d,40,0);
                }
            }

        }

        path.close();//路径闭合
        bitmapCanvas.drawPath(path,progressPaint);

        //绘制文本
        String text = (int)(((float)current_progress/max_progress) * 100) + "%";
        //获取文本宽度。便于规定绘制文本时候的x坐标
        float textWidth = textPaint.measureText(text);
        //获取文本规格。便于规定绘制文本时候的y坐标；
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float baseLine = height/2 - (metrics.ascent + metrics.descent)/2;
        //开始绘制文本
        bitmapCanvas.drawText(text, width/2-textWidth/2,baseLine,textPaint);

        //利用自定义bitmap的画布绘制完毕后。再通过显示的画布，绘制这自定义的bitmap
        canvas.drawBitmap(bitmap,0,0,null);
    }

}
