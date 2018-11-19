package cn.fanrunqi.waveprogress;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import java.lang.ref.WeakReference;

/**
 * A custom view that creates a wave animation on a image
 *
 * Created by fanrunqi on 2016/7/6.
 */
public class WaveProgressView extends View {

    private static Handler mHandler;

    private static final int INVALIDATE = 0X777;

    private static final int REFRESHGAP = 10;

    private Bitmap mBackgroundBitmap;

    private float mCurY;

    private int mCurrentProgress = 0;

    private String mCurrentText = "";

    private float mDistance = 0;

    private int mHeight;

    private Path mPath;

    private Paint mPathPaint;

    private String mTextColor = "#FFFFFF";

    private Paint mTextPaint;

    private int mTextSize = 41;

    private String mWaveColor = "#5be4ef";

    private float mWaveHalfWidth = 100f;

    private float mWaveHight = 20f;

    private int mWaveSpeed = 30;

    private int mMaxProgress = 100;

    private int mWidth;

    /**
     * Creates instance of WaveProgressView
     * @param context context
     */
    public WaveProgressView(Context context) {
        this(context, null, 0);
    }

    public WaveProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Sets current progress
     *
     * @param currentProgress Progress value
     * @param currentText     Text on the image.
     */
    public void setCurrent(int currentProgress, String currentText) {
        this.mCurrentProgress = currentProgress;
        this.mCurrentText = currentText;
    }

    /**
     * Sets max progress
     *
     * @param maxProgress The max progress value, e.g 100
     */
    public void setMaxProgress(int maxProgress) {
        this.mMaxProgress = maxProgress;
    }

    /**
     * Sets text color and text size
     *
     * @param mTextColor The text color
     * @param textSize  The text size
     */
    public void setText(String mTextColor, int textSize) {
        this.mTextColor = mTextColor;
        this.mTextSize = textSize;
    }

    /**
     * Sets the wave height and wave mWidth
     *
     * @param waveHeight The wave height
     * @param waveWidth The wave mWidth
     */
    public void setWave(float waveHeight, float waveWidth) {
        this.mWaveHight = waveHeight;
        this.mWaveHalfWidth = waveWidth / 2;
    }

    /**
     * Sets the wave color
     *
     * @param waveColor The color
     */
    public void setWaveColor(String waveColor) {
        this.mWaveColor = waveColor;
    }

    /**
     * Sets the wave speed
     *
     * @param mWaveSpeed the speed.
     */
    public void setWaveSpeed(int mWaveSpeed) {
        this.mWaveSpeed = mWaveSpeed;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mHandler.sendEmptyMessage(INVALIDATE);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacksAndMessages(mHandler);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBackgroundBitmap != null) {
            canvas.drawBitmap(createImage(), 0, 0, null);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mCurY = mHeight = MeasureSpec.getSize(heightMeasureSpec);
    }

    private Bitmap createImage() {
        mPathPaint.setColor(Color.parseColor(mWaveColor));
        mTextPaint.setColor(Color.parseColor(mTextColor));
        mTextPaint.setTextSize(mTextSize);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap finalBmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(finalBmp);
        float CurMidY = mHeight * (mMaxProgress - mCurrentProgress) / mMaxProgress;
        if (mCurY > CurMidY) {
            mCurY = mCurY - (mCurY - CurMidY) / 10;
        }
        mPath.reset();
        mPath.moveTo(0 - mDistance, mCurY);

        int waveNum = mWidth / ((int) mWaveHalfWidth * 4) + 1;
        int multiplier = 0;
        for (int i = 0; i < waveNum * 3; i++) {
            mPath.quadTo(mWaveHalfWidth * (multiplier + 1) - mDistance, mCurY - mWaveHight,
                    mWaveHalfWidth * (multiplier + 2) - mDistance, mCurY);
            mPath.quadTo(mWaveHalfWidth * (multiplier + 3) - mDistance, mCurY + mWaveHight,
                    mWaveHalfWidth * (multiplier + 4) - mDistance, mCurY);
            multiplier += 4;
        }
        mDistance += mWaveHalfWidth / mWaveSpeed;
        mDistance = mDistance % (mWaveHalfWidth * 4);

        mPath.lineTo(mWidth, mHeight);
        mPath.lineTo(0, mHeight);
        mPath.close();
        canvas.drawPath(mPath, mPathPaint);
        int min = Math.min(mWidth, mHeight);
        mBackgroundBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap, min, min, false);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
        canvas.drawBitmap(mBackgroundBitmap, 0, 0, paint);
        canvas.drawText(mCurrentText, mWidth / 2, mHeight / 2, mTextPaint);
        return finalBmp;
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        try {
            Bitmap bitmap;
            bitmap = Bitmap
                    .createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                            Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    private void init() {
        if (null == getBackground()) {
            throw new IllegalArgumentException(String.format("background is null."));
        } else {
            mBackgroundBitmap = getBitmapFromDrawable(getBackground());
        }
        mPath = new Path();
        mPathPaint = new Paint();
        mHandler = new MyHandler(this);
        mPathPaint.setAntiAlias(true);
        mPathPaint.setStyle(Paint.Style.FILL);
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    private static class MyHandler extends Handler {

        private final WeakReference<WaveProgressView> mView;

        /**
         * Creates instance of handler.
         * @param view
         */
        public MyHandler(WaveProgressView view) {
            mView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            WaveProgressView waveProgressView = mView.get();
            if (waveProgressView != null) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case INVALIDATE:
                        waveProgressView.invalidate();
                        sendEmptyMessageDelayed(INVALIDATE, REFRESHGAP);
                        break;
                }
            }
        }
    }
}
