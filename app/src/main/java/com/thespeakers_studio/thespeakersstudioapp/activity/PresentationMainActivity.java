package com.thespeakers_studio.thespeakersstudioapp.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.thespeakers_studio.thespeakersstudioapp.adapter.PresentationListSpanItemDecoration;
import com.thespeakers_studio.thespeakersstudioapp.ui.PresentationListViewHolder;
import com.thespeakers_studio.thespeakersstudioapp.adapter.PresentationListAdapter;
import com.thespeakers_studio.thespeakersstudioapp.model.PresentationData;
import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.settings.SettingsUtils;
import com.thespeakers_studio.thespeakersstudioapp.utils.AnalyticsHelper;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import java.util.ArrayList;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGD;
import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.makeLogTag;

public class PresentationMainActivity extends BaseActivity
        implements PresentationListViewHolder.OnPresentationCardClickedListener,
        View.OnClickListener {

    private static final String TAG = makeLogTag(PresentationMainActivity.class);
    private static final String SCREEN_LABEL = "Presentation List";

    private ArrayList<PresentationData> mPresentations;

    //private GoogleApiClient mGoogleApiClient;
    //private LocationSelectedListener mLocationListener;

    private Menu mMenu;

    static final String STATE_PRESENTATION_ID = "presentationId";
    static final String STATE_FRAGMENT = "openFragment";

    private boolean mIsTwoColumn;
    private StaggeredGridLayoutManager mTwoColumnManager;
    private LinearLayoutManager mOneColumnManager;

    private RecyclerView mRecyclerView;
    private PresentationListAdapter mAdapter;

    private boolean mSelectionActive;

    private FloatingActionButton mCreateFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_presentation_main);

        /*
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();
                */

        mPresentations = mDbHelper.loadPresentations();

        AnalyticsHelper.sendScreenView(SCREEN_LABEL);

        //TODO: registerHideableHeaderView(findViewById(R.id.headerbar));

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mTwoColumnManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mOneColumnManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mIsTwoColumn = SettingsUtils.isPresentationListTwoColumns(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.presentation_list);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new PresentationListAdapter(mPresentations, this);

        toggleView(mIsTwoColumn);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new PresentationListSpanItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.pres_list_card_padding)));

        mCreateFab = (FloatingActionButton) findViewById(R.id.create_presentation);
        if (mCreateFab != null) {
            mCreateFab.setOnClickListener(this);
        }

        showMessageIfEmpty();
    }

    @Override
    protected void setLayoutPadding(int actionBarSize) {
        int pad = (int) getResources().getDimension(R.dimen.pres_list_padding);
        int fab = (int) getResources().getDimension(R.dimen.fab_button);
        mRecyclerView.setPadding(pad, pad, pad, pad + fab);
        ((FrameLayout.LayoutParams) mRecyclerView.getLayoutParams()).setMargins(0, actionBarSize, 0, 0);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // TODO: enableActionBarAutoHide((CollectionView) findViewById());
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_PRESENTATIONS;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        MenuInflater inflater = getMenuInflater();
        int menuId;
        if (mAdapter != null && mAdapter.getSelectedCount() > 0) {
            menuId = R.menu.menu_presentation_list_selection;
        } else {
            menuId = R.menu.menu_presentation_list;
        }

        inflater.inflate(menuId, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.menu_action_search:
                Toast.makeText(this, "Search isn't implemented yet", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_action_view:
                toggleCardViewType();
                break;
            case R.id.menu_action_delete:
                deleteSelectedPresentations();
                break;
            case android.R.id.home:
                if (mSelectionActive) {
                    onBackPressed();
                } else {
                    super.onOptionsItemSelected(item);
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mSelectionActive) {
            deselectAll();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPresentationSelected(PresentationData presentation) {
        LOGD(TAG, "Presentation " + presentation.getId() + " selected");

        Toolbar selectionToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar_selection);

        if (selectionToolbar != null && !mSelectionActive) {
            setSupportActionBar(selectionToolbar);
            selectionToolbar.setAlpha(0);
            selectionToolbar.setVisibility(View.VISIBLE);
            selectionToolbar.animate().alpha(1).setDuration(SettingsUtils.SELECTION_TOOLBAR_FADE_DURATION);
        }
        mSelectionActive = true;

        invalidateOptionsMenu();

        ActionBar bar = getSupportActionBar();
        if (bar == null) {
            return;
        }

        bar.setTitle(String.valueOf(mAdapter.getSelectedCount()));
        bar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onPresentationDeselected(PresentationData presentation) {
        LOGD(TAG, "Presentation " + presentation.getId() + " deselected");

        ActionBar bar = getSupportActionBar();
        if (bar == null) {
            return;
        }

        if (mAdapter.getSelectedCount() <= 0) {
            hideSelectionToolbar();
        } else {
            bar.setTitle(String.valueOf(mAdapter.getSelectedCount()));
        }
    }

    // this method resets the toolbar to the regular one, hiding the selection version
    private void hideSelectionToolbar() {
        final Toolbar selectionToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar_selection);
        Toolbar basicToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);

        if (selectionToolbar != null && mSelectionActive) {
            final ViewPropertyAnimator vp = selectionToolbar.animate()
                    .alpha(0).setDuration(SettingsUtils.SELECTION_TOOLBAR_FADE_DURATION);

            vp.setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    selectionToolbar.setVisibility(View.GONE);
                    vp.setListener(null);
                }
            });
        }
        mSelectionActive = false;

        if (basicToolbar != null) {
            setSupportActionBar(basicToolbar);
        }
        invalidateOptionsMenu();
    }

    // this clears the selection, usually because the user hit "back"
    private void deselectAll() {
        hideSelectionToolbar();
        mAdapter.deselectAll();
    }

    @Override
    public boolean onPresentationOpened(PresentationData presentation) {
        // start the step list activity for the selected presentation
        LOGD(TAG, "Presentation " + presentation.getId() + " opened");
        Intent intent = new Intent(getApplicationContext(), EditPresentationActivity.class);
        intent.putExtra(Utils.INTENT_PRESENTATION_ID, presentation.getId());

        //startActivity(intent);
        createBackStack(intent);

        /*
        if (Utils.versionGreaterThan(21)) {
            TransitionManager.beginDelayedTransition(mRecyclerView, new Explode());
            for (int cnt = 0; cnt < mRecyclerView.getChildCount(); cnt++) {
                View view = mRecyclerView.getChildAt(cnt);
                boolean isVisible = view.getVisibility() == View.VISIBLE;
                view.setVisibility(isVisible ? View.INVISIBLE : View.VISIBLE);
            }
        }
        */

        return true;
    }

    @Override
    public void onPresentationPracticeSelected(PresentationData presentation) {
        Intent intent = new Intent(getApplicationContext(), PracticeSetupActivity.class);
        intent.putExtra(Utils.INTENT_PRESENTATION_ID, presentation.getId());
        startActivityForResult(intent, PracticeSetupActivity.REQUEST_CODE);
    }

    @Override
    public void onPresentationDeleteSelected(PresentationData presentation) {
        deleteSelectedPresentations(presentation);
    }

    public void toggleView(boolean set) {
        if (!set) {
            mAdapter.setIsTwoColumn(false);
            mRecyclerView.setLayoutManager(mOneColumnManager);
            mIsTwoColumn = false;
        } else {
            mAdapter.setIsTwoColumn(true);
            mRecyclerView.setLayoutManager(mTwoColumnManager);
            mIsTwoColumn = true;
        }
    }
    public boolean toggleView() {
        toggleView(!mIsTwoColumn);
        return mIsTwoColumn;
    }
    private void toggleCardViewType() {
        boolean twoCol = toggleView();

        SettingsUtils.setPresentationListTwoColumns(this, twoCol);

        if (mMenu != null) {
            if (twoCol) {
                mMenu.findItem(R.id.menu_action_view).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_view_agenda_white_24dp));
            } else {
                mMenu.findItem(R.id.menu_action_view).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_view_quilt_white_24dp));
            }
        }
    }

    public void showMessageIfEmpty() {
        if (mPresentations.size() == 0) {
            findViewById(R.id.no_presentations).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.no_presentations).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick (View v) {
        switch(v.getId()) {
            case R.id.create_presentation:
                PresentationData newPres = mDbHelper.createNewPresentation();
                onPresentationOpened(newPres);
                break;
        }
    }

    private void deleteSelectedPresentations(final PresentationData pres) {
        int count;
        if (pres == null) {
            count = mAdapter.getSelectedCount();
            if (count == 0) {
                return;
            }
        } else {
            count = 1;
        }

        new AlertDialog.Builder(this)
                .setMessage(getResources().getQuantityString(R.plurals.confirm_delete_message,
                        count, count))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doDeleteSelectedPresentations(pres);
                    }
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }
    private void deleteSelectedPresentations() {
        deleteSelectedPresentations(null);
    }
    private void doDeleteSelectedPresentations(PresentationData presentation) {
        if (presentation == null) {
            mDbHelper.deletePresentation(mAdapter.getSelectedPresentations());
        } else {
            mDbHelper.deletePresentation(presentation);
        }

        mPresentations = mDbHelper.loadPresentations();
        mAdapter.setPresentations(mPresentations);

        hideSelectionToolbar();
    }

}
