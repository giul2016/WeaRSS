package com.creativedrewy.wearss.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.astuetz.PagerSlidingTabStrip;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.creativedrewy.wearss.R;
import com.creativedrewy.wearss.adapters.MainActivityPagerAdapter;
import com.creativedrewy.wearss.feedservice.SyncAlarmListenerService;
import com.creativedrewy.wearss.fragments.ReadListFragment;
import com.creativedrewy.wearss.services.ArticleDownloadService;
import com.creativedrewy.wearss.services.PhoneFromWearListenerService;

import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Source;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Landing activity for entire app; list out the user's feeds on the feeds fragment
 */
public class FeedsMainActivity extends FragmentActivity {
    private int mCurrentPage = 0;

    @InjectView(R.id.mainViewPager)
    ViewPager mViewPager;
    @InjectView(R.id.topTabStrip)
    PagerSlidingTabStrip mTabStrip;

    private MainActivityPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_feeds_main);
        ButterKnife.inject(this);

        mPagerAdapter = new MainActivityPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);

        mTabStrip.setTextColor(0xFFFFFFFF);
        mTabStrip.setIndicatorColor(0xFF8d4104);
        mTabStrip.setIndicatorHeight((int) (4 * getResources().getDisplayMetrics().scaledDensity));
        mTabStrip.setViewPager(mViewPager);
        mTabStrip.setOnPageChangeListener(mPagerListener);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean(getString(R.string.key_prefs_alarms_started), false)) {
            WakefulIntentService.scheduleAlarms(new SyncAlarmListenerService(), this);
        }

        //When opening from the wearable we go to the read list tab
        if (getIntent().getBooleanExtra(PhoneFromWearListenerService.OPEN_READ_LIST_EXTRA, false)) {
            mViewPager.setCurrentItem(1);
        }

        registerReceiver(mWearableUpdateReceiver, new IntentFilter(getString(R.string.receive_headline_intent)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mWearableUpdateReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        switch (mCurrentPage) {
            case 0:
                getMenuInflater().inflate(R.menu.feeds_listing_menu, menu);
                break;
            case 1:
                getMenuInflater().inflate(R.menu.read_list_menu, menu);
                break;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_feed) {
            //Intent intent = new Intent(this, AddFeedActivity.class);
            //startActivity(intent);

            String url = "http://www.engadget.com/2015/04/05/optical-nanotech-gas-sensor/";
            ArticleDownloadService.downloadArticle(url)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(articleText -> {
                        Log.v("MyTag", "You got the article");
                        int sizey = articleText.length();

                        Renderer renderer = new Source(articleText).getRenderer();
                        renderer.setMaxLineLength(150);
                        renderer.setIncludeHyperlinkURLs(false);
                        renderer.setIncludeAlternateText(false);
                        renderer.setHRLineLength(8);
                        renderer.setListIndentSize(0);

                        String output = renderer.toString();
                        int size = output.length();
                    }, throwable -> {
                        Log.e("MyTag", "There was an error, yo: " + throwable.getMessage());
                    });

        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else {
            mPagerAdapter.getFragmentInstance(mCurrentPage).onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    private ViewPager.OnPageChangeListener mPagerListener = new ViewPager.OnPageChangeListener() {
        @Override public void onPageScrolled(int i, float v, int i2) { }
        @Override public void onPageScrollStateChanged(int i) { }

        @Override
        public void onPageSelected(int pageNum) {
            mCurrentPage = pageNum;
            invalidateOptionsMenu();
        }
    };

    private BroadcastReceiver mWearableUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ((ReadListFragment)mPagerAdapter.getFragmentInstance(1)).setupList();
        }
    };
}
