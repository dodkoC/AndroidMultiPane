package com.plgrnd.android.multipane;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MasterFragment.MasterFragmentCallback, ActionMode.Callback {

    private static final String STATE_SHOWING_DETAIL = "state_showing_detail";
    private static final String STATE_SELECTED_ITEM = "state_selected_item";
    private static final String STATE_MULTI_MODE_ITEMS = "state_multi_mode_items";
    private static final int NONE = -1;
    private boolean mDualPane;
    private boolean mShowingDetail;
    private RetainedFragment mRetainedFragment;
    private int mSelectedItem = NONE;
    private ArrayList<Integer> mMultiModeItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDualPane = findViewById(R.id.dual_pane_container) != null;

        FragmentManager fm = getFragmentManager();
        if (savedInstanceState == null) {
            mRetainedFragment = new RetainedFragment();
            fm.beginTransaction().add(mRetainedFragment, RetainedFragment.TAG).commit();
            if (!mDualPane) {
                fm.beginTransaction()
                        .replace(R.id.single_pane_container, new MasterFragment(), MasterFragment.TAG)
                        .commit();
                fm.executePendingTransactions();
            }
        } else {
            mShowingDetail = savedInstanceState.getBoolean(STATE_SHOWING_DETAIL);
            if (mShowingDetail) {
                mSelectedItem = savedInstanceState.getInt(STATE_SELECTED_ITEM, NONE);
            }
            if (savedInstanceState.containsKey(STATE_MULTI_MODE_ITEMS)) {
                mMultiModeItems = savedInstanceState.getIntegerArrayList(STATE_MULTI_MODE_ITEMS);
            }
        }
        mRetainedFragment = (RetainedFragment) fm.findFragmentByTag(RetainedFragment.TAG);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMultiModeItems != null) {
            MasterFragment fragment = getMasterFragment();
            if (fragment != null) {
                fragment.selectMultiModeItems(mMultiModeItems);
            }
        }
        if (mShowingDetail) {
            removeDetailFragment();
            addDetailFragment(mSelectedItem);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

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

        if (id == android.R.id.home) {
            if (!mDualPane) {
                removeDetailFragment();
                mSelectedItem = NONE;
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void addDetailFragment(int itemId) {
        mShowingDetail = true;
        mSelectedItem = itemId;

        MasterFragment masterFragment = getMasterFragment();
        if (masterFragment != null) {
            masterFragment.selectItem(mSelectedItem);
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (mDualPane) {
            ft.replace(R.id.detail_container, DetailFragment.newInstance(itemId), DetailFragment.TAG);
        } else {
            ft.replace(R.id.single_pane_container, DetailFragment.newInstance(itemId), DetailFragment.TAG);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
        ft.addToBackStack(null).commit();
    }

    private MasterFragment getMasterFragment() {
        FragmentManager fm = getFragmentManager();
        MasterFragment fragment;
        if (mDualPane) {
            fragment = (MasterFragment) fm.findFragmentById(R.id.master_fragment);
        } else {
            fragment = (MasterFragment) fm.findFragmentByTag(MasterFragment.TAG);
        }
        return fragment;
    }

    private void removeDetailFragment() {
        mShowingDetail = false;

        MasterFragment masterFragment = getMasterFragment();
        if (masterFragment != null) {
            masterFragment.deselectItem();
        }

        FragmentManager fm = getFragmentManager();
        Fragment detailFragment = fm.findFragmentByTag(DetailFragment.TAG);

        if (detailFragment != null) {
            fm.beginTransaction().remove(detailFragment).commit();
            fm.popBackStack();
        }
        fm.executePendingTransactions();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public void onBackPressed() {
        if (mShowingDetail) {
            removeDetailFragment();
            mSelectedItem = NONE;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SHOWING_DETAIL, mShowingDetail);
        outState.putInt(STATE_SELECTED_ITEM, mSelectedItem);
        if (mMultiModeItems != null) {
            outState.putIntegerArrayList(STATE_MULTI_MODE_ITEMS, mMultiModeItems);
        }
    }

    @Override
    public void onListItemClick(View v, int itemId) {
        removeDetailFragment();
        addDetailFragment(itemId);
    }

    @Override
    public void onListItemClickInMultiMode(View v, int itemId, ArrayList<Integer> selectedItems) {
        mMultiModeItems = selectedItems;
    }

    @Override
    public void onListItemLongClick(View v, int itemId, ArrayList<Integer> selectedItems) {
        startActionMode(this);
        mMultiModeItems = selectedItems;
    }

    @Override
    public List<String> getListData() {
        if (mRetainedFragment == null) {
            mRetainedFragment = (RetainedFragment) getFragmentManager().findFragmentByTag(RetainedFragment.TAG);
        }
        return mRetainedFragment != null ? mRetainedFragment.getRetainedArray() : null;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }
}
