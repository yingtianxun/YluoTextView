package com.example.yluotextview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class YluoTextView extends TextView {
	private static final String TAG = "YluoTextView";
	
	private int mTextColor;
	
	private Drawable mOriginDrawable;
	private Paint mPaint;
	private Drawable[] mNormalDrawables;
	private Drawable[] mPressedDrawables;
	private Drawable[] mDrawables;
	
	private int mPressedColor = -1;
	private float mDrawableWidth = -1;
	private float mDrawableHeight = -1;
	private boolean mIsCreatePressedDrawable = false;
	private Drawable mPressedDrawable = null;

	private boolean mIsUse = false;
	
	private int mDrawableDirection = -1;

	public YluoTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		getAttr(attrs);
		init();
	}

	public YluoTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		getAttr(attrs);
		init();
	}

	public YluoTextView(Context context) {
		super(context);

		init();
	}

	private void getAttr(AttributeSet attrs) {
		TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.YluoTextView);

		for (int i = 0; i < a.length(); i++) {
			switch (a.getIndex(i)) {
			case R.styleable.YluoTextView_create_presseddawable:
				mIsCreatePressedDrawable = a.getBoolean(R.styleable.YluoTextView_create_presseddawable, false);
				break;
			case R.styleable.YluoTextView_drawable_height:
				mDrawableHeight = a.getDimension(R.styleable.YluoTextView_drawable_height, -1);
				break;
			case R.styleable.YluoTextView_drawable_width:
				mDrawableWidth = a.getDimension(R.styleable.YluoTextView_drawable_width, -1);
				break;
			case R.styleable.YluoTextView_press_drawable:
				mPressedDrawable = a.getDrawable(R.styleable.YluoTextView_press_drawable);
				break;
			case R.styleable.YluoTextView_pressed_textcolor:
				mPressedColor = a.getColor(R.styleable.YluoTextView_pressed_textcolor, -1);
				break;
			case R.styleable.YluoTextView_use_yluo:
				mIsUse = a.getBoolean(R.styleable.YluoTextView_use_yluo, false);
				break;
			default:
				break;
			}
		}

		a.recycle();
	}

	private void init() {
		
		if(!mIsUse) {
			return;
		}
		mDrawables = getCompoundDrawables();

		for (int i = 0; i < mDrawables.length; i++) {
			if (mDrawables[i] != null) {
				mDrawableDirection = i;
				mOriginDrawable = mDrawables[i];
				break;
			}
		}

		if (mDrawableDirection == -1) {
			mIsUse = false;
			// 没有任何需要画的
			return;
		}

		if (mDrawableWidth == -1) {
			mDrawableWidth = mOriginDrawable.getIntrinsicWidth();
		}
		if (mDrawableHeight == -1) {
			mDrawableWidth = mOriginDrawable.getIntrinsicHeight();
		}

		ColorStateList colors = getTextColors();

		mTextColor = colors.getDefaultColor();
		
		
		//设置按下颜色
		if (mPressedColor == -1) {
			mPressedColor = mTextColor;
			
		}

		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		mPaint.setColor(mPressedColor);

		setClickable(true);

		changeDrawablesColor(); // 创建修改颜色的
		
		
		if(mPressedDrawable != null) {
			mPressedDrawables = new Drawable[mDrawables.length];
			mPressedDrawables[mDrawableDirection] = mPressedDrawable;
		}else if (mIsCreatePressedDrawable) {
			// 创建按下的图片
			createPressedDrawable();
		} else {
			mPressedDrawables = new Drawable[mDrawables.length];
			// 默认使用同一个了
			mPressedDrawables[mDrawableDirection] = mNormalDrawables[mDrawableDirection];
		}
	}

	@SuppressLint("NewApi")
	private void changeDrawablesColor() {
		mNormalDrawables = new Drawable[mDrawables.length];

		createDrawables(mNormalDrawables,255,mTextColor);
		
		setCompoundDrawables(mNormalDrawables[0], mNormalDrawables[1],
				mNormalDrawables[2], mNormalDrawables[3]);
	}

	private void createPressedDrawable() {

		mPressedDrawables = new Drawable[mDrawables.length];
		
		createDrawables(mPressedDrawables,150,mPressedColor);
	}
	
	private void createDrawables(Drawable[] createDrawables,int alpha,int paintColor) {
		
		BitmapDrawable bd = (BitmapDrawable) mOriginDrawable;

		Bitmap originBitmap = bd.getBitmap();

		Bitmap dstBmp = Bitmap.createBitmap(
				mOriginDrawable.getIntrinsicWidth(),
				mOriginDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(dstBmp);
		
		mPaint.setColor(paintColor);
		canvas.drawBitmap(originBitmap, 0, 0, mPaint);
		mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		mPaint.setAlpha(alpha);
		
		Rect bounds = new Rect(0, 0, mOriginDrawable.getIntrinsicWidth(),
				mOriginDrawable.getIntrinsicHeight());

		canvas.drawRect(bounds, mPaint);

		mPaint.setXfermode(null);

		createDrawables[mDrawableDirection] = new BitmapDrawable(getContext()
				.getResources(), dstBmp);

		createDrawables[mDrawableDirection].setBounds(new Rect(0, 0,
				(int) mDrawableWidth, (int) mDrawableHeight));
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(!mIsUse) {
			return super.onTouchEvent(event);
		}
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			handlePressEvent();
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP: {
			handleUpEvent();
		}
		default:
			break;
		}

		return super.onTouchEvent(event);
	}

	private void handleUpEvent() {
		setTextColor(mTextColor);
		setCompoundDrawables(mNormalDrawables[0], mNormalDrawables[1],
				mNormalDrawables[2], mNormalDrawables[3]);
		invalidate();
	}

	private void handlePressEvent() {
		setTextColor(mPressedColor);
		setCompoundDrawables(mPressedDrawables[0], mPressedDrawables[1],
				mPressedDrawables[2], mPressedDrawables[3]);
		invalidate();
	}



}
