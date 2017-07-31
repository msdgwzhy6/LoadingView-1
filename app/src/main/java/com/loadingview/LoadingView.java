package com.loadingview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


/**
 * Created by wujun on 2017/7/31.
 *
 * @author madreain
 * @desc loading框  目前支持三种状态及三种样式
 */

public class LoadingView extends View {
    //屏幕的宽高
    int mwidth;
    int mheight;
    int backgroudColor;

    //主
    Paint mPaint;
    //属性
    int mpaintColor;
    float mpaintStrokeWidth;

    Path mPath;
    //转圈的半径
    float radius = 100;
    //画圆圈的半径
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

    //成功失败的画笔
    Paint textPaint;
    int textPaintColor;
    float textPaintStrokeWidth;
    float textPaintTextSize;

    //成功的动效
    ValueAnimator successvalueAnimator;
    //失败的动效
    ValueAnimator errorvalueAnimator;


    //loading的类型
    private Type mcurrentType = Type.ROUND;

    //loading type
    public enum Type {
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
        initTypedArray(context, attrs);
        initPaint();
        initHandler();
        initListener();
    }

    private void initTypedArray(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingView);
        backgroudColor=typedArray.getColor(R.styleable.LoadingView_backgroudColor,Color.parseColor("#66000000"));
        mpaintColor = typedArray.getColor(R.styleable.LoadingView_mpaintColor, Color.BLUE);
        mpaintStrokeWidth = typedArray.getFloat(R.styleable.LoadingView_mpaintStrokeWidth, 16);
        textPaintColor = typedArray.getColor(R.styleable.LoadingView_textPaintColor, Color.BLUE);
        textPaintStrokeWidth = typedArray.getFloat(R.styleable.LoadingView_textPaintStrokeWidth, 6);
        textPaintTextSize = typedArray.getFloat(R.styleable.LoadingView_textPaintTextSize, 60);
        radius = typedArray.getFloat(R.styleable.LoadingView_radius, 100);
        circleRadius = typedArray.getFloat(R.styleable.LoadingView_circleRadius, 16);
        roundCount = typedArray.getInteger(R.styleable.LoadingView_roundCount, 10);
        int type = typedArray.getInt(R.styleable.LoadingView_Type, 0);
        if (type == 0) {
            mcurrentType = Type.ARC;
        } else if (type == 1) {
            mcurrentType = Type.CIRCLE;
        } else if (type == 2) {
            mcurrentType = Type.ROUND;
        }
    }


    public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTypedArray(context, attrs);
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
        canvas.drawColor(backgroudColor);
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
                    mPaint.setStyle(Paint.Style.FILL);
                    mPath = new Path();
                    for (int i = 0; i < 10; i++) {
                        mPath.addCircle(mAnimatorValue * mwidth / 2 * (float) Math.cos(30 * i), mAnimatorValue * mwidth / 2 * (float) Math.sin(30 * i), 16, Path.Direction.CW);
                        mPath.addCircle(mAnimatorValue * mwidth / 3 * (float) Math.cos(30 * i), mAnimatorValue * mwidth / 3 * (float) Math.sin(30 * i), 16, Path.Direction.CW);
                        mPath.addCircle(mAnimatorValue * mwidth / 4 * (float) Math.cos(30 * i), mAnimatorValue * mwidth / 4 * (float) Math.sin(30 * i), 16, Path.Direction.CW);
                        mPath.addCircle(mAnimatorValue * mwidth / 5 * (float) Math.cos(30 * i), mAnimatorValue * mwidth / 5 * (float) Math.sin(30 * i), 16, Path.Direction.CW);
                        mPath.addCircle(mAnimatorValue * mwidth / 6 * (float) Math.cos(30 * i), mAnimatorValue * mwidth / 6 * (float) Math.sin(30 * i), 16, Path.Direction.CW);
                    }
                    canvas.drawPath(mPath, mPaint);
                } else if (mcurrentType == Type.ROUND) {
                    mPaint.setStyle(Paint.Style.FILL);
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
                        mPaint.setAlpha(onealpha * i);
                        canvas.drawCircle((float) (Math.cos(angle + i * 0.4) * radius), (float) (Math.sin(angle + i * 0.4) * radius), circleRadius, mPaint);
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
     * 添加到activity的上层并执行动画
     * @param activity
     *
     */
    public void addPartentViewStartLoading(Activity activity) {
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //Activity中View布局的最祖宗布局,是一个FrameLayout,叫做DecorView,通过getWindow().getDecorView()可以获取到
        FrameLayout view = (FrameLayout) activity.getWindow().getDecorView();
        view.addView(this,layoutParams);
        startLoading();
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

    /**
     * 整个事件的消费来保证loading状态不可操作
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    /**
     * 设置loading的背景颜色
     * @param backgroudColor
     */
    public void setBackgroudColor(int backgroudColor) {
        this.backgroudColor = backgroudColor;
    }

    /**
     *
     * @param mpaintColor
     */
    public void setMpaintColor(int mpaintColor) {
        this.mpaintColor = mpaintColor;
    }

    /**
     *
     * @param mpaintStrokeWidth
     */
    public void setMpaintStrokeWidth(float mpaintStrokeWidth) {
        this.mpaintStrokeWidth = mpaintStrokeWidth;
    }

    /**
     *
     * @param textPaintColor
     */
    public void setTextPaintColor(int textPaintColor) {
        this.textPaintColor = textPaintColor;
    }

    /**
     *
     * @param textPaintStrokeWidth
     */
    public void setTextPaintStrokeWidth(float textPaintStrokeWidth) {
        this.textPaintStrokeWidth = textPaintStrokeWidth;
    }

    /**
     *
     * @param textPaintTextSize
     */
    public void setTextPaintTextSize(float textPaintTextSize) {
        this.textPaintTextSize = textPaintTextSize;
    }

    /**
     *
     * @param radius
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }

    /**
     *
     * @param circleRadius
     */
    public void setCircleRadius(float circleRadius) {
        this.circleRadius = circleRadius;
    }

    /**
     *
     * @param roundCount
     */
    public void setRoundCount(int roundCount) {
        this.roundCount = roundCount;
    }

    /**
     *
     * @param mcurrentType
     */
    public void setType(Type mcurrentType){
        this.mcurrentType=mcurrentType;
    }

}

