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

import android.view.View;

public class SpeedometerView extends View {

    private final float ARROW_CIRCLE_INDEX = 0.1f;
    private final float ARROW_HEIGHT_INDEX = 0.3f;
    private final float ARROW_WEIGHT_INDEX = 0.08f;
    private final float ARROW_WEIGHT2_INDEX = 0.4f;
    private final float SCALE_RADIUS_INDEX = 0.8f;

    private final float SCALE_HEIGHT = 10;
    private final float TEXT_HEIGHT = 0.1f;
    private final float SCALES_WIDTH = 0.1f;

    private final float TEXT_SIZE = 0.15f;

    private final float FUEL_ICO_SIZE = 0.2f;

    private final float INNER_CIRCLE_WIDTH = 0.15f;
    private final float INNER_CIRCLE_RADIUS = 0.4f;
    private final float OUTER_CIRCLE_WIDTH = 0.05f;

    private final float FUEL_X = 0.8f;
    private final float FUEL_Y = 0.68f;

    private int mBackgroundColor = Color.GRAY;
    private int mSpeedIndicatorColor;
    private int mBeforeArrowSectorColor;
    private int mAfterArrowSectorColor;

    private int mSectorRadius;
    private int mBorderColor;
    private int mArrowColor;
    private int mInnerSectorRadius;
    private int mOuterSectorRadius;
    private int mMaxSpeed = 100;
    private int mEnergyIcon;
    private int mEnergyLine;

    private float mFuelConsumption = 1;

    private int mAlphaLevel = 255;
    private int mAlphaR = 10;

    private float[] mMatrixFilter = new float[]{
            0, 0, 0, 0, 255,
            0, 255, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0, 0, 1, 0};

    private ColorMatrixColorFilter mColorFilter;

    private Paint mPaint;
    private Paint mPaint1;
    private TextPaint mTextPaint;

    private float mTextWidth;
    private float mTextHeight;

    int paddingLeft = getPaddingLeft();
    int paddingTop = getPaddingTop();
    int paddingRight = getPaddingRight();
    int paddingBottom = getPaddingBottom();

    int contentWidth = getWidth() - paddingLeft - paddingRight;
    int contentHeight = getHeight() - paddingTop - paddingBottom;

    private boolean stop;
    private boolean go;

    private SpeedometerView.SpeedChangeListener listener;

    private float mCurrentSpeed;
    private float mCurrentFuelLevel = 100;
    private float mMaxFuelLevel = 100;

    private Rect mTextRect;

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

        mBackgroundColor = a.getColor(R.styleable.SpeedometerView_backgroundColor, Color.BLUE);
        mSpeedIndicatorColor = a.getColor(R.styleable.SpeedometerView_speedIndicatorColor, Color.BLUE);
        mBeforeArrowSectorColor = a.getColor(R.styleable.SpeedometerView_beforeArrowSectorColor, Color.BLUE);
        mAfterArrowSectorColor = a.getColor(R.styleable.SpeedometerView_afterArrowSectorColor, Color.BLUE);
        mBorderColor = a.getColor(R.styleable.SpeedometerView_borderColor, Color.BLUE);
        mArrowColor = a.getColor(R.styleable.SpeedometerView_arrowColor, Color.BLUE);


        mSectorRadius = a.getInteger(R.styleable.SpeedometerView_sectorRadius, Color.BLUE);
        mInnerSectorRadius = a.getInteger(R.styleable.SpeedometerView_innerSectorRadius, Color.BLUE);
        mOuterSectorRadius = a.getInteger(R.styleable.SpeedometerView_outerSectorRadius, Color.BLUE);
        mMaxSpeed = a.getInteger(R.styleable.SpeedometerView_maxSpeed, 100);
        mEnergyIcon = a.getInteger(R.styleable.SpeedometerView_energyIcon, Color.BLUE);
        mEnergyLine = a.getInteger(R.styleable.SpeedometerView_energyLine, Color.BLUE);

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mPaint = new Paint();
        mPaint1 = new Paint();

        mTextRect = new Rect();

        mColorFilter = new ColorMatrixColorFilter(mMatrixFilter);

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

        drawBackground(canvas);
        drawArrow(canvas);
    }

    private void drawBackground(Canvas canvas){

        //      draw background circle
        mPaint.setColor(mBackgroundColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(contentWidth/2, contentHeight, contentHeight, mPaint);

        //      draw  arrow circle
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(contentWidth/2, contentHeight, contentHeight*(float) ARROW_CIRCLE_INDEX, mPaint);

        drawInnerCircle(canvas);
        drawFuelLevel(canvas);

        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(SCALE_HEIGHT);
        canvas.drawCircle(contentWidth/2, contentHeight, contentHeight, mPaint);

        //      draw scale
        RectF rectF = new RectF(contentWidth/2 - contentHeight + contentHeight*SCALES_WIDTH/2, contentHeight*SCALES_WIDTH/2 ,
                contentWidth/2 + contentHeight - contentHeight*SCALES_WIDTH/2,  contentHeight*2 - contentHeight*SCALES_WIDTH/2);
        mPaint.setStrokeWidth(contentHeight*SCALES_WIDTH);

        Path p = new Path();

        int scaleCount = mMaxSpeed/10;
        for(int i = mMaxSpeed, c = 0; i > 0; i -= 10, c++){
            p.addArc(rectF,-170 + ((180/scaleCount)*c),1f);
        }

        canvas.drawPath(p, mPaint);

        float centerX = contentWidth/2;
        float centerY = contentHeight/2;
        float radius = (float)contentHeight;
        float maxSpeed = 200;

        mPaint.setStrokeWidth(2);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextSize(contentHeight*TEXT_SIZE);
        canvas.save(Canvas.MATRIX_SAVE_FLAG);

        mPaint.setStrokeWidth(contentHeight*OUTER_CIRCLE_WIDTH);
        mPaint.setColor(Color.BLACK);
        PointF pointF = new PointF();
        float angle = 10;
        float step = 180/(mMaxSpeed/10);
        for(int i = 0; i < mMaxSpeed/10; ++i){

            angle = 10 + i*step;
            calculateCirclePoint(angle, contentHeight*SCALE_RADIUS_INDEX, pointF);
            drawText(canvas, String.valueOf((int)10*i), pointF.x, pointF.y, mPaint);
        }
    }

    private void drawFuelLevel(Canvas canvas){

//        draw fuel picture
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.fuel_ico);

        Matrix matrix = new Matrix();
        matrix = new Matrix();

        RectF rectF = new RectF(contentWidth/2 - contentHeight*0.2f, contentHeight*0.3f,
                contentWidth/2, contentHeight*0.5f);
        matrix.mapRect(rectF);

        canvas.drawBitmap(bitmap, null, rectF, mPaint);

        mMatrixFilter = new float[]{
                0, 0, 0, 0, (1 - mCurrentFuelLevel/100)*255,
                0, 0, 0, 0, mCurrentFuelLevel/100*255,
                0, 0, 0, 0, 0,
                0, 0, 0, 1, 0};

        mPaint1.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(mMatrixFilter)));
//        mPaint.setColor(Color.GREEN);
        mPaint1.setStyle(Paint.Style.FILL);

        Rect fuelLevel = new Rect(contentWidth/2, (int)(contentHeight*0.37f),
                (int)(contentWidth/2) + (int)(contentHeight*0.3f*(mCurrentFuelLevel/mMaxFuelLevel)), (int)(contentHeight*0.42f));
        if((float)mCurrentFuelLevel/mMaxFuelLevel < (float)1/3 ){

            if(mAlphaLevel < 35){
                 mAlphaR = 30;
            }else if(mAlphaLevel > 225){
                mAlphaR = -30;
            }

            mAlphaLevel += mAlphaR;

            mPaint1.setAlpha(mAlphaLevel);
        }else {
            mAlphaLevel = 255;
            mPaint1.setAlpha(mAlphaLevel);
        }

        canvas.drawRect(fuelLevel, mPaint1);
    }

    private void drawArrow(Canvas canvas){

        canvas.save();
        canvas.rotate(-90 + ((float)180/mMaxSpeed)*mCurrentSpeed, contentWidth/2, contentHeight);

        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);

        Path arrow = new Path();
        arrow.moveTo(contentWidth/2 - contentHeight*ARROW_WEIGHT_INDEX, contentHeight);
        arrow.lineTo(contentWidth/2 - contentHeight*ARROW_WEIGHT_INDEX*ARROW_WEIGHT2_INDEX, contentHeight*ARROW_HEIGHT_INDEX);
        arrow.lineTo(contentWidth/2 + contentHeight*ARROW_WEIGHT_INDEX*ARROW_WEIGHT2_INDEX, contentHeight*ARROW_HEIGHT_INDEX);
        arrow.lineTo(contentWidth/2 + contentHeight*ARROW_WEIGHT_INDEX, contentHeight);
        arrow.close();

        canvas.drawPath(arrow, mPaint);

        canvas.restore();
    }

    private void drawInnerCircle(Canvas canvas){
        //      draw inner stroke circle for arrow
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(contentHeight*INNER_CIRCLE_WIDTH);
        canvas.drawCircle(contentWidth/2, contentHeight, contentHeight*INNER_CIRCLE_RADIUS, mPaint);
        drawInnerCircle1(canvas);
    }

    private void drawInnerCircle1(Canvas canvas){
        //      draw inner stroke circle for arrow
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(contentHeight*INNER_CIRCLE_WIDTH);

        RectF rectF = new RectF(contentWidth/2 - contentHeight*INNER_CIRCLE_RADIUS,contentHeight - contentHeight*INNER_CIRCLE_RADIUS,
                contentWidth/2 + contentHeight*INNER_CIRCLE_RADIUS, contentHeight + contentHeight*INNER_CIRCLE_RADIUS);

        canvas.drawArc(rectF, ((float)180/mMaxSpeed)*mCurrentSpeed, 180, false, mPaint);
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

        point.set((float) (contentWidth/2 - radius * Math.cos(angle / 180 * Math.PI)),
                (float) (contentHeight - radius * Math.sin(angle / 180 * Math.PI)));
    }

    public void setFuelLevel(int newLevel){
        mCurrentFuelLevel = newLevel;
    }

    public void setFuelConsumptionPerSecond(float newConsumption){
        mFuelConsumption = newConsumption;
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

        void showInfoMessage(String string);
    }
}
