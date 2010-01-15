package com.sunlightlabs.android.congress;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;
import com.sunlightlabs.android.congress.utils.Utils;
import com.sunlightlabs.android.congress.utils.ViewArrayAdapter;

public class MainMenu extends ListActivity {
	public static final int RESULT_ZIP = 1;
	public static final int RESULT_LASTNAME = 2;
	public static final int RESULT_STATE = 3;
	
	private static final int ABOUT = 0;
	private static final int FIRST = 1;
	
	private static final int SEARCH_LOCATION = 1;
	private static final int SEARCH_ZIP = 2;
	private static final int SEARCH_STATE = 3;
	private static final int SEARCH_NAME = 4;

	private Location location;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        location = getLocation();
        setupControls();
        
        if (firstTime())
        	showDialog(FIRST);
    }
	
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
		int type = ((Integer) v.getTag()).intValue();
		switch(type) {
		case SEARCH_LOCATION:
			searchByLatLong(location.getLatitude(), location.getLongitude());
			break;
		case SEARCH_ZIP:
			getResponse(RESULT_ZIP);
			break;
		case SEARCH_NAME:
			getResponse(RESULT_LASTNAME);
			break;
		case SEARCH_STATE:
			getResponse(RESULT_STATE);
			break;
		}
    }
	
	public void setupControls() {
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout mainHeader = (LinearLayout) inflater.inflate(R.layout.main, null);
        ((TextView) mainHeader.findViewById(R.id.header_text)).setText("Find Legislator By");
        mainHeader.setEnabled(false);
        
        ArrayList<View> searchViews = new ArrayList<View>(4);
        searchViews.add(Utils.makeIconListItem(inflater, SEARCH_LOCATION, R.drawable.search_location, "My Location"));
        searchViews.add(Utils.makeIconListItem(inflater, SEARCH_STATE, R.drawable.search_all, "State"));
        searchViews.add(Utils.makeIconListItem(inflater, SEARCH_NAME, R.drawable.search_lastname, "Last Name"));
        searchViews.add(Utils.makeIconListItem(inflater, SEARCH_ZIP, R.drawable.search_zip, "Zip Code"));
        
        MergeAdapter adapter = new MergeAdapter();
        adapter.addView(mainHeader);
        adapter.addAdapter(new ViewArrayAdapter(this, searchViews));
        
        setListAdapter(adapter);
    }
	
	
	
	public Location getLocation() {
		Location location = null;
		LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
		
		if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
			location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		if (location == null && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
			location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		
		return location;
	}
	
	public void searchByZip(String zipCode) {
		Bundle extras = new Bundle();
		extras.putString("zip_code", zipCode);
		search(extras);
    }
	
	public void searchByLatLong(double latitude, double longitude) {
		Bundle extras = new Bundle();
		extras.putDouble("latitude", latitude);
		extras.putDouble("longitude", longitude);
		search(extras);
	}
	
	public void searchByLastName(String lastName) {
		Bundle extras = new Bundle();
		extras.putString("last_name", lastName);
		search(extras);
	}
	
	public void searchByState(String state) {
		Bundle extras = new Bundle();
		extras.putString("state", state);
		search(extras);
	}
	
	private void search(Bundle extras) {
		Intent i = new Intent();
		i.setClassName("com.sunlightlabs.android.congress", "com.sunlightlabs.android.congress.LegislatorList");
		i.putExtras(extras);
		startActivity(i);
	}
	
	private void getResponse(int requestCode) {
		Intent intent = new Intent();
		
		
		switch (requestCode) {
		case RESULT_ZIP:
			intent.setClassName("com.sunlightlabs.android.congress", "com.sunlightlabs.android.congress.GetText");
			intent.putExtra("ask", "Enter a zip code:");
			intent.putExtra("hint", "e.g. 11216");
			intent.putExtra("inputType", InputType.TYPE_CLASS_PHONE);
			break;
		case RESULT_LASTNAME:
			intent.setClassName("com.sunlightlabs.android.congress", "com.sunlightlabs.android.congress.GetText");
			intent.putExtra("ask", "Enter a last name:");
			intent.putExtra("hint", "e.g. Schumer");
			intent.putExtra("inputType", InputType.TYPE_TEXT_FLAG_CAP_WORDS);
			break;
		case RESULT_STATE:
			intent.setClassName("com.sunlightlabs.android.congress", "com.sunlightlabs.android.congress.GetState");
			break;
		default:
			break;
		}
		
		startActivityForResult(intent, requestCode);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case RESULT_ZIP:
			if (resultCode == RESULT_OK) {
				String zipCode = data.getExtras().getString("response").trim();
				if (!zipCode.equals(""))
					searchByZip(zipCode);
			}
			break;
		case RESULT_LASTNAME:
			if (resultCode == RESULT_OK) {
				String lastName = data.getExtras().getString("response").trim();
				if (!lastName.equals(""))
					searchByLastName(lastName);
			}
			break;
		case RESULT_STATE:
			if (resultCode == RESULT_OK) {
				String state = data.getExtras().getString("response").trim();
				
				String code = Utils.stateNameToCode(this, state.trim());
				if (code != null)
					state = code;
				
				if (!state.equals(""))
					searchByState(state);
			}
			break;
		}
	}
	
	public boolean firstTime() {
		if (Preferences.getBoolean(this, "first_time", true)) {
			Preferences.setBoolean(this, "first_time", false);
			return true;
		}
		return false;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	LayoutInflater inflater = getLayoutInflater();
    	
        switch(id) {
        case ABOUT:
        	LinearLayout aboutView = (LinearLayout) inflater.inflate(R.layout.about, null);
        	
        	TextView about3 = (TextView) aboutView.findViewById(R.id.about_3);
        	about3.setText(R.string.about_3);
        	Linkify.addLinks(about3, Linkify.WEB_URLS);
        	
        	builder.setView(aboutView);
        	builder.setPositiveButton(R.string.about_button, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}
			});
            return builder.create();
        case FIRST:
        	ScrollView firstView = (ScrollView) inflater.inflate(R.layout.first_time, null);
        	
        	builder.setView(firstView);
        	builder.setPositiveButton(R.string.first_button, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {}
			});
            return builder.create();
        default:
            return null;
        }
    }
	
	@Override 
    public boolean onCreateOptionsMenu(Menu menu) { 
	    super.onCreateOptionsMenu(menu); 
	    getMenuInflater().inflate(R.menu.main, menu);
	    return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) { 
    	case R.id.settings: 
    		startActivity(new Intent(this, Preferences.class));
    		break;
    	case R.id.feedback:
    		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getResources().getString(R.string.contact_email), null));
    		intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.contact_subject));
    		startActivity(intent);
    		break;
    	case R.id.about:
    		showDialog(ABOUT);
    		break;
    	}
    	return true;
    }
}