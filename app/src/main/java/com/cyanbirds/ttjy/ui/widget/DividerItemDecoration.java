package com.cyanbirds.ttjy.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * 
 * @ClassName:DividerItemDecoration
 * @Description:RecyclerView分隔线
 * @author wangyb
 * @Date:2015年7月1日上午10:33:39
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {

	private static final int[] ATTRS = new int[] { android.R.attr.listDivider };

	public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

	public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

	private Drawable mDivider;

	private int mOrientation;

	private int mPaddingRight;
	private int mPaddingLeft;

	private int mDividingLine = 1;

	public DividerItemDecoration(Context context, int orientation,
								 int paddingLeft, int paddingRight) {
		final TypedArray a = context.obtainStyledAttributes(ATTRS);
		mDivider = a.getDrawable(0);
		this.mPaddingLeft = paddingLeft;
		this.mPaddingRight = paddingRight;
		a.recycle();
		setOrientation(orientation);
	}

	public void setOrientation(int orientation) {
		if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
			throw new IllegalArgumentException("invalid orientation");
		}
		mOrientation = orientation;
	}

	@Override
	public void onDraw(Canvas c, RecyclerView parent) {
		if (mOrientation == VERTICAL_LIST) {
			drawVertical(c, parent);
		} else {
			drawHorizontal(c, parent);
		}
	}

	public void drawVertical(Canvas c, RecyclerView parent) {
		final int left = parent.getPaddingLeft();
		final int right = parent.getWidth() - parent.getPaddingRight();

		final int childCount = parent.getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View child = parent.getChildAt(i);
			final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
					.getLayoutParams();
			final int top = child.getBottom() + params.bottomMargin;
			final int bottom = top + mDividingLine;
			mDivider.setBounds(left + mPaddingLeft, top, right - mPaddingRight,
					bottom);
			mDivider.draw(c);
		}
	}

	public void drawHorizontal(Canvas c, RecyclerView parent) {
		final int top = parent.getPaddingTop();
		final int bottom = parent.getHeight() - parent.getPaddingBottom();

		final int childCount = parent.getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View child = parent.getChildAt(i);
			final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
					.getLayoutParams();
			final int left = child.getRight() + params.rightMargin;
			final int right = left + mDividingLine;
			mDivider.setBounds(left, top, right, bottom);
			mDivider.draw(c);
		}
	}

	@Override
	public void getItemOffsets(Rect outRect, int itemPosition,
			RecyclerView parent) {
		if (mOrientation == VERTICAL_LIST) {
			outRect.set(0, 0, 0, mDividingLine);
		} else {
			outRect.set(0, 0, mDividingLine, 0);
		}
	}
}