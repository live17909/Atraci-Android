package net.getatraci.atraci.activities;

import net.getatraci.atraci.R;
import net.getatraci.atraci.data.DatabaseHelper;
import net.getatraci.atraci.layouthelpers.PlayerPageTransformer;
import net.getatraci.atraci.loaders.PagerFragmentAdapter;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * This Activity is the main activity loaded once the application opens.
 * Features a nav drawer and displays the news fragment by default.
 * @author Blake LaFleur
 *
 */

public class HomeActivity extends ActionBarActivity implements OnItemClickListener, OnPageChangeListener{

	private String[] mNavigationDrawerItemTitles; 	 //Contains list of items in the Navigation Drawer
	private DrawerLayout mDrawerLayout;			 	 //Placeholder for navigation drawer
	private static ListView mDrawerList;					 //View that holds the navigation drawer
	private ActionBarDrawerToggle mDrawerToggle;  	 //Toggle on App Icon to open nav drawer
	private static DatabaseHelper database;
	public static ViewPager pager;
	private boolean confirmExit = false;
	private static PagerFragmentAdapter pageAdapter;
	public static PagerFragmentAdapter getPageAdapter() {
		return pageAdapter;
	}



	public static void setPageAdapter(PagerFragmentAdapter pageAdapter) {
		HomeActivity.pageAdapter = pageAdapter;
	}



	private static SearchFragment search;
	public static SongListFragment songlist;
	public static Toolbar mToolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		mToolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().show();
		pager = (ViewPager)findViewById(R.id.content_frame);
		pager.setOffscreenPageLimit(2);
		pager.setPageTransformer(true, new PlayerPageTransformer());
		pageAdapter = new PagerFragmentAdapter(this.getSupportFragmentManager());
		pager.setAdapter(pageAdapter);
		database = new DatabaseHelper(this.getApplicationContext());
		mNavigationDrawerItemTitles= getResources().getStringArray(R.array.navigation_items);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.navigation_bar_item, mNavigationDrawerItemTitles));
		mDrawerList.setDivider(getResources().getDrawable(android.R.drawable.divider_horizontal_dark));
		mDrawerList.setDividerHeight(10);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
			}
		};
		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		// Set the click listener to the callbacks in this Activity
		mDrawerList.setOnItemClickListener(this);	
		// Make the AppIcon clickable
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// Allow user to tap the appicon
		getSupportActionBar().setHomeButtonEnabled(true);
		
		pager.setOnPageChangeListener(this);
	}
	
	

	/* Called after the activity has been created */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();   // Sync the toggle state after onRestoreInstanceState has occurred.
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		Log.d("ATRACI", "Inflate options");
		return true;
	}

	/* Listener for navbar items like search and drawer */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		if (id == R.id.action_search) {
			openSearch();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		super.onPause();
	};



	@Override
	public void onBackPressed() {
		if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
			mDrawerLayout.closeDrawers();
		}
		else if(pager.getCurrentItem() == 1){
			pager.setCurrentItem(0);
			confirmExit = false;
		}
		else if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
			try {
				getSupportFragmentManager().popBackStack();
				confirmExit = false;
			} catch (IllegalStateException e) {
			}
		}
		else {
			if(confirmExit){
				database.closeDatabaseConnection();
				((PlayerFragment)pageAdapter.getRegisteredFragment(1)).onDestroy();
				finish();
			} else {
				confirmExit = true;
				Toast.makeText(this, "Press back again to exit Atraci.", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/* Called when configuration has changed. Ex: when screen rotated */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		//Update navigation drawer to match the current config
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	/* Function that starts the Seaching activity */
	private void openSearch() {
		if(search == null){
			search = new SearchFragment(songlist);
		}
		pager.setCurrentItem(0);
		mDrawerLayout.closeDrawers();
		getSupportFragmentManager().beginTransaction().replace(R.id.root_frame, search).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
	}

	/* Listener for when user clicks an item in the Navigation Drawer */
	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		pager.setCurrentItem(0); //If user is using the nav drawer to make selection, we always want to see the root fragment
		switch(position) {
		case 0:		// Top 100 tracks item clicked
			launchTop100();
			break;
		case 1:		// Playlists items clicked
			this.getSupportFragmentManager().beginTransaction().replace(R.id.root_frame, new PlaylistSelectorFragment(database)).addToBackStack("playlists").commit();
			break;
		case 2:		// History item clicked
			launchHistory();
			break;
		case 3:		//Now playing item clicked
			pager.setCurrentItem(1);
			break;	
		case 4:		//Donate item clicked
			launchDonate();
			break;

		case 5:		//Settings item clicked
			this.getSupportFragmentManager().beginTransaction().replace(R.id.root_frame, new SettingsFragment(this)).addToBackStack(null).commit();
			break;
		}
		// Set which item was selected in the nav drawer
		mDrawerList.setItemChecked(position, true);
		// Since an item was clicked, we want to move it out of the way and bring focus to user selection
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	private void launchDonate() {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=Q9SDPBK7VMQ8N"));
		startActivity(browserIntent);
	}

	private void launchTop100() {
		Bundle bundle = new Bundle();
		bundle.putString("query", SongListFragment.QUERY_TOP100);
		bundle.putBoolean("isPlaylist", false);
		if(songlist == null){
			songlist = new SongListFragment();
		}
		if(songlist.getActivity() == null){
			songlist.setArguments(bundle);
		} else {
			songlist.setBundle(bundle);
		}
		getSupportFragmentManager().beginTransaction().replace(R.id.root_frame, songlist).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
	}

	private void launchHistory(){
		Bundle bundle = new Bundle();
		bundle.putString("query", SongListFragment.QUERY_HISTORY);
		bundle.putBoolean("isPlaylist", false);
		if(songlist == null){
			songlist = new SongListFragment();
		}
		if(songlist.getActivity() == null){
			songlist.setArguments(bundle);
		} else {
			songlist.setBundle(bundle);
		}
		getSupportFragmentManager().beginTransaction().replace(R.id.root_frame, songlist).addToBackStack(null).commit();
	}

	public ViewPager getPager() {
		return pager;
	}

	public static DatabaseHelper getDatabase() {
		return database;
	}



	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onPageScrolled(int pos, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onPageSelected(int pos) {
		if(pos == 1){
			getSupportActionBar().setTitle(getResources().getStringArray(R.array.navigation_items)[3]);
		} else {
			getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
		}
	}
}
