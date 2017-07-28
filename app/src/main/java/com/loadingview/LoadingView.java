package com.loadingview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by wujun on 2017/7/26.
 *
 * @author madreain
 * @desc
 */

public class LoadingView extends View {
    //屏幕的宽高
    int mwidth;
    int mheight;
    //主
    Paint mPaint;
    //
    Path mPath;
    RectF loadingrectF;
    Paint circlePaint;
//    Path circlePath;
//    float onangle=30;
////    //颜色
//    private static final int[] DEFAULT_COLORS = new int[]{
//            Color.RED, Color.GREEN, Color.BLUE,Color.YELLOW
//    };
//    private int mColorIndex=0;
//    private int mCurrentColor=DEFAULT_COLORS[mColorIndex];
//    float[] mCurrentPosition;
//    PathMeasure mPathMeasure;
    //成功失败
    Paint textPaint;
    Paint textCirclePaint;
    //用于计算文字的宽高
    Rect textrect;
    float textheight;
    float textwidth;

    //loading的动效
    ValueAnimator loadingvalueAnimator;
    //成功的动效
    ValueAnimator successvalueAnimator;
    //失败的动效
    ValueAnimator errorvalueAnimator;
    //loading 动画数值(用于控制动画状态,因为同一时间内只允许有一种状态出现,具体数值处理取决于当前状态)
    private float mAnimatorValue = 0;

    //loading 用于控制动画状态转换
    private Handler mAnimatorHandler;

    //当前动画状态
    private State mcurrentState = State.LODING;

    //loading状态，loading，成功，失败
    private enum State {
        LODING,
        SUCCES,
        ERROR,
    }

    public LoadingView(Context context) {
        super(context);
        initPaint();
        initHandler();
        initListener();
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();
        initHandler();
        initListener();
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
        initHandler();
        initListener();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mwidth = w;
        mheight = h;
    }

    private void initPaint() {
//        mCurrentPosition=new float[2];
//        circlePath=new Path();
//        circlePath.moveTo(100,100);
//        mPathMeasure=new PathMeasure(circlePath,true);


        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(16);
        mPaint.setStyle(Paint.Style.STROKE);
//        LinearGradient shader = new LinearGradient(3, 3, (mwidth-3), mheight-3,DEFAULT_COLORS,null, Shader.TileMode.MIRROR);
//        RadialGradient shader=new RadialGradient(mwidth/2,mheight/2,mwidth,DEFAULT_COLORS,null, Shader.TileMode.CLAMP);
//        mPaint.setShader(shader);
        circlePaint= new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStrokeCap(Paint.Cap.ROUND);
        circlePaint.setStrokeWidth(16);


        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLUE);
        textPaint.setStrokeCap(Paint.Cap.ROUND);
        textPaint.setStrokeWidth(2);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setTextSize(60);

        textCirclePaint = new Paint();
        textCirclePaint.setAntiAlias(true);
        textCirclePaint.setColor(Color.BLUE);
        textCirclePaint.setStrokeCap(Paint.Cap.ROUND);
        textCirclePaint.setStrokeWidth(2);
        textCirclePaint.setStyle(Paint.Style.STROKE);

        textrect = new Rect();
    }

    private void initListener() {

        // 创建0－1的一个过程,任何复杂的过程都可以采用归一化，然后在addUpdateListener回调里去做自己想要的变化
        loadingvalueAnimator = ValueAnimator.ofFloat(0, 1);
        // 设置过程的时间为2S
        loadingvalueAnimator.setDuration(2000);
//        android里提供的插值器有如下一些：
//
//        AccelerateInterpolator　　　　　     加速，开始时慢中间加速
//        DecelerateInterpolator　　　 　　   减速，开始时快然后减速
//        AccelerateDecelerateInterolator　   先加速后减速，开始结束时慢，中间加速
//        AnticipateInterpolator　　　　　　  反向 ，先向相反方向改变一段再加速播放
//        AnticipateOvershootInterpolator　   反向加回弹，先向相反方向改变，再加速播放，会超出目的值然后缓慢移动至目的值
//        BounceInterpolator　　　　　　　  跳跃，快到目的值时值会跳跃，如目的值100，后面的值可能依次为85，77，70，80，90，100
//        CycleIinterpolator　　　　　　　　 循环，动画循环一定次数，值的改变为一正弦函数：Math.sin(2 * mCycles * Math.PI * input)
//        LinearInterpolator　　　　　　　　 线性，线性均匀改变
//        OvershootInterpolator　　　　　　  回弹，最后超出目的值然后缓慢改变到目的值
//        TimeInterpolator　　　　　　　　   一个接口，允许你自定义interpolator，以上几个都是实现了这个接口
//        // 设置这个过程
//        valueAnimator.setInterpolator(new BounceInterpolator());
//        valueAnimator.start();

        successvalueAnimator = ValueAnimator.ofFloat(0, 1);
        successvalueAnimator.setDuration(500);

        errorvalueAnimator = ValueAnimator.ofFloat(0, 1);
        errorvalueAnimator.setDuration(500);
    }


    private void addLoadingListener() {
        loadingvalueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                // 不断回调的在0-1这个范围内，经过插值器插值之后的返回值
                mAnimatorValue = (float) valueAnimator.getAnimatedValue();
                // 获取当前点坐标封装到mCurrentPosition
//                mPathMeasure.getPosTan(mAnimatorValue, mCurrentPosition, null);
                //重绘
                invalidate();

            }
        });

        loadingvalueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
//                mcurrentState = State.LODING;
//                //转一圈，执行一次
//                if(mColorIndex<DEFAULT_COLORS.length-1){
//                    mColorIndex+=1;
//                }else {
//                    mColorIndex=0;
//                }
//                mCurrentColor=DEFAULT_COLORS[mColorIndex];

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                // getHandle发消息通知动画状态更新
                mAnimatorHandler.sendEmptyMessage(0);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private void addSuccesLoadingListener() {
//        successvalueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//
//
//            }
//        });

        successvalueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                mcurrentState = State.SUCCES;
                invalidate();
            }

            @Override
            public void onAnimationEnd(Animator animator) {

                if (mWindowManager != null && loadingView != null) {
//                    mWindowManager.removeView(loadingView);
                    loainglayoutParams = new WindowManager.LayoutParams(0,
                            0, WindowManager.LayoutParams.TYPE_TOAST,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
                    mWindowManager.updateViewLayout(loadingView, loainglayoutParams);
                }

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private void addErrorLoadingListener() {
//        errorvalueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//
//
//            }
//        });

        errorvalueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                mcurrentState = State.ERROR;
                invalidate();
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (mWindowManager != null && loadingView != null) {
//                    mWindowManager.removeView(loadingView);
                    loainglayoutParams = new WindowManager.LayoutParams(0,
                            0, WindowManager.LayoutParams.TYPE_TOAST,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
                    mWindowManager.updateViewLayout(loadingView, loainglayoutParams);
                }

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private void initHandler() {
        mAnimatorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (mcurrentState) {
                    case LODING:
                        //保持loading时一直执行动画
                        loadingvalueAnimator.start();
                        break;
                    case SUCCES:
                        break;
                    case ERROR:
                        break;
                    default:
                        break;
                }

            }
        };
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //移到屏幕中间
        canvas.translate(mwidth / 2, mheight / 2);
        drawLoading(canvas);
    }

    /**
     * @param canvas 知识点：canvas.drawText(text, x, y, paint)
     *               x默认是这个字符串的左边在屏幕的位置，如果设置了paint.setTextAlign(Paint.Align.CENTER);那就是字符的中心，
     *               y是指定这个字符baseline在屏幕上的位置
     */
    private void drawLoading(Canvas canvas) {
        switch (mcurrentState) {
            case LODING:
//                RadialGradient shader=new RadialGradient(90,90,100,DEFAULT_COLORS,null, Shader.TileMode.CLAMP);
//                mPaint.setShader(shader);
//                mPaint.setColor(mCurrentColor);
                mPath = new Path();
                loadingrectF = new RectF(-100, -100, 100, 100);
                mPath.addArc(loadingrectF, mAnimatorValue * 360, 240);
                canvas.drawPath(mPath, mPaint);
                canvas.drawColor(Color.parseColor("#33000000"));


                // 绘制对应目标
//                canvas.drawCircle(mCurrentPosition[0], mCurrentPosition[1], 10, circlePaint);


//                circlePath=new Path();
//                for(int i=0;i<10;i++){
//                    circlePath.addCircle(mAnimatorValue*100*(float) Math.cos(onangle*i),mAnimatorValue*100*(float) Math.sin(onangle*i),16, Path.Direction.CW);
////                    circlePath.addCircle(mAnimatorValue*100,mAnimatorValue*100,16, Path.Direction.CW);
////                    circlePath.addCircle((float) (mAnimatorValue*100*Math.sin(onangle*i)), (float) (mAnimatorValue*100*Math.cos(onangle*i)),16, Path.Direction.CW);
////                    circlePath.addCircle((float) (100*Math.cos(mAnimatorValue*onangle*i)), (float) (100*Math.sin(mAnimatorValue*onangle*i)),16, Path.Direction.CW);
////                    circlePath.addCircle((float) (100*Math.sin(mAnimatorValue*onangle*i)), (float) (100*Math.cos(mAnimatorValue*onangle*i)),16, Path.Direction.CW);
//                }
//                canvas.drawPath(circlePath,circlePaint);

                break;
            case SUCCES://获取文字的宽度及其高度
                String success = "成功";
                textPaint.getTextBounds(success, 0, success.length(), textrect);
                textheight = textrect.height();
                textwidth = textrect.width();

                canvas.drawCircle(0, 0, 100, textCirclePaint);
                canvas.drawText(success, -textwidth / 2, textheight / 2, textPaint);
                break;
            case ERROR:
                String error = "失败";
                textPaint.getTextBounds(error, 0, error.length(), textrect);
                textheight = textrect.height();
                textwidth = textrect.width();

                canvas.drawCircle(0, 0, 100, textCirclePaint);
                canvas.drawText(error, -textwidth / 2, textheight / 2, textPaint);
                break;
            default:
                break;
        }
    }

    /**
     * 设置loading开始
     */
    public void startLoading() {
        if (loadingvalueAnimator != null) {
            mcurrentState = State.LODING;
            addLoadingListener();
            successvalueAnimator.removeAllListeners();
            errorvalueAnimator.removeAllListeners();
            loadingvalueAnimator.start();
        }
    }

    /**
     * 成功
     */
    public void setSuccess() {
        loadingvalueAnimator.removeAllListeners();
        errorvalueAnimator.removeAllListeners();
        addSuccesLoadingListener();
        successvalueAnimator.start();
    }

    /**
     * 失败
     */
    public void setError() {
        loadingvalueAnimator.removeAllListeners();
        successvalueAnimator.removeAllListeners();
        addErrorLoadingListener();
        errorvalueAnimator.start();
    }


    private WindowManager mWindowManager;
    private WindowManager.LayoutParams loainglayoutParams;
    LoadingView loadingView;

    /**
     * 纯代码生成时，将loading框加在最上层
     * <p>
     * Activity －> PhoneWindow －> DecorView －> ViewGroup－> View
     */
    public void addViewStartLoading(LoadingView loadingView, Context context) {
        this.loadingView = loadingView;
        loainglayoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);

        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
            mWindowManager.addView(loadingView, loainglayoutParams);
        } else {
            mWindowManager.updateViewLayout(loadingView, loainglayoutParams);
        }
        startLoading();

    }

}
