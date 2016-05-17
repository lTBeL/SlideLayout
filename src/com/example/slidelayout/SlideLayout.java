package com.example.slidelayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class SlideLayout extends FrameLayout {

	private ViewGroup mBelowView;
	private ViewGroup mAboveView;
	private int mBelowWidth;
	private int mAboveWidth;
	private int mAboveHeight;
	private Status mStatus;
	private int mBelowHeight;
	private ViewDragHelper mDragHelper;

	public SlideLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public SlideLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SlideLayout(Context context) {
		super(context);
		init();
	}

	private void init() {
		mStatus = Status.below;
		mDragHelper = ViewDragHelper.create(this, mCallback);
	}

	ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
		public boolean tryCaptureView(View arg0, int arg1) {
			return true;
		}

		@Override
		public int clampViewPositionVertical(View child, int top, int dy) {
			if (child == mAboveView) {
				if (top <= 0) {
					mStatus = Status.draging;
					if(top == 0)
						mStatus = Status.above;
					return top;
				}
			} else if (child == mBelowView) {
				if (top >= 0) {
					mStatus = Status.draging;
					if(top == 0)
						mStatus = Status.below;
					return top;
				}
			}
			return super.clampViewPositionVertical(child, top, dy);
		}

		public void onViewPositionChanged(View changedView, int left, int top,
				int dx, int dy) {
			super.onViewPositionChanged(changedView, left, top, dx, dy);

			if (changedView == mAboveView) {
				int pl = getPaddingLeft();
				mBelowView.layout(pl, top + mAboveHeight, pl + mBelowWidth, top
						+ mAboveHeight + mBelowHeight);
			} else if (changedView == mBelowView) {
				int pl = getPaddingLeft();
				mAboveView
						.layout(pl, top - mAboveHeight, pl + mAboveWidth, top);
			}
			invalidate();
		}

		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			super.onViewReleased(releasedChild, xvel, yvel);
			if(yvel == 0 && mBelowView.getTop() > getHeight() / 2.0f){
				slideDown();
			}else if (yvel > 0) {
				slideDown();
			}else {
				slideUp();
			}
		}
		@Override
		public int getViewVerticalDragRange(View child) {
			return 1;
		}
	};

	private void slideUp() {
		if(mDragHelper.smoothSlideViewTo(mAboveView, 0, -mAboveHeight)){
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	private void slideDown() {
		if(mDragHelper.smoothSlideViewTo(mAboveView, 0, 0)){
			ViewCompat.postInvalidateOnAnimation(this);
		}
	};
	@Override
	public void computeScroll() {
		super.computeScroll();
		if(mDragHelper.continueSettling(true)){
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		try {
			mDragHelper.processTouchEvent(event);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	protected void onFinishInflate() {
		super.onFinishInflate();

		if (getChildCount() < 2) {
			throw new IllegalStateException(
					" Your ViewGroup must have 2 ViewGroup children at least.");
		}
		if (!(getChildAt(0) instanceof ViewGroup && getChildAt(1) instanceof ViewGroup)) {
			throw new IllegalArgumentException(
					"子View必须是ViewGroup的子类. Your children must be an instance of ViewGroup");
		}

		mBelowView = (ViewGroup) getChildAt(1);
		mAboveView = (ViewGroup) getChildAt(0);
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mBelowHeight = mBelowView.getMeasuredHeight();
		mBelowWidth = mBelowView.getMeasuredWidth();
		mAboveHeight = mAboveView.getMeasuredHeight();
		mAboveWidth = mAboveView.getMeasuredWidth();
	}

	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (mStatus == Status.above) {
			mBelowView.layout(left, bottom, right, bottom + mBelowHeight);
		} else if (mStatus == Status.below) {
			mAboveView.layout(left, top - mAboveHeight, right, top);
		}
	}

	/**
	 * 应当有上下两幅视图,粗略分为3种状态
	 *
	 */
	private enum Status {
		/**
		 * 显示上方视图
		 */
		above,
		/**
		 * 显示下方视图
		 */
		below,
		/**
		 * 拖拽
		 */
		draging;
	}
}
