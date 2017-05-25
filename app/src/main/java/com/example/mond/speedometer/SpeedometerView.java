package com.example.mond.speedometer;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class SpeedometerView extends View {

    private final int FRAME_RATE_DELAY_IN_MS = 30;

    private boolean mIsInvalidation;

    private final float OUTER_CIRCLE_WIDTH_INDEX = 0.05f;
    private final float SCALE_RADIUS_INDEX = 0.73f;
    private final float SCALES_WIDTH_INDEX = 0.1f;
    private final float BORDER_HEIGHT_INDEX = 0.04F;
    private final float ARROW_CIRCLE_INDEX = 0.14f;
    private final float ARROW_WIGHT_INDEX = 0.1f;
    private final float ARROW_TOP_WIGHT_INDEX = 0.04f;
    private final float TEXT_SIZE_INDEX = 0.15f;

    private final float MAX_FUEL_LEVEL = 100;
    private final float ACCELERATION_INDEX = 4;

    int paddingLeft;
    int paddingTop;
    int paddingRight;
    int paddingBottom;

    int contentWidth;
    int contentHeight;

    int centerX;
    int centerY;
    int radius;

    private int mBackgroundColor;
    private int mSpeedIndicatorColor;
    private int mBeforeArrowSectorColor;
    private int mAfterArrowSectorColor;

    private int mSectorRadius;
    private int mBorderColor;
    private int mArrowColor;
    private float mArrowHeight;
    private float mInnerSectorRadius;
    private float mOuterSectorRadius;
    private int mMaxSpeed;

    private float mSpeedAccelerationIndex;
    private float mSpeedOnNeutralIndex;
    private float mSpeedFuelConsumptionIndex;

    private RectF mBackgroundCircleRec;
    private Rect mTextRect;
    private RectF mInnerCircleRec;

    private float mInnerCircleWidth;

    private RectF mScaleCircleRec;
    private RectF mFuelIcoRec;
    private Rect mFuelLevel;
    private Path mScalePath;
    private Path mArrowPath;
    private Path mTextPath = new Path();

    private Matrix mBitmapMatrix;
    private Matrix mRotateMatrix;

    private ValueAnimator mAlphaAnimator;
    private boolean mIsAlphaAnimating;

    private float[] mMatrixFilter;

    private Paint mPaint;
    private Paint mFuelPaint;
    private TextPaint mTextPaint;

    private boolean stop;
    private boolean go;

    private float mCurrentSpeed;
    private float mCurrentFuelLevel = 100;
    private float mMaxFuelLevel = 100;

    private Bitmap mFuelIco;

    private SpeedometerView.SpeedChangeListener listener;

    public SpeedometerView(Context context) {
        super(context);
        init(null, 0);
    }

    public SpeedometerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SpeedometerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        final TypedArray attr = getContext().obtainStyledAttributes(
                attrs, R.styleable.SpeedometerView, defStyle, 0);
        mBackgroundColor = attr.getColor(R.styleable.SpeedometerView_backgroundColor, Color.WHITE);
        mSpeedIndicatorColor = attr.getColor(R.styleable.SpeedometerView_speedIndicatorColor, Color.BLACK);
        mBeforeArrowSectorColor = attr.getColor(R.styleable.SpeedometerView_beforeArrowSectorColor, Color.RED);
        mAfterArrowSectorColor = attr.getColor(R.styleable.SpeedometerView_afterArrowSectorColor, Color.BLUE);
        mBorderColor = attr.getColor(R.styleable.SpeedometerView_borderColor, Color.BLACK);
        mArrowColor = attr.getColor(R.styleable.SpeedometerView_arrowColor, Color.BLACK);

        setArrowHeight((float) attr.getInteger(R.styleable.SpeedometerView_arrowHeight, 60)/100);
        setInnerSectorRadius(((float) attr.getInteger(R.styleable.SpeedometerView_innerSectorRadius, 30)) / 100);
        setOuterSectorRadius(((float) (attr.getInteger(R.styleable.SpeedometerView_outerSectorRadius, 40))) / 100);
        setMaxSpeed(attr.getInteger(R.styleable.SpeedometerView_maxSpeed, 90));
        setSpeedAccelerationIndex(attr.getFloat(R.styleable.SpeedometerView_speedAccelerationIndex, 50) * ((float) FRAME_RATE_DELAY_IN_MS / 100));
        setSpeedOnNeutralIndex(attr.getFloat(R.styleable.SpeedometerView_speedOnNeutralIndex, 2) * ((float) FRAME_RATE_DELAY_IN_MS / 100));
        setSpeedFuelConsumptionIndex(attr.getFloat(R.styleable.SpeedometerView_speedFuelConsumptionIndex, 5) * ((float) FRAME_RATE_DELAY_IN_MS / 100));
        attr.recycle();

        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mPaint = new Paint();
        mFuelPaint = new Paint();
        mTextRect = new Rect();
        mBitmapMatrix = new Matrix();
        mRotateMatrix = new Matrix();
        mArrowPath = new Path();

        mAlphaAnimator = ObjectAnimator.ofFloat(255, 0);
        mAlphaAnimator.setDuration(400);
        mAlphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAlphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAlphaAnimator.setRepeatMode(ValueAnimator.REVERSE);

        mFuelIco = BitmapFactory.decodeResource(getResources(), R.drawable.fuel_ico);

        mAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mFuelPaint.setAlpha(((int) (float) valueAnimator.getAnimatedValue()));
            }
        });

        start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desiredWidth = 400;
        int desiredHeight = 200;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        paddingLeft = getPaddingLeft();
        paddingTop = getPaddingTop();
        paddingRight = getPaddingRight();
        paddingBottom = getPaddingBottom();

        contentWidth = getWidth() - paddingLeft - paddingRight;
        contentHeight = getHeight() - paddingTop - paddingBottom;

        centerX = paddingLeft + contentWidth / 2;
        centerY = paddingTop + contentHeight;

        radius = Math.min(contentWidth / 2, contentHeight);

        mInnerCircleWidth = radius * (mOuterSectorRadius - mInnerSectorRadius);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBackgroundCircle(canvas);

        drawArrowCircle(canvas);

        drawBorderCircle(canvas);

        drawInnerCircle(canvas);

        drawFuelLevel(canvas);

        drawScale(canvas);

        drawSpeedIndicator(canvas);

        drawArrow(canvas);
    }

    private void drawBackgroundCircle(Canvas canvas){
        mPaint.setColor(mBackgroundColor);
        mPaint.setStyle(Paint.Style.FILL);

        mBackgroundCircleRec = new RectF(
                centerX - radius + (radius * BORDER_HEIGHT_INDEX), centerY - radius + (radius * BORDER_HEIGHT_INDEX),
                centerX + radius - (radius * BORDER_HEIGHT_INDEX), centerY + radius - (radius * BORDER_HEIGHT_INDEX));

        canvas.drawArc(mBackgroundCircleRec, -180, 180, false, mPaint);
    }

    private void drawBorderCircle(Canvas canvas){
        mPaint.setColor(mBorderColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(radius * BORDER_HEIGHT_INDEX);
        canvas.drawArc(mBackgroundCircleRec, -180, 180, false, mPaint);
    }

    private void drawArrowCircle(Canvas canvas){
        mPaint.setColor(mArrowColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, radius * ARROW_CIRCLE_INDEX, mPaint);
    }

    private void drawScale(Canvas canvas){
        mPaint.setColor(mBorderColor);

        mScaleCircleRec = new RectF(
                centerX - radius + (radius * BORDER_HEIGHT_INDEX) + radius * SCALES_WIDTH_INDEX / 2,
                centerY - radius + (radius * BORDER_HEIGHT_INDEX) + radius * SCALES_WIDTH_INDEX / 2,
                centerX + radius - (radius * BORDER_HEIGHT_INDEX) - radius * SCALES_WIDTH_INDEX / 2,
                centerY + radius - (radius * BORDER_HEIGHT_INDEX) - radius * SCALES_WIDTH_INDEX / 2);

        mPaint.setStrokeWidth(radius * SCALES_WIDTH_INDEX);

        mScalePath = new Path();
        int scaleCount = mMaxSpeed/10;
        for(int i = mMaxSpeed, c = 0; i > 0; i -= 10, c++){
            mScalePath.addArc(mScaleCircleRec,-170 + ((180/scaleCount)*c),1f);
        }

        canvas.drawPath(mScalePath, mPaint);
    }

    private void drawSpeedIndicator(Canvas canvas){
        mPaint.setColor(mSpeedIndicatorColor);
        mPaint.setStrokeWidth(2);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextSize(radius * TEXT_SIZE_INDEX);
        canvas.save(Canvas.MATRIX_SAVE_FLAG);

        mPaint.setStrokeWidth(radius * OUTER_CIRCLE_WIDTH_INDEX);
        float angle;
        float step = 180 / (mMaxSpeed / 10);
        for(int i = 0; i < mMaxSpeed / 10; ++i){

            angle = 10 + i * step;
            drawDial(angle, canvas, String.valueOf(10 * i + 10), mPaint);
        }
    }

    private void drawFuelLevel(Canvas canvas){

        mFuelIcoRec = new RectF(
                centerX - radius + radius * 0.8f,
                centerY - radius + radius * 0.35f,
                centerX,
                centerY - radius + radius * 0.55f);

        mBitmapMatrix.mapRect(mFuelIcoRec);

        mMatrixFilter = new float[]{
                0, 0, 0, 0, (1 - mCurrentFuelLevel / 100) * 255,
                0, 0, 0, 0, mCurrentFuelLevel / 100 * 255,
                0, 0, 0, 0, 0,
                0, 0, 0, 1, 0
        };

        canvas.drawBitmap(mFuelIco, null, mFuelIcoRec, mFuelPaint);

        mFuelPaint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(mMatrixFilter)));
        mFuelPaint.setStyle(Paint.Style.FILL);

        mFuelLevel = new Rect(
                centerX,
                centerY - radius + (int) (radius * 0.44f),
                centerX + (int ) (radius * 0.3f * (mCurrentFuelLevel / mMaxFuelLevel)),
                centerY - radius + (int) (radius * 0.47f));

        if(mCurrentFuelLevel/mMaxFuelLevel < (float) 1/3){
            if(!mIsAlphaAnimating){
                mIsAlphaAnimating = true;
                mAlphaAnimator.start();
            }
        }else{
            mIsAlphaAnimating = false;
            mAlphaAnimator.cancel();
            mFuelPaint.setColor(Color.GREEN);
        }

        canvas.drawRect(mFuelLevel, mFuelPaint);
    }

    private void drawArrow(Canvas canvas){

        mPaint.setColor(mArrowColor);
        mRotateMatrix.reset();
        mRotateMatrix.setRotate(-90 + ((float) 180 / mMaxSpeed) * mCurrentSpeed, centerX, centerY);
        mPaint.setStyle(Paint.Style.FILL);

        mArrowPath.reset();
        mArrowPath.moveTo(centerX - radius * ARROW_WIGHT_INDEX, centerY);
        mArrowPath.lineTo(centerX - radius * ARROW_TOP_WIGHT_INDEX, centerY - radius * mArrowHeight);
        mArrowPath.lineTo(centerX + radius * ARROW_TOP_WIGHT_INDEX, centerY - radius * mArrowHeight);
        mArrowPath.lineTo(centerX + radius * ARROW_WIGHT_INDEX, centerY);
        mArrowPath.close();
        mArrowPath.transform(mRotateMatrix);

        canvas.drawPath(mArrowPath, mPaint);
    }

    private void drawInnerCircle(Canvas canvas){
        mPaint.setColor(mAfterArrowSectorColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mInnerCircleWidth);

        mInnerCircleRec = new RectF(
                centerX - radius * mOuterSectorRadius + mInnerCircleWidth,
                centerY - radius * mOuterSectorRadius + mInnerCircleWidth,
                centerX + radius * mOuterSectorRadius - mInnerCircleWidth,
                centerY + radius * mOuterSectorRadius - mInnerCircleWidth);

        canvas.drawArc(mInnerCircleRec, -180, 180, false, mPaint);

        drawInnerSpeedCircle(canvas);
    }

    private void drawInnerSpeedCircle(Canvas canvas){
        mPaint.setColor(mBeforeArrowSectorColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mInnerCircleWidth);

        canvas.drawArc(mInnerCircleRec, - 180, ((float)180 / mMaxSpeed) * mCurrentSpeed, false, mPaint);
    }

    private float changeSpeed(float newSpeedValue){
        if(newSpeedValue > mMaxSpeed){
            setCurrentSpeed(mMaxSpeed);
        }else if(newSpeedValue < 0){
            setCurrentSpeed(0);
        }else {
            setCurrentSpeed(newSpeedValue);
        }

        if(listener != null) {
            listener.onSpeedChange(mCurrentSpeed);
        }

        return mCurrentSpeed;
    }

    private void start() {
        mIsInvalidation = true;
        final Handler handler = new Handler();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mCurrentSpeed > 0 || go) {
                    handler.postDelayed(this, FRAME_RATE_DELAY_IN_MS);
                    }else {
                        mIsInvalidation = false;
                    }

                    if (stop) {
                        changeSpeed(mCurrentSpeed -= calculateAcceleration(ACCELERATION_INDEX));
                    } else if (go && mCurrentFuelLevel > 0) {
                        changeSpeed(mCurrentSpeed += calculateAcceleration(mSpeedAccelerationIndex));
                        changeFuelLevel();
                    } else if(mCurrentFuelLevel <= 0 && go){
                        go = false;
                    } else {
                        changeSpeed(mCurrentSpeed -= mSpeedOnNeutralIndex);
                    }

                    invalidate();
                }
            }, 0);
    }

    private void drawDial(float angle, Canvas canvas, String text, Paint paint){

        mTextPath.reset();
        mTextPath.moveTo(centerX, centerY - radius * SCALE_RADIUS_INDEX);
        mTextPath.lineTo(centerX + 100, centerY - radius * SCALE_RADIUS_INDEX);
        mTextPath.close();

        paint.getTextBounds(text, 0, text.length(), mTextRect);

        mRotateMatrix.reset();
        mRotateMatrix.setRotate(90 - angle, centerX, centerY - radius);
        mTextPath.transform(mRotateMatrix);

        mRotateMatrix.reset();
        mRotateMatrix.setRotate(- 90 + angle, centerX, centerY - radius * (1 - SCALE_RADIUS_INDEX));

        mTextPath.transform(mRotateMatrix);
        
        mRotateMatrix.reset();
        mRotateMatrix.setTranslate(-mTextRect.width() / 2f, 0);
        mTextPath.transform(mRotateMatrix);

        canvas.drawTextOnPath(text, mTextPath, 0, 0, paint);
    }

    private void changeFuelLevel(){
       if(mCurrentFuelLevel != 0 && mCurrentFuelLevel <= MAX_FUEL_LEVEL){
            mCurrentFuelLevel -= mSpeedFuelConsumptionIndex;
        }
    }

    private float calculateAcceleration(float baseAcceleration){
        return (1 - mCurrentSpeed/mMaxSpeed)*baseAcceleration;
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setBackgroundColor(int mBackgroundColor) {
        this.mBackgroundColor = mBackgroundColor;
    }

    public int getSpeedIndicatorColor() {
        return mSpeedIndicatorColor;
    }

    public void setSpeedIndicatorColor(int mSpeedIndicatorColor) {
        this.mSpeedIndicatorColor = mSpeedIndicatorColor;
    }

    public int getBeforeArrowSectorColor() {
        return mBeforeArrowSectorColor;
    }

    public void setBeforeArrowSectorColor(int mBeforeArrowSectorColor) {
        this.mBeforeArrowSectorColor = mBeforeArrowSectorColor;
    }

    public int getAfterArrowSectorColor() {
        return mAfterArrowSectorColor;
    }

    public void setAfterArrowSectorColor(int mAfterArrowSectorColor) {
        this.mAfterArrowSectorColor = mAfterArrowSectorColor;
    }

    public int getSectorRadius() {
        return mSectorRadius;
    }

    public void setSectorRadius(int mSectorRadius) {
        this.mSectorRadius = mSectorRadius;
    }

    public int getBorderColor() {
        return mBorderColor;
    }

    public void setBorderColor(int mBorderColor) {
        this.mBorderColor = mBorderColor;
    }

    public int getArrowColor() {
        return mArrowColor;
    }

    public void setArrowColor(int mArrowColor) {
        this.mArrowColor = mArrowColor;
    }

    public float getArrowHeight() {
        return mArrowHeight;
    }

    public void setArrowHeight(float mArrowHeight) {
        if(mArrowHeight < 0){
            throw new IllegalArgumentException("Argument can not be negative or 0");
        }else {
            this.mArrowHeight = mArrowHeight;
        }
    }

    public float getInnerSectorRadius() {
        return mInnerSectorRadius;
    }

    public void setInnerSectorRadius(float mInnerSectorRadius) {
        if(mInnerSectorRadius < 0){
            throw new IllegalArgumentException("Argument can not be negative or 0");
        }else {
            this.mInnerSectorRadius = mInnerSectorRadius;
        }
    }

    public float getOuterSectorRadius() {
        return mOuterSectorRadius;
    }

    public void setOuterSectorRadius(float mOuterSectorRadius) {
        if(mOuterSectorRadius < 0 || mOuterSectorRadius < mInnerSectorRadius){
            throw new IllegalArgumentException("Argument can not be negative or 0 and must be bigger" +
                    " then inner sector radius");
        }else {
            this.mOuterSectorRadius = mOuterSectorRadius;
        }
    }

    public int getMaxSpeed() {
        return mMaxSpeed;
    }

    public void setMaxSpeed(int mMaxSpeed) {
        if(mMaxSpeed < 60 || mMaxSpeed%10 != 0){
            throw new IllegalArgumentException("Argument can not be less then 60 and must be" +
                    " multiple to 10");
        }else {
            this.mMaxSpeed = mMaxSpeed;
        }
    }

    public Paint getPaint() {
        return mPaint;
    }

    public void setPaint(Paint mPaint) {
        this.mPaint = mPaint;
    }

    public TextPaint getTextPaint() {
        return mTextPaint;
    }

    public void setTextPaint(TextPaint mTextPaint) {
        this.mTextPaint = mTextPaint;
    }

    public float getCurrentSpeed() {
        return mCurrentSpeed;
    }

    public void setCurrentSpeed(float mCurrentSpeed) {
        this.mCurrentSpeed = mCurrentSpeed;
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public boolean isGo() {
        return go;
    }

    public void setGo(boolean go) {

        if(!mIsInvalidation) {
            start();
        }

        this.go = go;
    }

    public SpeedChangeListener getListener() {
        return listener;
    }

    public void setListener(SpeedChangeListener listener) {
        this.listener = listener;
    }

    public float getCurrentFuelLevel() {
        return mCurrentFuelLevel;
    }

    public void setCurrentFuelLevel(float currentFuelLevel) {
        if( currentFuelLevel < 0 || currentFuelLevel > 100){
            throw new IllegalArgumentException("Argument can not be negative or more then 100");
        }else {
            this.mCurrentFuelLevel = currentFuelLevel;
        }
    }

    public void refillFuelLevel(){
        mCurrentFuelLevel = mMaxFuelLevel;
    }

    public float getMaxFuelLevel() {
        return mMaxFuelLevel;
    }

    public void setMaxFuelLevel(float mMaxFuelLevel) {
        this.mMaxFuelLevel = mMaxFuelLevel;
    }

    public void setCurrentFuelLevel(int newCurrentFuelLevel){
        mCurrentFuelLevel = newCurrentFuelLevel;
    }

    public float getSpeedAccelerationIndex() {
        return mSpeedAccelerationIndex;
    }

    public void setSpeedAccelerationIndex(float mSpeedAccelerationIndex) {
        if(mSpeedAccelerationIndex < 0){
            throw new IllegalArgumentException("Argument can not be negative");
        }else {
            this.mSpeedAccelerationIndex = mSpeedAccelerationIndex;
        }
    }

    public float getSpeedOnNeutralIndex() {
        return mSpeedOnNeutralIndex;
    }

    public void setSpeedOnNeutralIndex(float mSpeedOnNeutralIndex) {
        if(mSpeedAccelerationIndex < 0){
            throw new IllegalArgumentException("Argument can not be negative");
        }else {
            this.mSpeedOnNeutralIndex = mSpeedOnNeutralIndex;
        }
    }

    public float getSpeedFuelConsumptionIndex() {
        return mSpeedFuelConsumptionIndex;
    }

    public void setSpeedFuelConsumptionIndex(float mSpeedFuelConsumptionIndex) {
        if(mSpeedFuelConsumptionIndex < 0){
            throw new IllegalArgumentException("Argument can not be negative");
        } else {
            this.mSpeedFuelConsumptionIndex = mSpeedFuelConsumptionIndex;
        }
    }

    public void setFuelMaxLevel(){
        mCurrentFuelLevel = MAX_FUEL_LEVEL;
    }


    interface SpeedChangeListener {

        void onSpeedChange(float newSpeedValue);
    }
}
