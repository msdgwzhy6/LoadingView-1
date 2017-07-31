package com.loadingview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by wujun on 2017/7/31.
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


    Paint circlePaint;
    float radius = 100;
    float circleRadius = 16;
    //设置转圈的圆点数量
    private int roundCount = 10;
    //1-255
    private int onealpha;

    private float[] pos;                // 当前点的实际位置
    private float[] tan;                // 当前点的tangent值,用于计算图片所需旋转的角度

    //loading的动效
    ValueAnimator loadingvalueAnimator;
    //loading 动画数值(用于控制动画状态,因为同一时间内只允许有一种状态出现,具体数值处理取决于当前状态)
    private float mAnimatorValue = 0;

    //loading 用于控制动画状态转换
    private Handler mAnimatorHandler;

    State mcurrentState = State.LODING;

    //loading状态，loading，成功，失败
    private enum State {
        LODING,
        SUCCES,
        ERROR,
    }

    //成功失败
    Paint textPaint;

    //成功的动效
    ValueAnimator successvalueAnimator;
    //失败的动效
    ValueAnimator errorvalueAnimator;


    private Type mcurrentType = Type.CIRCLE;

    //loading type
    private enum Type {
        ARC,//传统弧形转圈
        CIRCLE,//天女散花
        ROUND,//渐变的圆圈旋转

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
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(16);
        mPaint.setStyle(Paint.Style.STROKE);

        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStrokeCap(Paint.Cap.ROUND);
        circlePaint.setStrokeWidth(16);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLUE);
        textPaint.setStrokeCap(Paint.Cap.ROUND);
        textPaint.setStrokeWidth(6);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setTextSize(60);

        pos = new float[2];
        tan = new float[2];

        //计算透明度
        onealpha = 255 / roundCount;

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

    private void initListener() {
        // 创建0－1的一个过程,任何复杂的过程都可以采用归一化，然后在addUpdateListener回调里去做自己想要的变化
        loadingvalueAnimator = ValueAnimator.ofFloat(0, 1);
        // 设置过程的时间为2S
        loadingvalueAnimator.setDuration(2000);

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
                //动画结束，隐藏状态
                LoadingView.this.setVisibility(GONE);
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
                //动画结束，隐藏状态
                LoadingView.this.setVisibility(GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //移到屏幕中间
        canvas.translate(mwidth / 2, mheight / 2);
        //都添加背景
        canvas.drawColor(Color.parseColor("#33000000"));
        drawLoading(canvas);

    }

    private void drawLoading(Canvas canvas) {
        switch (mcurrentState) {
            case LODING:
                if (mcurrentType == Type.ARC) {
                    mPath = new Path();
                    RectF loadingrectF = new RectF(-radius, -radius, radius, radius);
                    mPath.addArc(loadingrectF, mAnimatorValue * 360, 240);
                    canvas.drawPath(mPath, mPaint);
                } else if (mcurrentType == Type.CIRCLE) {
                    mPath = new Path();
                    for (int i = 0; i < 10; i++) {
                        mPath.addCircle(mAnimatorValue * mwidth / 2 * (float) Math.cos(30 * i), mAnimatorValue * mwidth / 2 * (float) Math.sin(30 * i), 16, Path.Direction.CW);
                        mPath.addCircle(mAnimatorValue * mwidth / 3 * (float) Math.cos(30 * i), mAnimatorValue * mwidth / 3 * (float) Math.sin(30 * i), 16, Path.Direction.CW);
                        mPath.addCircle(mAnimatorValue * mwidth / 4 * (float) Math.cos(30 * i), mAnimatorValue * mwidth / 4 * (float) Math.sin(30 * i), 16, Path.Direction.CW);
                        mPath.addCircle(mAnimatorValue * mwidth / 5 * (float) Math.cos(30 * i), mAnimatorValue * mwidth / 5 * (float) Math.sin(30 * i), 16, Path.Direction.CW);
                        mPath.addCircle(mAnimatorValue * mwidth / 6 * (float) Math.cos(30 * i), mAnimatorValue * mwidth / 6 * (float) Math.sin(30 * i), 16, Path.Direction.CW);
                    }
                    canvas.drawPath(mPath, circlePaint);
                } else if (mcurrentType == Type.ROUND) {
                    Path path = new Path();
                    path.addCircle(0, 0, radius, Path.Direction.CW);           // 添加一个圆形
                    PathMeasure pathMeasure = new PathMeasure(path, false);
                    pathMeasure.getPosTan(pathMeasure.getLength() * mAnimatorValue, pos, tan);

//              mPath = new Path();
                    //使用 Math.atan2(tan[1], tan[0]) 将 tan 转化为角(单位为弧度)的时候要注意参数顺序。
                    float angle = (float) Math.atan2(tan[1], tan[0]);
                    for (int i = 0; i < roundCount; i++) {
                        //用path一次性画的，透明度不好设置
//              mPath.addCircle((float) (Math.cos(angle + i*0.4) * 100), (float) (Math.sin(angle+ i*0.4) * 100), 16, Path.Direction.CW);
                        circlePaint.setAlpha(onealpha * i);
                        canvas.drawCircle((float) (Math.cos(angle + i * 0.4) * radius), (float) (Math.sin(angle + i * 0.4) * radius), circleRadius, circlePaint);
                    }
//              canvas.drawPath(mPath, circlePaint);
                }

                break;
            case SUCCES:

                canvas.drawCircle(0, 0, radius, textPaint);

                canvas.drawLine(-radius / 2, 0, 0, radius / 2, textPaint);
                canvas.drawLine(0, radius / 2, radius / 2, -radius / 2, textPaint);

                break;
            case ERROR:
                canvas.drawCircle(0, 0, radius, textPaint);

                canvas.drawLine(radius / 2, radius / 2, -radius / 2, -radius / 2, textPaint);
                canvas.drawLine(radius / 2, -radius / 2, -radius / 2, radius / 2, textPaint);

                break;
            default:
                break;
        }
    }


    /**
     * @param layout 添加在那个夫布局里面
     *               然后执行loading动画
     */
    public void addPartentViewStartLoading(ViewGroup layout) {
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.addView(this, layoutParams);
        startLoading();
    }

    /**
     * 支持范型
     *
     * @param layout 添加在那个夫布局里面
     */
    public void addView(ViewGroup layout) {
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.addView(this, layoutParams);
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
            //如果是gone --->VISIBLE
            if (this.getVisibility() == GONE) {
                this.setVisibility(VISIBLE);
            }
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


}

