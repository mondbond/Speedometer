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
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class SpeedometerView extends View {

    private final float OUTER_CIRCLE_WIDTH_INDEX = 0.05f;
    private final float SCALE_RADIUS_INDEX = 0.75f;
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
    private PointF mScaleTextPoint;

    private Matrix mBitmapMatrix;

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
        // TODO: 22.05.17 implement check for errors also on setters
        mBackgroundColor = attr.getColor(R.styleable.SpeedometerView_backgroundColor, Color.WHITE);
        mSpeedIndicatorColor = attr.getColor(R.styleable.SpeedometerView_speedIndicatorColor, Color.BLACK);
        mBeforeArrowSectorColor = attr.getColor(R.styleable.SpeedometerView_beforeArrowSectorColor, Color.RED);
        mAfterArrowSectorColor = attr.getColor(R.styleable.SpeedometerView_afterArrowSectorColor, Color.BLUE);
        mBorderColor = attr.getColor(R.styleable.SpeedometerView_borderColor, Color.BLACK);
        mArrowColor = attr.getColor(R.styleable.SpeedometerView_arrowColor, Color.BLACK);

        mArrowHeight = (float) attr.getInteger(R.styleable.SpeedometerView_arrowHeight, 60)/100;
        if(mArrowHeight < 0){
            throw new IllegalArgumentException("Argument can not be negative or 0");
        }

        mInnerSectorRadius = ((float) attr.getInteger(R.styleable.SpeedometerView_innerSectorRadius, 30)) / 100;
        if(mInnerSectorRadius < 0){
            throw new IllegalArgumentException("Argument can not be negative or 0");
        }

        mOuterSectorRadius = ((float) (attr.getInteger(R.styleable.SpeedometerView_outerSectorRadius, 40))) / 100;
        if(mOuterSectorRadius < 0 || mOuterSectorRadius < mInnerSectorRadius){
            throw new IllegalArgumentException("Argument can not be negative or 0 and must be bigger" +
                    " then inner sector radius");
        }

        mMaxSpeed = attr.getInteger(R.styleable.SpeedometerView_maxSpeed, 90);
        if(mMaxSpeed < 60 || mMaxSpeed%10 != 0){
            throw new IllegalArgumentException("Argument can not be less then 60 and must be" +
                    " multiple to 10");
        }

        mSpeedAccelerationIndex = attr.getFloat(R.styleable.SpeedometerView_speedAccelerationIndex, 50) / 10;
        mSpeedOnNeutralIndex = attr.getFloat(R.styleable.SpeedometerView_speedOnNeutralIndex, 2) / 10;
        mSpeedFuelConsumptionIndex = attr.getFloat(R.styleable.SpeedometerView_speedFuelConsumptionIndex, 5) / 10;

        attr.recycle();

        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mPaint = new Paint();
        mFuelPaint = new Paint();
        mTextRect = new Rect();
        mScaleTextPoint = new PointF();
        mBitmapMatrix = new Matrix();
        mArrowPath = new Path();

        mAlphaAnimator = ObjectAnimator.ofFloat(255, 0);
        mAlphaAnimator.setDuration(400);
        mAlphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAlphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAlphaAnimator.setRepeatMode(ValueAnimator.REVERSE);

        mFuelIco = BitmapFactory.decodeResource(getResources(), R.drawable.fuel_ico);

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
        mPaint.setColor(Color.BLACK);
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
            calculateCirclePoint(angle, radius * SCALE_RADIUS_INDEX, mScaleTextPoint);
            drawText(canvas, String.valueOf(10 * i + 10), mScaleTextPoint.x, mScaleTextPoint.y, mPaint);
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

        mFuelPaint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(mMatrixFilter)));
        mFuelPaint.setStyle(Paint.Style.FILL);

        canvas.drawBitmap(mFuelIco, null, mFuelIcoRec, mFuelPaint);

        mFuelLevel = new Rect(
                centerX,
                centerY - radius + (int) (radius * 0.44f),
                centerX + (int ) (radius * 0.3f * (mCurrentFuelLevel / mMaxFuelLevel)),
                centerY - radius + (int) (radius * 0.47f));

        mAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mFuelPaint.setAlpha(((int) (float) valueAnimator.getAnimatedValue()));
            }
        });

        if(mCurrentFuelLevel/mMaxFuelLevel < (float) 1/3 ){
            if(!mIsAlphaAnimating){
                mIsAlphaAnimating = true;
                mAlphaAnimator.start();
            }
        }else{
            mIsAlphaAnimating = false;
            mAlphaAnimator.cancel();
        }

        canvas.drawRect(mFuelLevel, mFuelPaint);
    }

    private void drawArrow(Canvas canvas){

        mPaint.setColor(mArrowColor);
        canvas.save();
        canvas.rotate( -90 + ((float) 180 / mMaxSpeed) * mCurrentSpeed, centerX, centerY);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);

        mArrowPath.moveTo(centerX - radius * ARROW_WIGHT_INDEX, centerY);
        mArrowPath.lineTo(centerX - radius * ARROW_TOP_WIGHT_INDEX, centerY - radius * mArrowHeight);
        mArrowPath.lineTo(centerX + radius * ARROW_TOP_WIGHT_INDEX, centerY - radius * mArrowHeight);
        mArrowPath.lineTo(centerX + radius * ARROW_WIGHT_INDEX, centerY);
        mArrowPath.close();

        canvas.drawPath(mArrowPath, mPaint);
        canvas.restore();
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

        listener.onSpeedChange(mCurrentSpeed);

        return mCurrentSpeed;
    }

    private void start() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 100);

                if (stop) {
                    changeSpeed(mCurrentSpeed -= calculateAcceleration(ACCELERATION_INDEX));
                } else if (go && mCurrentFuelLevel > 0) {
                    changeSpeed(mCurrentSpeed += calculateAcceleration(mSpeedAccelerationIndex));
                    changeFuelLevel();
                } else {
                    changeSpeed(mCurrentSpeed -= mSpeedOnNeutralIndex);
                }

                invalidate();
            }
        }, 3000);
    }

    private void drawText(Canvas canvas, String text, float x, float y, Paint paint){
        paint.getTextBounds(text, 0, text.length(), mTextRect);
        if(Float.parseFloat(text) >= 100){
            canvas.drawText(text, x - mTextRect.width() / 2f - 10, y + mTextRect.height() / 2f, paint);
        }else {
            canvas.drawText(text, x - mTextRect.width() / 2f, y + mTextRect.height() / 2f, paint);
        }
    }

    private void changeFuelLevel(){
        if(mCurrentFuelLevel != 0 && mCurrentFuelLevel <= MAX_FUEL_LEVEL){
            mCurrentFuelLevel -= mSpeedFuelConsumptionIndex;
        }else if(mCurrentFuelLevel <= 0 && go){
            go = false;
        }
    }

    private float calculateAcceleration(float baseAcceleration){
        return (1 - mCurrentSpeed/mMaxSpeed)*baseAcceleration;
    }

    private void calculateCirclePoint(float angle, float radius, PointF point) {
        point.set((float) (centerX - radius * Math.cos(angle / 180 * Math.PI)),
                (float) (centerY - radius * Math.sin(angle / 180 * Math.PI)));
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

    public float getInnerSectorRadius() {
        return mInnerSectorRadius;
    }

    public void setInnerSectorRadius(int mInnerSectorRadius) {
        this.mInnerSectorRadius = mInnerSectorRadius;
    }

    public float getOuterSectorRadius() {
        return mOuterSectorRadius;
    }

    public void setOuterSectorRadius(int mOuterSectorRadius) {
        this.mOuterSectorRadius = mOuterSectorRadius;
    }

    public int getMaxSpeed() {
        return mMaxSpeed;
    }

    public void setMaxSpeed(int mMaxSpeed) {
        this.mMaxSpeed = mMaxSpeed;
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

    public void setCurrentFuelLevel(float mCurrentFuelLevel) {
        this.mCurrentFuelLevel = mCurrentFuelLevel;
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

    public void setFuelMaxLevel(){
        mCurrentFuelLevel = MAX_FUEL_LEVEL;
    }


    interface SpeedChangeListener {

        void onSpeedChange(float newSpeedValue);
    }
}
