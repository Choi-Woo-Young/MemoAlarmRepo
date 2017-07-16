package com.wychoi.success.memoalarm;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.samples.apps.iosched.ui.widget.SlidingTabLayout;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.wychoi.success.memoalarm.alarm.AlarmsFragment;
import com.wychoi.success.memoalarm.memo.MemoListViewFragment;
import com.wychoi.success.memoalarm.memo.MemoRegiterActivity;
import com.wychoi.success.memoalarm.observablescrollview.CacheFragmentStatePagerAdapter;
import com.wychoi.success.memoalarm.observablescrollview.ObservableScrollViewCallbacks;
import com.wychoi.success.memoalarm.observablescrollview.ScrollState;
import com.wychoi.success.memoalarm.observablescrollview.ScrollUtils;
import com.wychoi.success.memoalarm.observablescrollview.Scrollable;
import com.wychoi.success.memoalarm.observablescrollview.TouchInterceptionFrameLayout;
import com.wychoi.success.memoalarm.util.NetworkService;

import static com.wychoi.success.memoalarm.R.id.fab;


public class MainActivity extends AppCompatActivity implements ObservableScrollViewCallbacks {

    private static final String TAG = "MainActivity";
    private TouchInterceptionFrameLayout mInterceptionLayout;
    private View mToolbarView;
    private ViewPager mPager;
    private NavigationAdapter mPagerAdapter;

    private int mSlop;
    private boolean mScrolled;
    private ScrollState mLastScrollState;
    FloatingActionButton mFab;
    private Drawable mAddItemDrawable;

    static public NetworkService networkService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ApplicationController application = ApplicationController.getInstance();
        application.buildNetworkService("192.168.43.217", 8080);
        //application.buildNetworkService("192.168.0.102", 8080);
        //application.buildNetworkService("api.openweathermap.org", 80);
        networkService = ApplicationController.getInstance().getNetworkService();


        //layout 후킹
        setContentView(R.layout.activity_viewpagertab2);
        //액션바 layout 후킹
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        // header의 높이 효과 적용 *Elevation(높이효과)
        ViewCompat.setElevation(findViewById(R.id.header), getResources().getDimension(R.dimen.toolbar_elevation));

        //툴바 layout 후킹
        mToolbarView = findViewById(R.id.toolbar);
        //pager Adapter 생성
        mPagerAdapter = new NavigationAdapter(getSupportFragmentManager());
        //layout에 ViewPager 후킹
        mPager = (ViewPager) findViewById(R.id.pager);
        //pager에 pager Adater 셋팅
        mPager.setAdapter(mPagerAdapter);
        //mPager.addOnPageChangeListener(viewPagerSimpleOnPageChangeListener);

        // Padding for ViewPager must be set outside the ViewPager itself
        // because with padding, EdgeEffect of ViewPager become strange.
        final int tabHeight = getResources().getDimensionPixelSize(R.dimen.tab_height);
        //FrameLayout안에 ViewPager 위치시키고, FrameLayout의 상단 페딩으로 위치 셋팅
        findViewById(R.id.pager_wrapper).setPadding(0, getActionBarSize() + tabHeight, 0, 0);

        //SlidingTabLayout 후킹
        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        //SlidingTabLayout에 customTabView 셋팅
        slidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        //선택된 tab indicator를 표시할 색 셋팅
        slidingTabLayout.setSelectedIndicatorColors(ContextCompat.getColor(this, R.color.accent));
        //slidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.accent));

        //setDistributeEvenly() method is actually needed to make fixed tabs
        slidingTabLayout.setDistributeEvenly(true);
        //slidingTabLayout를 ViewPager와 연결
        slidingTabLayout.setViewPager(mPager);

        //ViewConfiguration: Contains methods to standard constants used in the UI for timeouts, sizes, and distances.
        ViewConfiguration vc = ViewConfiguration.get(this);
        //Distance in pixels a touch can wander before we think the user is scrolling
        //사용자가 스크롤하고 있다고 생각하기 전에 거리가 픽셀 단위로 표시됩니다.
        mSlop = vc.getScaledTouchSlop();
        //최상위 Layout인 TouchInterceptionFrameLayout 후킹
        mInterceptionLayout = (TouchInterceptionFrameLayout) findViewById(R.id.container);
        //TouchInterception 리스너 셋팅
        mInterceptionLayout.setScrollInterceptionListener(mInterceptionListener);
        mFab = (FloatingActionButton) findViewById(fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //알람 Fragment 에서 fab 클릭한 경우
                int idx = mPager.getCurrentItem();
                //if(f instanceof AlarmsFragment){
                if(1 == idx){

                    //Fragment f = mPagerAdapter.getItem(mPager.getCurrentItem());
                    //((AlarmsFragment) f).onFabClick();

                    Snackbar.make(view, "idx:"+ idx, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else if (0 == idx) {

                    Intent intent = new Intent(getApplicationContext(), MemoRegiterActivity.class);
                    //intent.putExtra("MEMO_ITEM_JSONSTR",item.toString()); //이동 엑티비티에 데이터 전달
                    startActivity(intent);
                }
            }
        });

        mAddItemDrawable = ContextCompat.getDrawable(this, R.drawable.shadow);




    }


    ViewPager.SimpleOnPageChangeListener viewPagerSimpleOnPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        /**
         * @param position Either the current page position if the offset is increasing,
         *                 or the previous page position if it is decreasing.
         * @param positionOffset If increasing from [0, 1), scrolling right and position = currentPagePosition
         *                       If decreasing from (1, 0], scrolling left and position = (currentPagePosition - 1)
         */
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            Log.d(TAG, String.format("pos = %d, posOffset = %f, posOffsetPixels = %d",
                    position, positionOffset, positionOffsetPixels));
            int pageBeforeLast = mPagerAdapter.getCount() - 2;
            if (position <= pageBeforeLast) {
                if (position < pageBeforeLast) {
                    // When the scrolling is due to tab selection between multiple tabs apart,
                    // this callback is called for each intermediate page, but each of those pages
                    // will briefly register a sparsely decreasing range of positionOffsets, always
                    // from (1, 0). As such, you would notice the FAB to jump back and forth between
                    // x-positions as each intermediate page is scrolled through.
                    // This is a visual optimization that ends the translation motion, immediately
                    // returning the FAB to its target position.
                    // TODO: The animation visibly skips to the end. We could interpolate
                    // intermediate x-positions if we cared to smooth it out.
                    mFab.setTranslationX(0);
                } else {
                    // Initially, the FAB's translationX property is zero because, at its original
                    // position, it is not translated. setTranslationX() is relative to the view's
                    // left position, at its original position; this left position is taken to be
                    // the zero point of the coordinate system relative to this view. As your
                    // translationX value is increasingly negative, the view is translated left.
                    // But as translationX is decreasingly negative and down to zero, the view
                    // is translated right, back to its original position.
                    float translationX = positionOffsetPixels / -2f;
                    // NOTE: You MUST scale your own additional pixel offsets by positionOffset,
                    // or else the FAB will immediately translate by that many pixels, appearing
                    // to skip/jump.
                    translationX += positionOffset * getFabPixelOffsetForXTranslation();
                    mFab.setTranslationX(translationX);
                }
            }
        }

        @Override
        public void onPageSelected(int position) {
            Log.d(TAG, "onPageSelected");
            if (position < mPagerAdapter.getCount() - 1) {
                mFab.setImageDrawable(mAddItemDrawable);
            }
            //Fragment f = mPagerAdapter.getFragment(mViewPager.getCurrentItem());
            // NOTE: This callback is fired after a rotation, right after onStart().
            // Unfortunately, the FragmentManager handling the rotation has yet to
            // tell our adapter to re-instantiate the Fragments, so our collection
            // of fragments is empty. You MUST keep this check so we don't cause a NPE.
            /*
            if (f instanceof BaseFragment) {
                ((BaseFragment) f).onPageSelected();
            }
            */
        }
    };

    /**
     * @return the positive offset in pixels required to rebase an X-translation of the FAB
     * relative to its center position. An X-translation normally is done relative to a view's
     * left position.
     */
    private float getFabPixelOffsetForXTranslation() {
        final int margin;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Since each side's margin is the same, any side's would do.
            margin = ((ViewGroup.MarginLayoutParams) mFab.getLayoutParams()).rightMargin;
        } else {
            // Pre-Lollipop has measurement issues with FAB margins. This is
            // probably as good as we can get to centering the FAB, without
            // hardcoding some small margin value.
            margin = 0;
        }
        // By adding on half the FAB's width, we effectively rebase the translation
        // relative to the view's center position.
        return mFab.getWidth() / 2f + margin;
    }

    //TouchInterception 리스너
    private TouchInterceptionFrameLayout.TouchInterceptionListener mInterceptionListener = new TouchInterceptionFrameLayout.TouchInterceptionListener() {

        //Check if this event as provided to the parent view's onInterceptTouchEvent should cause the parent to intercept the touch event stream.
        //부모 뷰의 onInterceptTouchEvent에게 제공된 이 이벤트가 부모뷰에 터치 이벤트 스트림을 인터럽트를 발생 시켜야 하는지 체크

        /**
         * Determine whether the layout should intercept this event.
         * @param ev     Motion event.
         * @param moving True if this event is ACTION_MOVE type.
         * @param diffX  Difference between previous X and current X, if moving is true.
         * @param diffY  Difference between previous Y and current Y, if moving is true.
         * @return True if the layout should intercept.
         */
        @Override
        public boolean shouldInterceptTouchEvent(MotionEvent ev, boolean moving, float diffX, float diffY) {
            //Math.abs : 절대값
            if (!mScrolled && mSlop < Math.abs(diffX) && Math.abs(diffY) < Math.abs(diffX)) {
                // Horizontal scroll is maybe handled by ViewPager
                return false;
            }
            //현재 프레임에 포함된 뷰를 찾아서 Scrollable 객체로 리턴
            Scrollable scrollable = getCurrentScrollable();
            if (scrollable == null) {
                mScrolled = false;
                return false;
            }

            // If interceptionLayout can move, it should intercept.
            // And once it begins to move, horizontal scroll shouldn't work any longer.
            int toolbarHeight = mToolbarView.getHeight();

            //이동된 Y??
            int translationY = (int) ViewHelper.getTranslationY(mInterceptionLayout);

            boolean scrollingUp = 0 < diffY;
            boolean scrollingDown = diffY < 0;

            if (scrollingUp) {
                if (translationY < 0) {
                    mScrolled = true;
                    mLastScrollState = ScrollState.UP;
                    return true;
                }
            } else if (scrollingDown) {
                if (-toolbarHeight < translationY) {
                    mScrolled = true;
                    mLastScrollState = ScrollState.DOWN;
                    return true;
                }
            }
            mScrolled = false;
            return false;
        }

        //이벤트 callback 메소드

        /**
         * Called if the down motion event is intercepted by this layout.
         *
         * @param ev Motion event.
         */
        @Override
        public void onDownMotionEvent(MotionEvent ev) {
        }

        /**
         * Called if the move motion event is intercepted by this layout.
         *
         * @param ev    Motion event.
         * @param diffX Difference between previous X and current X.
         * @param diffY Difference between previous Y and current Y.
         */
        @Override
        public void onMoveMotionEvent(MotionEvent ev, float diffX, float diffY) {
            float translationY = ScrollUtils.getFloat(ViewHelper.getTranslationY(mInterceptionLayout) + diffY, -mToolbarView.getHeight(), 0);
            ViewHelper.setTranslationY(mInterceptionLayout, translationY);
            if (translationY < 0) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mInterceptionLayout.getLayoutParams();
                lp.height = (int) (-translationY + getScreenHeight());
                mInterceptionLayout.requestLayout();
            }
        }

        //android.R.id.content 연결되어 있는 XML로 가면 auto_complete_list.xml인데 프로젝트 목록에는 없음.
        private int getScreenHeight() {
            return findViewById(android.R.id.content).getHeight();
        }

        /**
         * Called if the up (or cancel) motion event is intercepted by this layout.
         *
         * @param ev Motion event.
         */
        @Override
        public void onUpOrCancelMotionEvent(MotionEvent ev) {
            mScrolled = false;
            adjustToolbar(mLastScrollState);
        }
    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //ObservableScrollViewCallbacks
    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

    }

    @Override
    public void onDownMotionEvent() {
    }


    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {//1
        if (!mScrolled) {
            // This event can be used only when TouchInterceptionFrameLayout
            // doesn't handle the consecutive events.
            adjustToolbar(scrollState);
        }
    }

    //옵션 메뉴
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /////////////////////////////////////////////////////////////////////////////////////


    //현재 프레임에 포함된 뷰를 찾아서 Scrollable 객체로 리턴
    private Scrollable getCurrentScrollable() {
        Fragment fragment = getCurrentFragment();
        if (fragment == null) {
            return null;
        }
        View view = fragment.getView();
        if (view == null) {
            return null;
        }
        //Interface for providing common API for observable and scrollable widgets.
        return (Scrollable) view.findViewById(R.id.scroll);
    }

    //엑션바 픽셀 사이즈 리턴
    int getActionBarSize() {
        TypedValue typedValue = new TypedValue();
        int[] textSizeAttr = new int[]{R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = obtainStyledAttributes(typedValue.data, textSizeAttr);
        int actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        return actionBarSize;
    }


    private void animateToolbar(final float toY) {
        float layoutTranslationY = ViewHelper.getTranslationY(mInterceptionLayout);
        if (layoutTranslationY != toY) {
            ValueAnimator animator = ValueAnimator.ofFloat(ViewHelper.getTranslationY(mInterceptionLayout), toY).setDuration(200);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float translationY = (float) animation.getAnimatedValue();
                    ViewHelper.setTranslationY(mInterceptionLayout, translationY);
                    if (translationY < 0) {
                        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mInterceptionLayout.getLayoutParams();

                        lp.height = (int) (-translationY + findViewById(android.R.id.content).getHeight());
                        //lp.height = (int) (-translationY + getSscreenHeight());

                        mInterceptionLayout.requestLayout();
                    }
                }
            });
            animator.start();
        }
    }


    private boolean toolbarIsHidden() {
        return ViewHelper.getTranslationY(mInterceptionLayout) == -mToolbarView.getHeight();
    }

    private Fragment getCurrentFragment() {
        return mPagerAdapter.getItemAt(mPager.getCurrentItem());
    }

    private boolean toolbarIsShown() {
        return ViewHelper.getTranslationY(mInterceptionLayout) == 0;
    }

    private void showToolbar() {
        animateToolbar(0);
    }

    private void hideToolbar() {
        animateToolbar(-mToolbarView.getHeight());
    }


    private void adjustToolbar(ScrollState scrollState) {
        int toolbarHeight = mToolbarView.getHeight();
        final Scrollable scrollable = getCurrentScrollable();
        if (scrollable == null) {
            return;
        }
        int scrollY = scrollable.getCurrentScrollY();
        if (scrollState == ScrollState.DOWN) { //////////
            showToolbar();
        } else if (scrollState == ScrollState.UP) {
            if (toolbarHeight <= scrollY) {
                hideToolbar();
            } else {
                showToolbar();
            }
        } else if (!toolbarIsShown() && !toolbarIsHidden()) {
            // Toolbar is moving but doesn't know which to move:
            // you can change this to hideToolbar()
            showToolbar();
        }
    }

    /**
     * This adapter provides two types of fragments as an example.
     * {@linkplain #createItem(int)} should be modified if you use this example for your app.
     */
    private static class NavigationAdapter extends CacheFragmentStatePagerAdapter {

        private static final String[] TITLES = new String[]{"메모", "알람", "분석"};

        private NavigationAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        protected Fragment createItem(int position) {
            Fragment f;
            final int pattern = position % 5;
            switch (pattern) {
                case 0:
                    f = new MemoListViewFragment();
                    break;
                case 1:
                    f = new AlarmsFragment();
                    break;
                case 2:
                    f = new ViewPagerTab2ScrollViewFragment();
                    break;
                case 3:
                    f = new ViewPagerTab2GridViewFragment();
                    break;
                case 4:
                default:
                    f = new ViewPagerTab2WebViewFragment();
                    break;
            }
            return f;
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }
    }

}
