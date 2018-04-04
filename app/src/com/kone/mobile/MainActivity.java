/*
 * Copyright (c) 2012-present, salesforce.com, inc.
 * All rights reserved.
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * - Neither the name of salesforce.com, inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission of salesforce.com, inc.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.kone.mobile;

import android.os.Bundle;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestClient.AsyncRequestCallback;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceActivity;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import android.support.v7.app.AppCompatActivity;

/**
 * Main activity
 */
public class MainActivity extends SalesforceActivity implements AppCompatCallback {

    private RestClient client;
    private ArrayAdapter<String> listAdapter;
	private AppCompatDelegate delegate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {


		super.onCreate(savedInstanceState);

		// Setup view
//		setContentView(R.layout.main);

		delegate = AppCompatDelegate.create(this, this);
		//call the onCreate() of the AppCompatDelegate
		delegate.onCreate(savedInstanceState);
		//use the delegate to inflate the layout
		delegate.setContentView(R.layout.main);
//		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//		AppCompatActivity bar = new AppCompatActivity();
//		bar.setSupportActionBar(toolbar);
		Toolbar toolbar= (Toolbar) findViewById(R.id.toolbar);
//		delegate.setSupportActionBar(toolbar);

	}

	public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback){
		return null;
	}
	public void onSupportActionModeFinished(ActionMode mode){

	}
	public void onSupportActionModeStarted(ActionMode mode){

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public void onResume() {
		// Hide everything until we are logged in
		findViewById(R.id.root).setVisibility(View.INVISIBLE);

		// Create list adapter
//		listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
//		((ListView) findViewById(R.id.contacts_list)).setAdapter(listAdapter);

		super.onResume();
	}

	@Override
	public void onResume(RestClient client) {
        // Keeping reference to rest client
        this.client = client;

		// Show everything
		findViewById(R.id.root).setVisibility(View.VISIBLE);
	}

	/**
	 * Called when "Logout" button is clicked.
	 *
	 * @param v
	 */
	public void onLogoutClick(View v) {
		SalesforceSDKManager.getInstance().logout(this);
	}

	/**
	 * Called when "Clear" button is clicked.
	 *
	 * @param v
	 */
	public void onClearClick(View v) {
		listAdapter.clear();
	}

	/**
	 * Called when "Fetch Contacts" button is clicked
	 *
	 * @param v
	 * @throws UnsupportedEncodingException
	 */
	public void onFetchContactsClick(View v) throws UnsupportedEncodingException {
        sendRequest("SELECT Name FROM Contact");
	}

	/**
	 * Called when "Fetch Accounts" button is clicked
	 *
	 * @param v
	 * @throws UnsupportedEncodingException
	 */
	public void onFetchAccountsClick(View v) throws UnsupportedEncodingException {
		sendRequest("SELECT Name FROM Account");
	}

	private void sendRequest(String soql) throws UnsupportedEncodingException {
		RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);

		client.sendAsync(restRequest, new AsyncRequestCallback() {
			@Override
			public void onSuccess(RestRequest request, final RestResponse result) {
				result.consumeQuietly(); // consume before going back to main thread
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {
							listAdapter.clear();
							JSONArray records = result.asJSONObject().getJSONArray("records");
							for (int i = 0; i < records.length(); i++) {
								listAdapter.add(records.getJSONObject(i).getString("Name"));
							}
						} catch (Exception e) {
							onError(e);
						}
					}
				});
			}

			@Override
			public void onError(final Exception exception) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(MainActivity.this,
								MainActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
								Toast.LENGTH_LONG).show();
					}
				});
			}
		});
	}
}
