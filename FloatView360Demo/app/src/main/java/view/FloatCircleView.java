package view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.administrator.floatview360demo.R;

/**
 * Created by Administrator on 2017/10/4.
 *
 * 自定义控件。
 * 绘制悬浮球
 */

public class FloatCircleView extends View {
    public int width = 200;
    public int height = 200;

    //绘制悬浮球的两只画笔。一个绘制圆形，一个绘制文本内容
    private Paint circlePaint;
    private Paint textPaint;

    //文本内容
    private String text = "50%";

    //是否移动中的状态标识
    private boolean draw = false;

    //移动中，改变的图片
    private Bitmap bitmap;

    public FloatCircleView(Context context) {
        super(context);
        initPaints(); //初始化画笔
    }

    private void initPaints() {
        circlePaint = new Paint();
        circlePaint.setColor(Color.GRAY);
        circlePaint.setAntiAlias(true);//抗锯齿效果

        textPaint = new Paint();
        textPaint.setTextSize(50);
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(true);

        //初始化移动需要的bitmap。并且缩放到合适大小
        Bitmap src = BitmapFactory.decodeResource(getResources(), R.drawable.hj);
        bitmap = Bitmap.createScaledBitmap(src,width,height,true);
    }

//    确定控件的大小。在这方法中，规定了这个自定义view，在调用onDraw方法进行绘制的时候的大小
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(width,height);
    }

//    绘制方法
    @Override
    protected void onDraw(Canvas canvas) {
        //如果移动中，则显示图片，否则正常显示
        if (draw){
            canvas.drawBitmap(bitmap,0,0,null);
        } else {
        //绘制圆形
        canvas.drawCircle(width/2,height/2,width/2,circlePaint);
        //绘制文本
        float textWidth = textPaint.measureText(text);//用画笔去测量文本的宽度
        float x = width/2 - textWidth/2;//确定文本的x坐标

        Paint.FontMetrics metrics = textPaint.getFontMetrics();//获得画笔绘制下的文本规格
//      确定文本的y坐标.descent ascent，基准线下的高度，基准线下的高度。为什么不是除2，而是除4？为什么是+而不是-？
//      + 是因为文本的初始位置在圆的上方。除4 则可能是因为有默认的行距。以后凡是需要精确的数值，则可以将其都显示出来，再一个个去测试值。
        float y = height/2 + (metrics.descent - metrics.ascent)/4;
        canvas.drawText(text,x,y ,textPaint);
        }
    }    

//   在移动中，则进行状态的改变
    public void setDrawState(boolean b) {
        draw = b;
        invalidate();//每当状态改变的时候，需要重新执行draw方法，进行重新绘制
    }
}
