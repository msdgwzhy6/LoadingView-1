# LoadingView

自定义view————loading框

写自定义view，怎么能没有自己写的loading框了，今天给大家送上一个loading框，前期losing框是很简单的效果，三个状态：loading、success、error，不逼逼了，先上给糙一点的效果GIF，回头继续完善。

![效果图](https://github.com/madreain/madreain.github.io/blob/master/images/loading.gif)

糙一点的loading框效果看完了，接下来说一下实现过程，采用WindowManager将losing框的view置于显示view的上层，起初上来是loading转圈状态，根据加载返回的结构设置成功或者失败。

### loading状态

ValueAnimator实现AnimatorUpdateListener、AnimatorListener监听方法，AnimatorUpdateListener中接受valueAnimator.getAnimatedValue();(不断回调的在0-1这个范围内，经过插值器插值之后的返回值),然后去画圈圈

这里画的是一个通过改变不同起始角度画给一个240度的圆弧，以此达到loading效果

```
    mPath = new Path();
    loadingrectF = new RectF(-100, -100, 100, 100);
    mPath.addArc(loadingrectF, mAnimatorValue * 360, 240);
    canvas.drawPath(mPath, mPaint);
    canvas.drawColor(Color.parseColor("#33000000"));    
    
```
mAnimatorValue值的变化是AnimatorUpdateListener函数里面回调的值，然后通知重绘

```

loadingvalueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                // 不断回调的在0-1这个范围内，经过插值器插值之后的返回值
                mAnimatorValue = (float) valueAnimator.getAnimatedValue();
                 //重绘
                invalidate();

            }
        });

```

为了保证转了一圈过后继续转，这里引进Handler，在AnimatorListener的结束方法中进行通知动画继续

```
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
        
        
```

附上Handler相关代码

```

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
        
```

到此为止，loading就能一直转了，接下来就是success、error两个状态了的讲解了

###  success状态

成功状态时，loading消失，显示成功的相关提醒（原谅我是一个程序员，做的loading不好看，准备后期功能完善后，会把loading、success、error状态的显示弄的像UI设计的一样），废话不多说了，该上菜了

```

  /**
     * 成功
     */
    public void setSuccess() {
        loadingvalueAnimator.removeAllListeners();
        errorvalueAnimator.removeAllListeners();
        addSuccesLoadingListener();
        successvalueAnimator.start();
    }

```

移除掉其他ValueAnimator对应的监听方法，为success加上ValueAnimator的监听方法，这里就是显示一下success状态的文案，保持500ms，然后显示（这一块可以改成其他动画酷炫效果）

```

successvalueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                //动画开始，去绘制success的文案
                mcurrentState = State.SUCCES;
                invalidate();
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //将当前绘制的view设置宽高都为零，也可以采取移除的方法
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
        
```

### error状态

和success状态同理，只是文案不一样

### 代码中使用

添加到activity开始loading动画，待结果返回时然后设置相关状态显示500ms，然后显示消失，接下来贴出模拟的loading--->success--->dismiss的一个过程

```
        LoadingView loadingview = new LoadingView(this);

        Handler mSuccesHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (succes <= 50) {
                    succes += 1;
                    mSuccesHandler.sendEmptyMessageDelayed(0, 100);
                } else {
                    loadingview.setSuccess();
                }
            }
        };

        loadingview.addViewStartLoading(loadingview, this);
        mSuccesHandler.sendEmptyMessageDelayed(0, 100);


```

[个人博客](https://madreain.github.io/)
