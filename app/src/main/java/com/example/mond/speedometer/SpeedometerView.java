package com.example.mond.speedometer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class SpeedometerView extends View {

    private final float ARROW_CIRCLE_INDEX = 0.1f;
    private final float ARROW_HEIGHT_INDEX = 0.3f;
    private final float ARROW_WEIGHT_INDEX = 0.03f;
    private final float ARROW_WEIGHT2_INDEX = 0.4f;
    private final float SCALE_RADIUS_INDEX = 0.8f;

    private final float FUEL_X = 1f;
    private final float FUEL_Y = 0.78f;

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

    private Paint mPaint;

    private TextPaint mTextPaint;

    private float mTextWidth;
    private float mTextHeight;

    int paddingLeft = getPaddingLeft();
    int paddingTop = getPaddingTop();
    int paddingRight = getPaddingRight();
    int paddingBottom = getPaddingBottom();

    int contentWidth = getWidth() - paddingLeft - paddingRight;
    int contentHeight = getHeight() - paddingTop - paddingBottom;

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
        mMaxSpeed = a.getInteger(R.styleable.SpeedometerView_maxSpeed, Color.BLUE);
        mEnergyIcon = a.getInteger(R.styleable.SpeedometerView_energyIcon, Color.BLUE);
        mEnergyLine = a.getInteger(R.styleable.SpeedometerView_energyLine, Color.BLUE);

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mPaint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desiredWidth = 200;
        int desiredHeight = 100;

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

        Log.d("DDDDDD", "cw is =  " + String.valueOf(contentWidth));
        Log.d("DDDDDD", "ch is =  " + String.valueOf(contentHeight));

        Log.d("DDDDDD", "pt is =  " + String.valueOf(paddingTop));
        Log.d("DDDDDD", "pb is =  " + String.valueOf(paddingBottom));

        //      draw background circle
        mPaint.setColor(mBackgroundColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(contentWidth/2, contentHeight, contentHeight, mPaint);

        //      draw  arrow circle
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(contentWidth/2, contentHeight, contentHeight*(float) ARROW_CIRCLE_INDEX, mPaint);

        //      draw inner stroke circle for arrow
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(40);
        canvas.drawCircle(contentWidth/2, contentHeight, contentHeight/2, mPaint);


        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(10);
        canvas.drawCircle(contentWidth/2, contentHeight, contentHeight, mPaint);


        //      draw scale
        RectF rectF = new RectF(0, 0, 600, 600);
        mPaint.setStrokeWidth(30);

        Path p = new Path();
        for(int i = -180; i < 0; i+= 10){
            p.addArc(rectF,i,2f);
        }
        canvas.drawPath(p, mPaint);

        float centerX = contentWidth/2;
        float centerY = contentHeight/2;
        float radius = (float)contentHeight;
        float maxSpeed = 100;

        mPaint.setStrokeWidth(2);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextSize(40);
        canvas.save(Canvas.MATRIX_SAVE_FLAG);


        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(10);
        mPaint.setColor(Color.BLACK);
        PointF pointF = new PointF();
        float angle = 10;
        float step = 180/10;
        for(int i = 0; i < 10; ++i){

            angle = 10+ i*step;
            calculateCirclePoint(angle, contentHeight*SCALE_RADIUS_INDEX, pointF);
            canvas.drawText("A", pointF.x, pointF.y, mPaint);
        }

//        draw fuel picture
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.fuil_ico);

        Matrix matrix = new Matrix();
        matrix = new Matrix();
        matrix.postScale(0.4f, 0.4f);
        matrix.preTranslate(contentWidth*FUEL_X, contentHeight*FUEL_Y);

        Rect bitmapRect = new Rect(100, 100, bitmap.getWidth()/2, bitmap.getHeight()/2 );

        canvas.drawBitmap(bitmap, matrix, mPaint);

    }


    private void calculateCirclePoint(float angle, float radius, PointF point) {

        point.set((float) (contentWidth/2 - radius * Math.cos(angle / 180 * Math.PI)),
                (float) (contentHeight - radius * Math.sin(angle / 180 * Math.PI)));
    }

    private void drawScale(Canvas canvas){

    }

    private void drawArrow(Canvas canvas){
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);

        Path arrow = new Path();
        arrow.moveTo(contentWidth/2 - contentWidth*ARROW_WEIGHT_INDEX, contentHeight);
        arrow.lineTo(contentWidth/2 - contentWidth*ARROW_WEIGHT_INDEX*ARROW_WEIGHT2_INDEX, contentHeight*ARROW_HEIGHT_INDEX);
        arrow.lineTo(contentWidth/2 + contentWidth*ARROW_WEIGHT_INDEX*ARROW_WEIGHT2_INDEX, contentHeight*ARROW_HEIGHT_INDEX);
        arrow.lineTo(contentWidth/2 + contentWidth*ARROW_WEIGHT_INDEX, contentHeight);
        arrow.close();

        canvas.drawPath(arrow, mPaint);
    }


    public void setmBackgroundColor(int mBackgroundColor) {
        this.mBackgroundColor = mBackgroundColor;
    }

    public void setmSpeedIndicatorColor(int mSpeedIndicatorColor) {
        this.mSpeedIndicatorColor = mSpeedIndicatorColor;
    }

    public void setmBeforeArrowSectorColor(int mBeforeArrowSectorColor) {
        this.mBeforeArrowSectorColor = mBeforeArrowSectorColor;
    }

    public void setmAfterArrowSectorColor(int mAfterArrowSectorColor) {
        this.mAfterArrowSectorColor = mAfterArrowSectorColor;
    }

    public void setmSectorRadius(int mSectorRadius) {
        this.mSectorRadius = mSectorRadius;
    }

    public void setmBorderColor(int mBorderColor) {
        this.mBorderColor = mBorderColor;
    }

    public void setmArrowColor(int mArrowColor) {
        this.mArrowColor = mArrowColor;
    }

    public void setmInnerSectorRadius(int mInnerSectorRadius) {
        this.mInnerSectorRadius = mInnerSectorRadius;
    }

    public void setmOuterSectorRadius(int mOuterSectorRadius) {
        this.mOuterSectorRadius = mOuterSectorRadius;
    }

    public void setmMaxSpeed(int mMaxSpeed) {
        this.mMaxSpeed = mMaxSpeed;
    }

    public void setmEnergyIcon(int mEnergyIcon) {
        this.mEnergyIcon = mEnergyIcon;
    }

    public void setmEnergyLine(int mEnergyLine) {
        this.mEnergyLine = mEnergyLine;
    }

    public void setmTextPaint(TextPaint mTextPaint) {
        this.mTextPaint = mTextPaint;
    }

    public void setmTextWidth(float mTextWidth) {
        this.mTextWidth = mTextWidth;
    }

    public void setmTextHeight(float mTextHeight) {
        this.mTextHeight = mTextHeight;
    }
}
