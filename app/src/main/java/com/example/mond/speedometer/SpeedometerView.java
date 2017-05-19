package com.example.mond.speedometer;

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

import android.util.Log;
import android.view.View;

public class SpeedometerView extends View {

    private final float INNER_CIRCLE_WIDTH = 0.15f;
    private final float INNER_CIRCLE_RADIUS = 0.4f;
    private final float OUTER_CIRCLE_WIDTH = 0.05f;

    private final float SCALE_RADIUS_INDEX = 0.8f;
    private final float BORDER_HEIGHT = 0.04F;
    private final float SCALES_WIDTH = 0.1f;

    private final float ARROW_CIRCLE_INDEX = 0.14f;
    private final float ARROW_WEIGHT_INDEX = 0.08f;
    private final float ARROW_WEIGHT2_INDEX = 0.4f;
    private final float ARROW_HEIGHT_INDEX = 0.3f;

    private final float TEXT_SIZE = 0.15f;

    int paddingLeft;
    int paddingTop;
    int paddingRight;
    int paddingBottom;

    int contentWidth;
    int contentHeight;

    int radius = getHeight() - paddingTop - paddingBottom + paddingLeft + paddingRight;

    private int mBackgroundColor = Color.GRAY;
    private int mSpeedIndicatorColor;
    private int mBeforeArrowSectorColor;
    private int mAfterArrowSectorColor;

    private int mSectorRadius;
    private int mBorderColor;
    private int mArrowColor;
    private int mInnerSectorRadius;
    private int mOuterSectorRadius;
    private int mMaxSpeed;
    private int mEnergyIcon;
    private int mEnergyLine;

    private float mTextWidth;
    private float mTextHeight;

    private RectF mBackgroundCircleRec;
    private Rect mTextRect;
    private RectF innerCircleRec;
    private RectF mScaleCircleRec;
    private Path mScalePath;

    private Matrix mBitmapMatrix;

    private int mAlphaLevel = 255;
    private int mAlphaR = 10;
    private float[] mMatrixFilter;

    private Paint mPaint;
    private Paint mFuelPaint;
    private TextPaint mTextPaint;

    private boolean stop;
    private boolean go;

    private float mCurrentSpeed;
    private float mCurrentFuelLevel = 100;
    private float mMaxFuelLevel = 100;

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

        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.SpeedometerView, defStyle, 0);

        mBackgroundColor = a.getColor(R.styleable.SpeedometerView_backgroundColor, Color.GRAY);
        mSpeedIndicatorColor = a.getColor(R.styleable.SpeedometerView_speedIndicatorColor, Color.BLACK);
        mBeforeArrowSectorColor = a.getColor(R.styleable.SpeedometerView_beforeArrowSectorColor, Color.BLACK);
        mAfterArrowSectorColor = a.getColor(R.styleable.SpeedometerView_afterArrowSectorColor, Color.BLACK);
        mBorderColor = a.getColor(R.styleable.SpeedometerView_borderColor, Color.BLACK);
        mArrowColor = a.getColor(R.styleable.SpeedometerView_arrowColor, Color.BLACK);

        mSectorRadius = a.getInteger(R.styleable.SpeedometerView_sectorRadius, Color.BLACK);
        mInnerSectorRadius = a.getInteger(R.styleable.SpeedometerView_innerSectorRadius, Color.BLACK);
        mOuterSectorRadius = a.getInteger(R.styleable.SpeedometerView_outerSectorRadius, Color.BLACK);
        mMaxSpeed = a.getInteger(R.styleable.SpeedometerView_maxSpeed, 90);
        mEnergyIcon = a.getInteger(R.styleable.SpeedometerView_energyIcon, Color.BLACK);
        mEnergyLine = a.getInteger(R.styleable.SpeedometerView_energyLine, Color.BLACK);

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mPaint = new Paint();
        mFuelPaint = new Paint();
        mTextRect = new Rect();
        mBitmapMatrix = new Matrix();

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

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        paddingLeft = getPaddingLeft();
        paddingTop = getPaddingTop();
        paddingRight = getPaddingRight();
        paddingBottom = getPaddingBottom();

        contentWidth = getWidth() - paddingLeft - paddingRight;
        contentHeight = getHeight() - paddingTop - paddingBottom;

//        radius = Math.min(contentWidth/2, contentHeight) - Math.max(Math.max(paddingLeft, paddingRight), Math.max(paddingTop, paddingBottom));
        radius = Math.min(contentWidth/2, contentHeight);

        drawBackground(canvas);
    }

    private void drawBackground(Canvas canvas){

        drawBackgroundCircle(canvas);

        drawArrowCircle(canvas);

        drawBorderCircle(canvas);

        drawInnerCircle(canvas);

        drawFuelLevel(canvas);

        drawScale(canvas);

        drawArrow(canvas);
    }

    private void drawBackgroundCircle(Canvas canvas){
        mPaint.setColor(mBackgroundColor);
        mPaint.setStyle(Paint.Style.FILL);

        mBackgroundCircleRec = new RectF(paddingLeft + contentWidth / 2 - radius,
                paddingTop + (contentHeight - radius),
                contentWidth / 2 + radius + paddingRight,
                paddingTop + (contentHeight - radius) + radius*2);

        Log.d("RADIUS", String.valueOf(radius));
        Log.d("C_HEIGHT", String.valueOf(contentHeight));
        Log.d("C_WEIGHT", String.valueOf(contentWidth));
        Log.d("REC", mBackgroundCircleRec.toString());

        Log.d("START", String.valueOf(paddingTop + contentHeight - radius));
        Log.d("FINISCH", String.valueOf(paddingTop + contentHeight));

        canvas.drawArc(mBackgroundCircleRec, -180, 180, false, mPaint);
    }

    private void drawBorderCircle(Canvas canvas){
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(radius *BORDER_HEIGHT);
        canvas.drawArc(mBackgroundCircleRec, -180, 180, false, mPaint);
    }

    private void drawArrowCircle(Canvas canvas){
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(getWidth()/2,paddingTop + contentHeight,
                radius * ARROW_CIRCLE_INDEX, mPaint);
    }

    private void drawScale(Canvas canvas){
        mPaint.setColor(Color.BLACK);

        mScaleCircleRec = new RectF(paddingLeft + contentWidth/2 - radius + radius *SCALES_WIDTH/2,
                paddingTop + (contentHeight - radius) + radius *SCALES_WIDTH/2 ,
                contentWidth/2 + radius - radius *SCALES_WIDTH/2 + paddingRight,
                paddingTop + (contentHeight - radius) + radius *SCALES_WIDTH/2 + radius*2);
        mPaint.setStrokeWidth(radius *SCALES_WIDTH);

        mScalePath = new Path();
        int scaleCount = mMaxSpeed/10;
        for(int i = mMaxSpeed, c = 0; i > 0; i -= 10, c++){
            mScalePath.addArc(mScaleCircleRec,-170 + ((180/scaleCount)*c),1f);
        }

        canvas.drawPath(mScalePath, mPaint);

        mPaint.setStrokeWidth(2);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextSize(radius *TEXT_SIZE);
        canvas.save(Canvas.MATRIX_SAVE_FLAG);

        mPaint.setStrokeWidth(radius *OUTER_CIRCLE_WIDTH);
        PointF pointF = new PointF();
        float angle;
        float step = 180/(mMaxSpeed/10);
        for(int i = 0; i < mMaxSpeed/10; ++i){

            angle = 10 + i*step;
            calculateCirclePoint(angle, radius *SCALE_RADIUS_INDEX, pointF);
            drawText(canvas, String.valueOf(10 * i + 10), pointF.x, pointF.y, mPaint);
        }
    }

    private void drawFuelLevel(Canvas canvas){
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.fuel_ico);

        RectF fuelIcoRec = new RectF(paddingLeft + contentWidth/2 - radius *0.2f,
                paddingTop + radius *0.3f,
                paddingLeft + contentWidth/2, paddingTop + radius *0.5f);
        mBitmapMatrix.mapRect(fuelIcoRec);

        mMatrixFilter = new float[]{
                0, 0, 0, 0, (1 - mCurrentFuelLevel/100)*255,
                0, 0, 0, 0, mCurrentFuelLevel/100*255,
                0, 0, 0, 0, 0,
                0, 0, 0, 1, 0
        };

        mFuelPaint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(mMatrixFilter)));
        mFuelPaint.setStyle(Paint.Style.FILL);

        canvas.drawBitmap(bitmap, null, fuelIcoRec, mFuelPaint);

        Rect fuelLevelRec = new Rect(paddingLeft + contentWidth/2,paddingTop + (int)(radius *0.37f),
                paddingLeft + (int)(contentWidth/2) + (int)(radius *0.3f*(mCurrentFuelLevel/mMaxFuelLevel)),
                (int)(radius *0.42f));
        if(mCurrentFuelLevel/mMaxFuelLevel < (float)1/3 ){

            if(mAlphaLevel < 35){
                 mAlphaR = 30;
            }else if(mAlphaLevel > 225){
                mAlphaR = -30;
            }

            mAlphaLevel += mAlphaR;

            mFuelPaint.setAlpha(mAlphaLevel);
        }else {
            mAlphaLevel = 255;
            mFuelPaint.setAlpha(mAlphaLevel);
        }

        canvas.drawRect(fuelLevelRec, mFuelPaint);
    }

    private void drawArrow(Canvas canvas){

        canvas.save();
        canvas.rotate(-90 + ((float)180/mMaxSpeed)*mCurrentSpeed, paddingLeft + contentWidth/2,
                paddingTop + contentHeight);

        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);

        Path arrow = new Path();
        arrow.moveTo(paddingLeft + contentWidth/2 - radius *ARROW_WEIGHT_INDEX, paddingTop + contentHeight);
        arrow.lineTo(paddingLeft + contentWidth/2 - radius *ARROW_WEIGHT_INDEX*ARROW_WEIGHT2_INDEX,
                paddingTop +  (contentHeight - radius*ARROW_HEIGHT_INDEX));
        arrow.lineTo(paddingLeft + contentWidth/2 + radius *ARROW_WEIGHT_INDEX*ARROW_WEIGHT2_INDEX,
                paddingTop + (contentHeight - radius*ARROW_HEIGHT_INDEX));
        arrow.lineTo(paddingLeft + contentWidth/2 + radius *ARROW_WEIGHT_INDEX, paddingTop + contentHeight);
        arrow.close();

        canvas.drawPath(arrow, mPaint);
        canvas.restore();
    }

    private void drawInnerCircle(Canvas canvas){
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(radius *INNER_CIRCLE_WIDTH);

        innerCircleRec = new RectF(paddingLeft + contentWidth/2 - radius *INNER_CIRCLE_RADIUS,
                paddingTop + contentHeight - radius *INNER_CIRCLE_RADIUS,
                contentWidth/2 + radius *INNER_CIRCLE_RADIUS + paddingRight,
                radius + radius *INNER_CIRCLE_RADIUS + paddingBottom);

        canvas.drawArc(innerCircleRec, -180, 180, false, mPaint);

        drawInnerSpeedCircle(canvas);
    }

    private void drawInnerSpeedCircle(Canvas canvas){
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(radius *INNER_CIRCLE_WIDTH);

        canvas.drawArc(innerCircleRec, - 180, ((float)180/mMaxSpeed)*mCurrentSpeed, false, mPaint);
    }

    private float changeSpeed(float newSpeedValue){
        if(newSpeedValue > mMaxSpeed){
            setmCurrentSpeed(mMaxSpeed);
        }else if(newSpeedValue < 0){
            setmCurrentSpeed(0);
        }else {
            setmCurrentSpeed(newSpeedValue);
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
                    changeSpeed(mCurrentSpeed -= calculateAcceleration(4));
                } else if (go && mCurrentFuelLevel > 0) {
                    changeSpeed(mCurrentSpeed += calculateAcceleration(4));
                    changeFuelLevel();
                } else {
                    changeSpeed(mCurrentSpeed -= 0.4);
                }

                invalidate();
            }
        }, 3000);
    }

    private void drawText(Canvas canvas, String text, float x, float y, Paint paint){
        paint.getTextBounds(text, 0, text.length(), mTextRect);
        canvas.drawText(text, x - mTextRect.width()/2f, y + mTextRect.height()/2f, paint);
    }

    private void changeFuelLevel(){
        if(mCurrentFuelLevel != 0 && mCurrentFuelLevel <= 100){
            mCurrentFuelLevel -= 1;
        }else if(mCurrentFuelLevel <= 0 && go){
            go = false;
        }
    }

    private float calculateAcceleration(float baseAcceleration){
        return (1 - mCurrentSpeed/mMaxSpeed)*baseAcceleration;
    }

    private void calculateCirclePoint(float angle, float radius, PointF point) {

        point.set((float) (paddingLeft + contentWidth/2 - radius * Math.cos(angle / 180 * Math.PI)),
                (float) (paddingTop + this.radius - radius * Math.sin(angle / 180 * Math.PI)));
    }

    public void setFuelLevel(int newLevel){
        mCurrentFuelLevel = newLevel;
    }

    public int getmBackgroundColor() {
        return mBackgroundColor;
    }

    public void setmBackgroundColor(int mBackgroundColor) {
        this.mBackgroundColor = mBackgroundColor;
    }

    public int getmSpeedIndicatorColor() {
        return mSpeedIndicatorColor;
    }

    public void setmSpeedIndicatorColor(int mSpeedIndicatorColor) {
        this.mSpeedIndicatorColor = mSpeedIndicatorColor;
    }

    public int getmBeforeArrowSectorColor() {
        return mBeforeArrowSectorColor;
    }

    public void setmBeforeArrowSectorColor(int mBeforeArrowSectorColor) {
        this.mBeforeArrowSectorColor = mBeforeArrowSectorColor;
    }

    public int getmAfterArrowSectorColor() {
        return mAfterArrowSectorColor;
    }

    public void setmAfterArrowSectorColor(int mAfterArrowSectorColor) {
        this.mAfterArrowSectorColor = mAfterArrowSectorColor;
    }

    public int getmSectorRadius() {
        return mSectorRadius;
    }

    public void setmSectorRadius(int mSectorRadius) {
        this.mSectorRadius = mSectorRadius;
    }

    public int getmBorderColor() {
        return mBorderColor;
    }

    public void setmBorderColor(int mBorderColor) {
        this.mBorderColor = mBorderColor;
    }

    public int getmArrowColor() {
        return mArrowColor;
    }

    public void setmArrowColor(int mArrowColor) {
        this.mArrowColor = mArrowColor;
    }

    public int getmInnerSectorRadius() {
        return mInnerSectorRadius;
    }

    public void setmInnerSectorRadius(int mInnerSectorRadius) {
        this.mInnerSectorRadius = mInnerSectorRadius;
    }

    public int getmOuterSectorRadius() {
        return mOuterSectorRadius;
    }

    public void setmOuterSectorRadius(int mOuterSectorRadius) {
        this.mOuterSectorRadius = mOuterSectorRadius;
    }

    public int getmMaxSpeed() {
        return mMaxSpeed;
    }

    public void setmMaxSpeed(int mMaxSpeed) {
        this.mMaxSpeed = mMaxSpeed;
    }

    public int getmEnergyIcon() {
        return mEnergyIcon;
    }

    public void setmEnergyIcon(int mEnergyIcon) {
        this.mEnergyIcon = mEnergyIcon;
    }

    public int getmEnergyLine() {
        return mEnergyLine;
    }

    public void setmEnergyLine(int mEnergyLine) {
        this.mEnergyLine = mEnergyLine;
    }

    public Paint getmPaint() {
        return mPaint;
    }

    public void setmPaint(Paint mPaint) {
        this.mPaint = mPaint;
    }

    public TextPaint getmTextPaint() {
        return mTextPaint;
    }

    public void setmTextPaint(TextPaint mTextPaint) {
        this.mTextPaint = mTextPaint;
    }

    public float getmTextWidth() {
        return mTextWidth;
    }

    public void setmTextWidth(float mTextWidth) {
        this.mTextWidth = mTextWidth;
    }

    public float getmTextHeight() {
        return mTextHeight;
    }

    public void setmTextHeight(float mTextHeight) {
        this.mTextHeight = mTextHeight;
    }

    public float getmCurrentSpeed() {
        return mCurrentSpeed;
    }

    public void setmCurrentSpeed(float mCurrentSpeed) {
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

    public float getmCurrentFuelLevel() {
        return mCurrentFuelLevel;
    }

    public void setmCurrentFuelLevel(float mCurrentFuelLevel) {
        this.mCurrentFuelLevel = mCurrentFuelLevel;
    }

    public float getmMaxFuelLevel() {
        return mMaxFuelLevel;
    }

    public void setmMaxFuelLevel(float mMaxFuelLevel) {
        this.mMaxFuelLevel = mMaxFuelLevel;
    }


    interface SpeedChangeListener {

        void onSpeedChange(float newSpeedValue);
    }
}
