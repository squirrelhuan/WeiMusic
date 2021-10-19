/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.demomaster.weimusic.view.appbar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.math.MathUtils;
import androidx.core.util.ObjectsCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.R;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.internal.CollapsingTextHelper;
import com.google.android.material.internal.DescendantOffsetUtils;
import com.google.android.material.internal.ThemeEnforcement;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import cn.demomaster.qdlogger_library.QDLogger;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

/**
 * CollapsingToolbarLayout is a wrapper for {@link Toolbar} which implements a collapsing app bar.
 * It is designed to be used as a direct child of a {@link AppBarLayout}. CollapsingToolbarLayout
 * contains the following features:
 *
 * <h4>Collapsing title</h4>
 *
 * A title which is larger when the layout is fully visible but collapses and becomes smaller as the
 * layout is scrolled off screen. You can set the title to display via {@link
 * #setTitle(CharSequence)}. The title appearance can be tweaked via the {@code
 * collapsedTextAppearance} and {@code expandedTextAppearance} attributes.
 *
 * <h4>Content scrim</h4>
 *
 * A full-bleed scrim which is show or hidden when the scroll position has hit a certain threshold.
 * You can change this via {@link #setContentScrim(Drawable)}.
 *
 * <h4>Status bar scrim</h4>
 *
 * A scrim which is shown or hidden behind the status bar when the scroll position has hit a certain
 * threshold. You can change this via {@link #setStatusBarScrim(Drawable)}. This only works on
 * {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} devices when we set to fit system
 * windows.
 *
 * <h4>Parallax scrolling children</h4>
 *
 * Child views can opt to be scrolled within this layout in a parallax fashion. See {@link
 * LayoutParams#COLLAPSE_MODE_PARALLAX} and {@link LayoutParams#setParallaxMultiplier(float)}.
 *
 * <h4>Pinned position children</h4>
 *
 * Child views can opt to be pinned in space globally. This is useful when implementing a collapsing
 * as it allows the {@link Toolbar} to be fixed in place even though this layout is moving. See
 * {@link LayoutParams#COLLAPSE_MODE_PIN}.
 *
 * <p><strong>Do not manually add views to the Toolbar at run time</strong>. We will add a 'dummy
 * view' to the Toolbar which allows us to work out the available space for the title. This can
 * interfere with any views which you add.
 *
 * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_collapsedTitleTextAppearance
 * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleTextAppearance
 * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_contentScrim
 * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMargin
 * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMarginStart
 * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMarginEnd
 * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMarginBottom
 * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_statusBarScrim
 * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_toolbarId
 */
public class CollapsingLayout extends FrameLayout {

  private static final int DEF_STYLE_RES = R.style.Widget_Design_CollapsingToolbar;
  private static final int DEFAULT_SCRIM_ANIMATION_DURATION = 600;

  private boolean refreshToolbar = true;
  private int layoutId;
  @Nullable private View toolbarView;

  @Nullable private Drawable contentScrim;
  @Nullable Drawable statusBarScrim;
  private int scrimAlpha;
  private boolean scrimsAreShown;
  private ValueAnimator scrimAnimator;
  private long scrimAnimationDuration;
  private int scrimVisibleHeightTrigger = -1;

  private AppBarLayout.OnOffsetChangedListener onOffsetChangedListener;

  int currentOffset;

  @Nullable WindowInsetsCompat lastInsets;

  public CollapsingLayout(@NonNull Context context) {
    this(context, null);
  }

  public CollapsingLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.collapsingToolbarLayoutStyle);
  }

  public CollapsingLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();


    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.CollapsingToolbarLayout,
            defStyleAttr,
            DEF_STYLE_RES);

    scrimVisibleHeightTrigger =
        a.getDimensionPixelSize(R.styleable.CollapsingToolbarLayout_scrimVisibleHeightTrigger, -1);

    scrimAnimationDuration =
        a.getInt(
            R.styleable.CollapsingToolbarLayout_scrimAnimationDuration,
            DEFAULT_SCRIM_ANIMATION_DURATION);

    setContentScrim(a.getDrawable(R.styleable.CollapsingToolbarLayout_contentScrim));
    setStatusBarScrim(a.getDrawable(R.styleable.CollapsingToolbarLayout_statusBarScrim));

    layoutId = a.getResourceId(R.styleable.CollapsingToolbarLayout_toolbarId, -1);

    a.recycle();

    setWillNotDraw(false);

    ViewCompat.setOnApplyWindowInsetsListener(
        this,
        new androidx.core.view.OnApplyWindowInsetsListener() {
          @Override
          public WindowInsetsCompat onApplyWindowInsets(
              View v, @NonNull WindowInsetsCompat insets) {
            return onWindowInsetChanged(insets);
          }
        });
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    // Add an OnOffsetChangedListener if possible
    final ViewParent parent = getParent();
    if (parent instanceof AppBarLayout) {
      // Copy over from the ABL whether we should fit system windows
      ViewCompat.setFitsSystemWindows(this, ViewCompat.getFitsSystemWindows((View) parent));

      if (onOffsetChangedListener == null) {
        onOffsetChangedListener = new OffsetUpdateListener();
      }
      ((AppBarLayout) parent).addOnOffsetChangedListener(onOffsetChangedListener);

      // We're attached, so lets request an inset dispatch
      ViewCompat.requestApplyInsets(this);
    }
  }

  AppBarLayout.OnOffsetChangedListener onOffsetChangedListener2;

  public void setOnOffsetChangedListener( AppBarLayout.OnOffsetChangedListener onOffsetChangedListener) {
    this.onOffsetChangedListener2 = onOffsetChangedListener;
  }

  @Override
  protected void onDetachedFromWindow() {
    // Remove our OnOffsetChangedListener if possible and it exists
    final ViewParent parent = getParent();
    if (onOffsetChangedListener != null && parent instanceof AppBarLayout) {
      ((AppBarLayout) parent).removeOnOffsetChangedListener(onOffsetChangedListener);
    }

    super.onDetachedFromWindow();
  }

  WindowInsetsCompat onWindowInsetChanged(@NonNull final WindowInsetsCompat insets) {
    WindowInsetsCompat newInsets = null;

    if (ViewCompat.getFitsSystemWindows(this)) {
      // If we're set to fit system windows, keep the insets
      newInsets = insets;
    }

    // If our insets have changed, keep them and invalidate the scroll ranges...
    if (!ObjectsCompat.equals(lastInsets, newInsets)) {
      lastInsets = newInsets;
      requestLayout();
    }

    // Consume the insets. This is done so that child views with fitSystemWindows=true do not
    // get the default padding functionality from View
    return insets.consumeSystemWindowInsets();
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    super.draw(canvas);

    // If we don't have a toolbar, the scrim will be not be drawn in drawChild() below.
    // Instead, we draw it here, before our collapsing text.
    ensureToolbar();
    if (toolbarView == null && contentScrim != null && scrimAlpha > 0) {
      contentScrim.mutate().setAlpha(scrimAlpha);
      contentScrim.draw(canvas);
    }

    // Now draw the status bar scrim
    if (statusBarScrim != null && scrimAlpha > 0) {
      final int topInset = toolbarView.getWidth();//lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;
      if (topInset > 0) {
        statusBarScrim.setBounds(0, -currentOffset, getWidth(), topInset - currentOffset);
        statusBarScrim.mutate().setAlpha(scrimAlpha);
        statusBarScrim.draw(canvas);
      }
    }
  }

  @Override
  protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
    // This is a little weird. Our scrim needs to be behind the Toolbar (if it is present),
    // but in front of any other children which are behind it. To do this we intercept the
    // drawChild() call, and draw our scrim just before the Toolbar is drawn
    boolean invalidated = false;
    if (contentScrim != null && scrimAlpha > 0 && isToolbarChild(child)) {
      contentScrim.mutate().setAlpha(scrimAlpha);
      contentScrim.draw(canvas);
      invalidated = true;
    }
    return super.drawChild(canvas, child, drawingTime) || invalidated;
  }

  private boolean isToolbarChild(View child) {
    return true;
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    if (contentScrim != null) {
      contentScrim.setBounds(0, 0, w, h);
    }
  }

  private void ensureToolbar() {
    if (!refreshToolbar) {
      return;
    }

    // First clear out the current Toolbar
    this.toolbarView = null;

    if (layoutId != -1) {
      // If we have an ID set, try and find it and it's direct parent to us
      this.toolbarView = findViewById(layoutId);
      setMinimumHeight(toolbarView.getMeasuredHeight());
    }
    refreshToolbar = false;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    ensureToolbar();
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    final int mode = MeasureSpec.getMode(heightMeasureSpec);
    final int topInset = 0;//lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;
    if (mode == MeasureSpec.UNSPECIFIED && topInset > 0) {
      // If we have a top inset and we're set to wrap_content height we need to make sure
      // we add the top inset to our height, therefore we re-measure
      heightMeasureSpec =
          MeasureSpec.makeMeasureSpec(getMeasuredHeight() + topInset, MeasureSpec.EXACTLY);
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    //setMinimumHeight 控制最小高度
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    if (lastInsets != null) {
      // Shift down any views which are not set to fit system windows
      final int insetTop = lastInsets.getSystemWindowInsetTop();
      for (int i = 0, z = getChildCount(); i < z; i++) {
        final View child = getChildAt(i);
        if (!ViewCompat.getFitsSystemWindows(child)) {
          if (child.getTop() < insetTop) {
            // If the child isn't set to fit system windows but is drawing within
            // the inset offset it down
            ViewCompat.offsetTopAndBottom(child, insetTop);
          }
        }
      }
    }

    // Update our child view offset helpers so that they track the correct layout coordinates
    for (int i = 0, z = getChildCount(); i < z; i++) {
      getViewOffsetHelper(getChildAt(i)).onViewLayout();
    }
    updateScrimVisibility();

    // Apply any view offsets, this should be done at the very end of layout
    for (int i = 0, z = getChildCount(); i < z; i++) {
      getViewOffsetHelper(getChildAt(i)).applyOffsets();
    }
  }

  private static int getHeightWithMargins(@NonNull final View view) {
    final ViewGroup.LayoutParams lp = view.getLayoutParams();
    if (lp instanceof MarginLayoutParams) {
      final MarginLayoutParams mlp = (MarginLayoutParams) lp;
      return view.getMeasuredHeight() + mlp.topMargin + mlp.bottomMargin;
    }
    return view.getMeasuredHeight();
  }

  @NonNull
  static ViewOffsetHelper getViewOffsetHelper(@NonNull View view) {
    ViewOffsetHelper offsetHelper = (ViewOffsetHelper) view.getTag(R.id.view_offset_helper);
    if (offsetHelper == null) {
      offsetHelper = new ViewOffsetHelper(view);
      view.setTag(R.id.view_offset_helper, offsetHelper);
    }
    return offsetHelper;
  }

  /**
   * Set whether the content scrim and/or status bar scrim should be shown or not. Any change in the
   * vertical scroll may overwrite this value. Any visibility change will be animated if this view
   * has already been laid out.
   *
   * @param shown whether the scrims should be shown
   * @see #getStatusBarScrim()
   * @see #getContentScrim()
   */
  public void setScrimsShown(boolean shown) {
    setScrimsShown(shown, ViewCompat.isLaidOut(this) && !isInEditMode());
  }

  /**
   * Set whether the content scrim and/or status bar scrim should be shown or not. Any change in the
   * vertical scroll may overwrite this value.
   *
   * @param shown whether the scrims should be shown
   * @param animate whether to animate the visibility change
   * @see #getStatusBarScrim()
   * @see #getContentScrim()
   */
  public void setScrimsShown(boolean shown, boolean animate) {
    if (scrimsAreShown != shown) {
      if (animate) {
        animateScrim(shown ? 0xFF : 0x0);
      } else {
        setScrimAlpha(shown ? 0xFF : 0x0);
      }
      scrimsAreShown = shown;
    }
  }

  private void animateScrim(int targetAlpha) {
    ensureToolbar();
    if (scrimAnimator == null) {
      scrimAnimator = new ValueAnimator();
      scrimAnimator.setDuration(scrimAnimationDuration);
      scrimAnimator.setInterpolator(
          targetAlpha > scrimAlpha
              ? AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR
              : AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR);
      scrimAnimator.addUpdateListener(
          new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animator) {
              setScrimAlpha((int) animator.getAnimatedValue());
            }
          });
    } else if (scrimAnimator.isRunning()) {
      scrimAnimator.cancel();
    }

    scrimAnimator.setIntValues(scrimAlpha, targetAlpha);
    scrimAnimator.start();
  }

  void setScrimAlpha(int alpha) {
    if (alpha != scrimAlpha) {
      final Drawable contentScrim = this.contentScrim;
      if (contentScrim != null && toolbarView != null) {
        ViewCompat.postInvalidateOnAnimation(toolbarView);
      }
      scrimAlpha = alpha;
      ViewCompat.postInvalidateOnAnimation(CollapsingLayout.this);
    }
  }

  int getScrimAlpha() {
    return scrimAlpha;
  }

  /**
   * Set the drawable to use for the content scrim from resources. Providing null will disable the
   * scrim functionality.
   *
   * @param drawable the drawable to display
   * @attr ref R.styleable#CollapsingToolbarLayout_contentScrim
   * @see #getContentScrim()
   */
  public void setContentScrim(@Nullable Drawable drawable) {
    if (contentScrim != drawable) {
      if (contentScrim != null) {
        contentScrim.setCallback(null);
      }
      contentScrim = drawable != null ? drawable.mutate() : null;
      if (contentScrim != null) {
        contentScrim.setBounds(0, 0, getWidth(), getHeight());
        contentScrim.setCallback(this);
        contentScrim.setAlpha(scrimAlpha);
      }
      ViewCompat.postInvalidateOnAnimation(this);
    }
  }

  /**
   * Set the color to use for the content scrim.
   *
   * @param color the color to display
   * @attr ref R.styleable#CollapsingToolbarLayout_contentScrim
   * @see #getContentScrim()
   */
  public void setContentScrimColor(@ColorInt int color) {
    setContentScrim(new ColorDrawable(color));
  }

  /**
   * Set the drawable to use for the content scrim from resources.
   *
   * @param resId drawable resource id
   * @attr ref R.styleable#CollapsingToolbarLayout_contentScrim
   * @see #getContentScrim()
   */
  public void setContentScrimResource(@DrawableRes int resId) {
    setContentScrim(ContextCompat.getDrawable(getContext(), resId));
  }

  /**
   * Returns the drawable which is used for the foreground scrim.
   *
   * @attr ref R.styleable#CollapsingToolbarLayout_contentScrim
   * @see #setContentScrim(Drawable)
   */
  @Nullable
  public Drawable getContentScrim() {
    return contentScrim;
  }

  /**
   * Set the drawable to use for the status bar scrim from resources. Providing null will disable
   * the scrim functionality.
   *
   * <p>This scrim is only shown when we have been given a top system inset.
   *
   * @param drawable the drawable to display
   * @attr ref R.styleable#CollapsingToolbarLayout_statusBarScrim
   * @see #getStatusBarScrim()
   */
  public void setStatusBarScrim(@Nullable Drawable drawable) {
    if (statusBarScrim != drawable) {
      if (statusBarScrim != null) {
        statusBarScrim.setCallback(null);
      }
      statusBarScrim = drawable != null ? drawable.mutate() : null;
      if (statusBarScrim != null) {
        if (statusBarScrim.isStateful()) {
          statusBarScrim.setState(getDrawableState());
        }
        DrawableCompat.setLayoutDirection(statusBarScrim, ViewCompat.getLayoutDirection(this));
        statusBarScrim.setVisible(getVisibility() == VISIBLE, false);
        statusBarScrim.setCallback(this);
        statusBarScrim.setAlpha(scrimAlpha);
      }
      ViewCompat.postInvalidateOnAnimation(this);
    }
  }

  @Override
  protected void drawableStateChanged() {
    super.drawableStateChanged();

    final int[] state = getDrawableState();
    boolean changed = false;

    Drawable d = statusBarScrim;
    if (d != null && d.isStateful()) {
      changed |= d.setState(state);
    }
    d = contentScrim;
    if (d != null && d.isStateful()) {
      changed |= d.setState(state);
    }
    if (changed) {
      invalidate();
    }
  }

  @Override
  protected boolean verifyDrawable(@NonNull Drawable who) {
    return super.verifyDrawable(who) || who == contentScrim || who == statusBarScrim;
  }

  @Override
  public void setVisibility(int visibility) {
    super.setVisibility(visibility);

    final boolean visible = visibility == VISIBLE;
    if (statusBarScrim != null && statusBarScrim.isVisible() != visible) {
      statusBarScrim.setVisible(visible, false);
    }
    if (contentScrim != null && contentScrim.isVisible() != visible) {
      contentScrim.setVisible(visible, false);
    }
  }

  /**
   * Set the color to use for the status bar scrim.
   *
   * <p>This scrim is only shown when we have been given a top system inset.
   *
   * @param color the color to display
   * @attr ref R.styleable#CollapsingToolbarLayout_statusBarScrim
   * @see #getStatusBarScrim()
   */
  public void setStatusBarScrimColor(@ColorInt int color) {
    setStatusBarScrim(new ColorDrawable(color));
  }

  /**
   * Set the drawable to use for the status bar scrim from resources.
   *
   * @param resId drawable resource id
   * @attr ref R.styleable#CollapsingToolbarLayout_statusBarScrim
   * @see #getStatusBarScrim()
   */
  public void setStatusBarScrimResource(@DrawableRes int resId) {
    setStatusBarScrim(ContextCompat.getDrawable(getContext(), resId));
  }

  /**
   * Returns the drawable which is used for the status bar scrim.
   *
   * @attr ref R.styleable#CollapsingToolbarLayout_statusBarScrim
   * @see #setStatusBarScrim(Drawable)
   */
  @Nullable
  public Drawable getStatusBarScrim() {
    return statusBarScrim;
  }

  /**
   * Set the amount of visible height in pixels used to define when to trigger a scrim visibility
   * change.
   *
   * <p>If the visible height of this view is less than the given value, the scrims will be made
   * visible, otherwise they are hidden.
   *
   * @param height value in pixels used to define when to trigger a scrim visibility change
   * @attr ref
   *     com.google.android.material.R.styleable#CollapsingToolbarLayout_scrimVisibleHeightTrigger
   */
  public void setScrimVisibleHeightTrigger(@IntRange(from = 0) final int height) {
    if (scrimVisibleHeightTrigger != height) {
      scrimVisibleHeightTrigger = height;
      // Update the scrim visibility
      updateScrimVisibility();
    }
  }

  /**
   * Returns the amount of visible height in pixels used to define when to trigger a scrim
   * visibility change.
   *
   * @see #setScrimVisibleHeightTrigger(int)
   */
  public int getScrimVisibleHeightTrigger() {
    if (scrimVisibleHeightTrigger >= 0) {
      // If we have one explicitly set, return it
      return scrimVisibleHeightTrigger;
    }
    if(toolbarView!=null){
      return toolbarView.getMeasuredHeight();
    }

    // Otherwise we'll use the default computed value
    final int insetTop = lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;

    final int minHeight = ViewCompat.getMinimumHeight(this);
    if (minHeight > 0) {
      // If we have a minHeight set, lets use 2 * minHeight (capped at our height)
      return Math.min((minHeight * 2) + insetTop, getHeight());
    }

    // If we reach here then we don't have a min height set. Instead we'll take a
    // guess at 1/3 of our height being visible
    return getHeight() / 3;
  }

  /**
   * Set the duration used for scrim visibility animations.
   *
   * @param duration the duration to use in milliseconds
   * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_scrimAnimationDuration
   */
  public void setScrimAnimationDuration(@IntRange(from = 0) final long duration) {
    scrimAnimationDuration = duration;
  }

  /** Returns the duration in milliseconds used for scrim visibility animations. */
  public long getScrimAnimationDuration() {
    return scrimAnimationDuration;
  }

  @Override
  protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
    return p instanceof LayoutParams;
  }

  @Override
  protected LayoutParams generateDefaultLayoutParams() {
    return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
  }

  @Override
  public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new LayoutParams(getContext(), attrs);
  }

  @Override
  protected FrameLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
    return new LayoutParams(p);
  }

  public static class LayoutParams extends FrameLayout.LayoutParams {

    private static final float DEFAULT_PARALLAX_MULTIPLIER = 0.5f;

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    @IntDef({COLLAPSE_MODE_OFF, COLLAPSE_MODE_PIN, COLLAPSE_MODE_PARALLAX})
    @Retention(RetentionPolicy.SOURCE)
    @interface CollapseMode {}

    /** The view will act as normal with no collapsing behavior. */
    public static final int COLLAPSE_MODE_OFF = 0;

    /**
     * The view will pin in place until it reaches the bottom of the {@link
     * CollapsingLayout}.
     */
    public static final int COLLAPSE_MODE_PIN = 1;

    /**
     * The view will scroll in a parallax fashion. See {@link #setParallaxMultiplier(float)} to
     * change the multiplier used.
     */
    public static final int COLLAPSE_MODE_PARALLAX = 2;

    int collapseMode = COLLAPSE_MODE_OFF;
    float parallaxMult = DEFAULT_PARALLAX_MULTIPLIER;

    public LayoutParams(Context c, AttributeSet attrs) {
      super(c, attrs);

      TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.CollapsingToolbarLayout_Layout);
      collapseMode =
          a.getInt(
              R.styleable.CollapsingToolbarLayout_Layout_layout_collapseMode, COLLAPSE_MODE_OFF);
      setParallaxMultiplier(
          a.getFloat(
              R.styleable.CollapsingToolbarLayout_Layout_layout_collapseParallaxMultiplier,
              DEFAULT_PARALLAX_MULTIPLIER));
      a.recycle();
    }

    public LayoutParams(int width, int height) {
      super(width, height);
    }

    public LayoutParams(int width, int height, int gravity) {
      super(width, height, gravity);
    }

    public LayoutParams(@NonNull ViewGroup.LayoutParams p) {
      super(p);
    }

    public LayoutParams(@NonNull MarginLayoutParams source) {
      super(source);
    }

    @RequiresApi(19)
    public LayoutParams(@NonNull FrameLayout.LayoutParams source) {
      // The copy constructor called here only exists on API 19+.
      super(source);
    }

    /**
     * Set the collapse mode.
     *
     * @param collapseMode one of {@link #COLLAPSE_MODE_OFF}, {@link #COLLAPSE_MODE_PIN} or {@link
     *     #COLLAPSE_MODE_PARALLAX}.
     */
    public void setCollapseMode(@CollapseMode int collapseMode) {
      this.collapseMode = collapseMode;
    }

    /**
     * Returns the requested collapse mode.
     *
     * @return the current mode. One of {@link #COLLAPSE_MODE_OFF}, {@link #COLLAPSE_MODE_PIN} or
     *     {@link #COLLAPSE_MODE_PARALLAX}.
     */
    @CollapseMode
    public int getCollapseMode() {
      return collapseMode;
    }

    /**
     * Set the parallax scroll multiplier used in conjunction with {@link #COLLAPSE_MODE_PARALLAX}.
     * A value of {@code 0.0} indicates no movement at all, {@code 1.0f} indicates normal scroll
     * movement.
     *
     * @param multiplier the multiplier.
     * @see #getParallaxMultiplier()
     */
    public void setParallaxMultiplier(float multiplier) {
      parallaxMult = multiplier;
    }

    /**
     * Returns the parallax scroll multiplier used in conjunction with {@link
     * #COLLAPSE_MODE_PARALLAX}.
     *
     * @see #setParallaxMultiplier(float)
     */
    public float getParallaxMultiplier() {
      return parallaxMult;
    }
  }

  /** Show or hide the scrims if needed */
  final void updateScrimVisibility() {
    if (contentScrim != null || statusBarScrim != null) {
      setScrimsShown(getHeight() + currentOffset < getScrimVisibleHeightTrigger());
    }
  }

  final int getMaxOffsetForPinChild(@NonNull View child) {
    final ViewOffsetHelper offsetHelper = getViewOffsetHelper(child);
    final LayoutParams lp = (LayoutParams) child.getLayoutParams();
    int top = offsetHelper.getLayoutTop();
    int childHeight = child.getHeight();
    int height = getHeight();
    return height - top - childHeight - lp.bottomMargin;
  }

  private class OffsetUpdateListener implements AppBarLayout.OnOffsetChangedListener {
    OffsetUpdateListener() {}

    @Override
    public void onOffsetChanged(AppBarLayout layout, int verticalOffset) {
      currentOffset = verticalOffset;
      QDLogger.i("currentOffset="+currentOffset);
      if(onOffsetChangedListener2!=null){
        onOffsetChangedListener2.onOffsetChanged(layout,verticalOffset);
      }
      final int insetTop = lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;

      for (int i = 0, z = getChildCount(); i < z; i++) {
        final View child = getChildAt(i);
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        final ViewOffsetHelper offsetHelper = getViewOffsetHelper(child);

        switch (lp.collapseMode) {
          case LayoutParams.COLLAPSE_MODE_PIN:
            int max= getMaxOffsetForPinChild(child);
            offsetHelper.setTopAndBottomOffset(
                MathUtils.clamp(-verticalOffset, 0, max));
            break;
          case LayoutParams.COLLAPSE_MODE_PARALLAX:
            offsetHelper.setTopAndBottomOffset(Math.round(-verticalOffset * lp.parallaxMult));
            break;
          default:
            break;
        }
      }

      // Show or hide the scrims if needed
      updateScrimVisibility();

      if (statusBarScrim != null && insetTop > 0) {
        ViewCompat.postInvalidateOnAnimation(CollapsingLayout.this);
      }
      // Update the collapsing text's fraction
      /*final int expandRange =
          getHeight() - ViewCompat.getMinimumHeight(CollapsingLayout.this) - insetTop;
      collapsingTextHelper.setExpansionFraction(Math.abs(verticalOffset) / (float) expandRange);*/
    }
  }
}
