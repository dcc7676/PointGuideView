package com.classroom100.android.view;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.class100.lib.base.DimenUtil;
import com.classroom100.android.R;

/**
 * Created by vicy on 17-11-20.
 */

public class GuidePointView {
    private RelativeLayout mContentRoot;

    private View mOriView;
    private ViewGroup mOriViewParent;
    private View mReplaceView;
    private View mTipView;

    private Activity mActivity;

    private OnDismissListener mListener;

    public GuidePointView(@NonNull Activity activity) {
        mActivity = activity;
        mContentRoot = (RelativeLayout) LayoutInflater.from(activity).inflate(R.layout.dialog_view_guide, null);
        mContentRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void setListener(OnDismissListener listener){
        mListener = listener;
    }

    public final void prepare(View view) {
        if(view == null || !(view.getParent() instanceof ViewGroup)){
            return;
        }
        reset();

        mOriView = view;
        mOriViewParent = (ViewGroup) mOriView.getParent();
        mReplaceView = new View(mActivity);
        mReplaceView.setVisibility(View.INVISIBLE);
    }

    public final void setTipOnTop(@NonNull View tipView, @NonNull View baseView, int margin){
        RelativeLayout.LayoutParams lp;
        lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_BOTTOM, baseView.getId());
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lp.bottomMargin = baseView.getHeight() + DimenUtil.dip2px(mActivity, margin);
        tipView.setLayoutParams(lp);
        mTipView = tipView;
    }

    public final void show() {
        if(mOriView == null || mOriViewParent == null || mOriView.getParent() != mOriViewParent
                || mReplaceView == null || mActivity.getWindow() == null){
            return;
        }
        int index = mOriViewParent.indexOfChild(mOriView);
        if(index < 0){
            return;
        }
        ViewGroup.LayoutParams oriLp = mOriView.getLayoutParams();
        oriLp.width = mOriView.getWidth();
        oriLp.height = mOriView.getHeight();
        RelativeLayout.LayoutParams replaceLp = new RelativeLayout.LayoutParams(oriLp.width, oriLp.height);
        int[] location = new int[2];
        mOriView.getLocationInWindow(location);
        replaceLp.leftMargin = location[0];
        replaceLp.topMargin = location[1];
        mReplaceView.setId(mOriView.getId());

        mOriViewParent.removeViewAt(index);
        removeFromParent(mReplaceView);
        mOriViewParent.addView(mReplaceView, index, oriLp);
        mContentRoot.addView(mOriView, replaceLp);

        if(mTipView != null) {
            removeFromParent(mTipView);
            mContentRoot.addView(mTipView);
        }

        View decorView = mActivity.getWindow().getDecorView();
        if(decorView instanceof ViewGroup){
            removeFromParent(mContentRoot);
            ((ViewGroup) decorView).addView(mContentRoot);
            mContentRoot.bringToFront();
        }
    }

    private void removeFromParent(@NonNull View view){
        if(view.getParent() != null){
            ((ViewGroup)view.getParent()).removeView(view);
        }
    }

    public void dismiss(){
        if(mReplaceView == null || mOriViewParent == null || mReplaceView.getParent() != mOriViewParent
                || mOriView == null){
            return;
        }
        int index = mOriViewParent.indexOfChild(mReplaceView);
        if(index < 0){
            return;
        }
        ViewGroup.LayoutParams oriLp = mReplaceView.getLayoutParams();

        mOriViewParent.removeViewAt(index);
        removeFromParent(mOriView);
        mOriViewParent.addView(mOriView, index, oriLp);

        if(mTipView != null) {
            removeFromParent(mTipView);
        }

        removeFromParent(mContentRoot);

        if(mListener != null){
            mListener.onDismiss();
        }
    }

    public final void reset(){
        dismiss();
        mContentRoot.removeAllViews();
        mTipView = null;
        mOriView = null;
        mOriViewParent = null;
        mReplaceView = null;
    }

    public interface OnDismissListener {
        void onDismiss();
    }
}
