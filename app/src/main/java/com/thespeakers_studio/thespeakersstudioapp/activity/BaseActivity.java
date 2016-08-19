package com.thespeakers_studio.thespeakersstudioapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.data.PresentationDbHelper;
import com.thespeakers_studio.thespeakersstudioapp.settings.SettingsActivity;
import com.thespeakers_studio.thespeakersstudioapp.settings.SettingsUtils;
import com.thespeakers_studio.thespeakersstudioapp.ui.NavDrawerItemView;
import com.thespeakers_studio.thespeakersstudioapp.ui.ScrimInsetsScrollView;
import com.thespeakers_studio.thespeakersstudioapp.ui.SmartScrollView;
import com.thespeakers_studio.thespeakersstudioapp.ui.ToolbarShadowFrameLayout;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import java.util.ArrayList;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGD;
import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGE;
import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGW;
import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.makeLogTag;

/**
 * Created by smcgi_000 on 8/1/2016.
 */
public abstract class BaseActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        SmartScrollView.Callbacks {

    protected static final String TAG = makeLogTag(BaseActivity.class);

    private static final int SELECT_GOOGLE_ACCOUNT_RESULT = 9999;

    private Handler mHandler;
    protected PresentationDbHelper mDbHelper;

    protected Toolbar mActionBarToolbar;
    protected DrawerLayout mDrawerLayout;
    protected ActionBarDrawerToggle mDrawerToggle;

    protected static final int NAVDRAWER_ITEM_INVALID = -1;
    protected static final int NAVDRAWER_ITEM_SEPARATOR = -2;
    protected static final int NAVDRAWER_ITEM_SEPARATOR_SPECIAL = -3;
    protected static final int NAVDRAWER_ITEM_PRESENTATIONS = 0;
    protected static final int NAVDRAWER_ITEM_TIMER = 1;
    protected static final int NAVDRAWER_ITEM_STORIES = 2;
    protected static final int NAVDRAWER_ITEM_CURIOUS = 3;
    protected static final int NAVDRAWER_ITEM_SETTINGS = 4;
    protected static final int NAVDRAWER_ITEM_ABOUT = 5;

    // list of navdrawer items that were actually added to the navdrawer, in order
    private ArrayList<Integer> mNavDrawerItems = new ArrayList<Integer>();

    // views that correspond to each navdrawer item, null if not yet created
    private View[] mNavDrawerItemViews = null;

    private ViewGroup mDrawerItemsListContainer;

    // indices of these things should match up with static values above
    private static final int[] NAVDRAWER_TITLE_RES_ID = new int[] {
            R.string.presentations,
            R.string.timer,
            R.string.stories,
            R.string.curious,
            R.string.action_settings,
            R.string.about
    };

    private static final int[] NAVDRAWER_ICON_RES_ID = new int[] {
            R.drawable.ic_record_voice_over_white_24dp,
            R.drawable.ic_alarm_white_24dp,
            R.drawable.ic_chrome_reader_mode_white_24dp,
            R.drawable.ic_help_white_24dp,
            R.drawable.ic_settings_white_24dp,
            R.drawable.ic_info_white_24dp
    };

    protected View mHeaderBar;
    protected View mHeaderDetails;
    protected int mHeaderHeightPixels = -1;
    protected int mHeaderDetailsHeightPixels = -1;
    protected float mMaxHeaderElevation;

    protected Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeId = getIntent().getIntExtra(Utils.INTENT_THEME_ID, 0);
        if (themeId > 0) {
            setTheme(themeId);
        }

        super.onCreate(savedInstanceState);

        mHandler = new Handler();
        mDbHelper = new PresentationDbHelper(getApplicationContext());

        mMaxHeaderElevation = getResources().getDimensionPixelSize(R.dimen.max_header_elevation);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);
    }

    private void setupNavDrawer() {
        // what item is selected
        int selfItem = getSelfNavDrawerItem();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout == null) {
            return;
        }
        mDrawerLayout.setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        ScrimInsetsScrollView navDrawer = (ScrimInsetsScrollView) mDrawerLayout.findViewById(R.id.main_activity_navigation_drawer_rootLayout);

        if (selfItem == NAVDRAWER_ITEM_INVALID) {
            // do not show a nav drawer
            if (navDrawer != null) {
                ((ViewGroup) navDrawer.getParent()).removeView(navDrawer);
            }
            mDrawerLayout = null;
            return;
        }

        View accountView = findViewById(R.id.navigation_drawer_account_section);
        if (accountView != null) {
            accountView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://thespeakers-studio.com"));
                    startActivity(browserIntent);
                }
            });
        }

        mDrawerToggle = new ActionBarDrawerToggle
                (
                        this,
                        mDrawerLayout,
                        (Toolbar) findViewById(R.id.toolbar_actionbar),
                        R.string.drawer_open,
                        R.string.drawer_close
                )
        {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0);
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        populateNavDrawer();

        // this will show the nav drawer on the first run
        /*
        if (!SettingsUtils.isFirstRunProcessComplete(this)) {
            SettingsUtils.markFirstRunProcessesDone(this, true);
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
        */
    }

    protected boolean isNavDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    protected void closeNavDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void populateNavDrawer() {
        // TODO: add a login item?

        mNavDrawerItems.add(NAVDRAWER_ITEM_PRESENTATIONS);
        mNavDrawerItems.add(NAVDRAWER_ITEM_TIMER);
        mNavDrawerItems.add(NAVDRAWER_ITEM_STORIES);

        mNavDrawerItems.add(NAVDRAWER_ITEM_SEPARATOR_SPECIAL);

        mNavDrawerItems.add(NAVDRAWER_ITEM_CURIOUS);
        mNavDrawerItems.add(NAVDRAWER_ITEM_SETTINGS);
        mNavDrawerItems.add(NAVDRAWER_ITEM_ABOUT);

        createNavDrawerItems();

    }

    private void createNavDrawerItems() {
        mDrawerItemsListContainer = (ViewGroup) findViewById(R.id.navigation_drawer_item_list);
        if (mDrawerItemsListContainer == null) {
            return;
        }
        mNavDrawerItemViews = new View[mNavDrawerItems.size()];
        mDrawerItemsListContainer.removeAllViews();
        int i = 0;
        for (int itemId : mNavDrawerItems) {
            mNavDrawerItemViews[i] = makeNavDrawerItem(itemId, mDrawerItemsListContainer);
            mDrawerItemsListContainer.addView(mNavDrawerItemViews[i]);
            ++i;
        }
    }

    private View makeNavDrawerItem(final int itemId, ViewGroup container) {
        if (isSeparator(itemId)) {
            View separator = getLayoutInflater().inflate(R.layout.nav_drawer_separator, container, false);
            //TODO: accessibility - UIUtils.setAccessibilityIgnore(separator);
            return separator;
        }

        NavDrawerItemView item = (NavDrawerItemView) getLayoutInflater().inflate(
                R.layout.nav_drawer_item, container, false);
        item.setContent(NAVDRAWER_ICON_RES_ID[itemId], NAVDRAWER_TITLE_RES_ID[itemId]);
        item.setActivated(getSelfNavDrawerItem() == itemId);
        if (item.isActivated()) {
            // TODO: a11y - item.setContentDescription();
        } else {
            // TODO: a11y - item
        }

        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNavDrawerItemClicked(itemId);
            }
        });
        return item;
    }

    private boolean isSpecialItem(int itemId) {
        return itemId == NAVDRAWER_ITEM_SETTINGS;
    }

    private boolean isSeparator(int itemId) {
        return itemId == NAVDRAWER_ITEM_SEPARATOR || itemId == NAVDRAWER_ITEM_SEPARATOR_SPECIAL;
    }

    // this should be overridden by each subclass
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_INVALID;
    }

    private void onNavDrawerItemClicked(final int itemId) {
        if (itemId == getSelfNavDrawerItem()) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        if (isSpecialItem(itemId)) {
            goToNavDrawerItem(itemId);
        } else {
            // delay the activity, so the close animation will finish
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    goToNavDrawerItem(itemId);
                }
            }, SettingsUtils.NAVDRAWER_LAUNCH_DELAY);

            setSelectedNavDrawerItem(itemId);

            View mainContent = findViewById(R.id.main_content);
            if (mainContent != null) {
                mainContent.animate().alpha(0).setDuration(SettingsUtils.MAIN_CONTENT_FADEOUT_DURATION);
            }
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private void setSelectedNavDrawerItem(int itemId) {
        if (mNavDrawerItemViews != null) {
            for (int i = 0; i < mNavDrawerItemViews.length; i++) {
                if (i < mNavDrawerItems.size()) {
                    int thisItemId = mNavDrawerItems.get(i);
                    mNavDrawerItemViews[i].setActivated(itemId == thisItemId);
                }
            }
        }
    }

    private void goToNavDrawerItem(int item) {
        switch (item) {
            case NAVDRAWER_ITEM_PRESENTATIONS:
                createBackStack(new Intent(this, PresentationMainActivity.class));
                break;
            case NAVDRAWER_ITEM_TIMER:
                createBackStack(new Intent(this, PracticeSetupActivity.class));
                break;
            case NAVDRAWER_ITEM_STORIES:
                createBackStack(new Intent(this, StoriesMainActivity.class));
                break;
            case NAVDRAWER_ITEM_CURIOUS:
                createBackStack(new Intent(this, CuriousActivity.class));
                break;
            case NAVDRAWER_ITEM_SETTINGS:
                createBackStack(new Intent(this, SettingsActivity.class));
                break;
            case NAVDRAWER_ITEM_ABOUT:
                createBackStack(new Intent(this, AboutActivity.class));
                break;
        }
    }

    protected void createBackStack(Intent intent) {
        TaskStackBuilder builder = TaskStackBuilder.create(this);
        builder.addNextIntentWithParentStack(intent);
        builder.startActivities();
    }

    public static void navigateUpOrBack(Activity currentActivity,
                                        Bundle extras,
                                        Class<? extends Activity> syntheticParentActivity) {
        // Retrieve parent activity from AndroidManifest
        Intent intent = NavUtils.getParentActivityIntent(currentActivity);

        // Fake the parent activity if it doesn't exist
        if (intent == null && syntheticParentActivity != null) {
            try {
                intent = NavUtils.getParentActivityIntent(currentActivity, syntheticParentActivity);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (intent == null) {
            // No parent is defined in the manifest
            // Maybe we came here from another app or something, I don't see this happening actually
            // TODO: whatever happens in this case
            LOGE(TAG, "There is no parent activity to go back to");
        } else {
            if (extras != null) {
                intent.putExtras(extras);
            }

            if (NavUtils.shouldUpRecreateTask(currentActivity, intent)) {
                // generate a backstack
                TaskStackBuilder builder = TaskStackBuilder.create(currentActivity);
                builder.addNextIntentWithParentStack(intent);
                builder.startActivities();
            } else {
                NavUtils.navigateUpTo(currentActivity, intent);
            }
        }
    }

    protected Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                mActionBarToolbar.setNavigationContentDescription(getResources().getString(R.string
                    .navdrawer_description_a11y));
                setSupportActionBar(mActionBarToolbar);

                ActionBar ab = getSupportActionBar();
                if (ab != null) {
                    ab.setDisplayHomeAsUpEnabled(true);
                }
            }
            mHeaderBar = findViewById(R.id.headerbar);
            mHeaderDetails = findViewById(R.id.headerbar_details);
            if (mHeaderBar != null) {
                ViewTreeObserver vto = mHeaderBar.getViewTreeObserver();
                if (vto.isAlive()) {
                    vto.addOnGlobalLayoutListener(mGlobalLayoutListener);
                }
            }
        }
        return mActionBarToolbar;
    }

    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener
            = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            setupHeaderHeight();
        }
    };

    private void setupHeaderHeight() {
        if (mHeaderDetailsHeightPixels == -1 && mHeaderDetails != null) {
            mHeaderDetailsHeightPixels = mHeaderDetails.getHeight();
        }

        if (mHeaderHeightPixels == -1 && mHeaderBar != null) {
            mHeaderHeightPixels = mHeaderBar.getHeight();
            setLayoutPadding(mHeaderHeightPixels);
            onScrollChanged(0,0);
        }
    }

    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        if (findViewById(R.id.headerbar_scroll) == null) {
            return;
        }

        int scrollY = findViewById(R.id.headerbar_scroll).getScrollY();

        // the headerbar needs to follow the scroll
        mHeaderBar.setTranslationY(scrollY);

        if (mHeaderDetails != null) {
            ViewGroup.LayoutParams params = mHeaderDetails.getLayoutParams();
            params.height = getNewHeaderDetailsHeight(scrollY, true);
            mHeaderDetails.setLayoutParams(params);
        }

        setHeaderElevation(scrollY);
    }

    protected float setHeaderElevation(int scrollY) {
        // the headerbar will show a shadow as the user scrolls
        float gapFillProgress = 1;
        gapFillProgress = Math.min(Math.max(Utils.getProgress(scrollY,
                0,
                mHeaderHeightPixels), 0), 1);

        ViewCompat.setElevation(mHeaderBar, gapFillProgress * mMaxHeaderElevation);
        return gapFillProgress;
    }

    protected int getNewHeaderDetailsHeight(int scrollY, boolean parallax) {
        int minHeaderDetailsHeight = getMinHeaderHeight(); //mHeaderDetails.getMinHeight();
        // the details text will shrink as the user scrolls
        // multiply scrollY by a fraction to give it a bit of a parallax effect
        float factor = parallax ? (float) (scrollY * 0.75) : scrollY;
        float heightDifference = Math.min(mHeaderDetailsHeightPixels - minHeaderDetailsHeight, factor);
        return mHeaderDetailsHeightPixels - (int) heightDifference;
    }

    // override this to make the header details area shrink to a specific size instead of 0
    protected int getMinHeaderHeight() {
        return 0;
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getActionBarToolbar();
        if (findViewById(R.id.headerbar_scroll) != null) {
            ((SmartScrollView) findViewById(R.id.headerbar_scroll)).addCallbacks(this);
        }
    }

    @Override
    public void onBackPressed() {
        if (isNavDrawerOpen()) {
            closeNavDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupNavDrawer();
        // TODO: setupAccountBox();

        // TODO: trySetupSwipeRefresh();
        // TODO: updateSwipeRefreshProgressBarTop();

        /* fade in the content
        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        } else {
            LOGW(TAG, "No view with ID main_content to fade in");
        }
        */
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // TODO: any cross-activity defaults here

        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        /* React to settings changes here
        if (key != null && key.equals(SettingsUtils.SOME_KEY_HERE)) {
            // do a thing
        }
        */
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        int actionBarSize = Utils.calculateActionBarSize(this);
        ToolbarShadowFrameLayout layout = (ToolbarShadowFrameLayout) findViewById(R.id.main_content);
        if (layout != null) {
            layout.setShadowTopOffset(actionBarSize);
            setLayoutPadding(actionBarSize);
        }
    }

    abstract protected void setLayoutPadding(int actionBarSize);

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO: google account stuff will go here
        if (requestCode == SELECT_GOOGLE_ACCOUNT_RESULT) {

        }
    }

    @Override
    protected void onStop() {
        LOGD(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }
}
