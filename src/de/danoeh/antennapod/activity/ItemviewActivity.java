package de.danoeh.antennapod.activity;

import java.text.DateFormat;

import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import de.danoeh.antennapod.AppConfig;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.dialog.DownloadRequestErrorDialogCreator;
import de.danoeh.antennapod.feed.Feed;
import de.danoeh.antennapod.feed.FeedItem;
import de.danoeh.antennapod.feed.FeedManager;
import de.danoeh.antennapod.fragment.FeedlistFragment;
import de.danoeh.antennapod.fragment.ItemDescriptionFragment;
import de.danoeh.antennapod.fragment.ItemlistFragment;
import de.danoeh.antennapod.preferences.UserPreferences;
import de.danoeh.antennapod.storage.DownloadRequestException;
import de.danoeh.antennapod.util.StorageUtils;
import de.danoeh.antennapod.util.menuhandler.FeedItemMenuHandler;

/** Displays a single FeedItem and provides various actions */
public class ItemviewActivity extends SherlockFragmentActivity {
	private static final String TAG = "ItemviewActivity";

	private FeedManager manager;
	private FeedItem item;

	// Widgets
	private TextView txtvTitle;
	private TextView txtvPublished;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(UserPreferences.getTheme());
		super.onCreate(savedInstanceState);
		StorageUtils.checkStorageAvailability(this);
		manager = FeedManager.getInstance();
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		extractFeeditem();
		populateUI();
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	@Override
	protected void onResume() {
		super.onResume();
		StorageUtils.checkStorageAvailability(this);

	}

	@Override
	public void onStop() {
		super.onStop();
		if (AppConfig.DEBUG)
			Log.d(TAG, "Stopping Activity");
	}

	/** Extracts FeedItem object the activity is supposed to display */
	private void extractFeeditem() {
		long itemId = getIntent().getLongExtra(
				ItemlistFragment.EXTRA_SELECTED_FEEDITEM, -1);
		long feedId = getIntent().getLongExtra(
				FeedlistFragment.EXTRA_SELECTED_FEED, -1);
		if (itemId == -1 || feedId == -1) {
			Log.e(TAG, "Received invalid selection of either feeditem or feed.");
		}
		Feed feed = manager.getFeed(feedId);
		item = manager.getFeedItem(itemId, feed);
		if (AppConfig.DEBUG)
			Log.d(TAG, "Title of item is " + item.getTitle());
		if (AppConfig.DEBUG)
			Log.d(TAG, "Title of feed is " + item.getFeed().getTitle());
	}

	private void populateUI() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.feeditemview);
		txtvTitle = (TextView) findViewById(R.id.txtvItemname);
		txtvPublished = (TextView) findViewById(R.id.txtvPublished);
		setTitle(item.getFeed().getTitle());

		txtvPublished.setText(DateUtils.formatSameDayTime(item.getPubDate()
				.getTime(), System.currentTimeMillis(), DateFormat.MEDIUM,
				DateFormat.SHORT));
		txtvTitle.setText(item.getTitle());

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		ItemDescriptionFragment fragment = ItemDescriptionFragment
				.newInstance(item, false);
		fragmentTransaction.replace(R.id.description_fragment, fragment);
		fragmentTransaction.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.feeditem, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		try {
			if (!FeedItemMenuHandler.onMenuItemClicked(this,
					menuItem.getItemId(), item)) {
				switch (menuItem.getItemId()) {
				case android.R.id.home:
					finish();
					break;
				}
			}
		} catch (DownloadRequestException e) {
			e.printStackTrace();
			DownloadRequestErrorDialogCreator.newRequestErrorDialog(this,
					e.getMessage());
		}
		supportInvalidateOptionsMenu();
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		return FeedItemMenuHandler.onPrepareMenu(
				new FeedItemMenuHandler.MenuInterface() {

					@Override
					public void setItemVisibility(int id, boolean visible) {
						menu.findItem(id).setVisible(visible);
					}
				}, item, true);
	}

}
