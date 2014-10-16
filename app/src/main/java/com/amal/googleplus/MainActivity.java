package com.amal.googleplus;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.plus.model.people.Person;


public class MainActivity extends Activity implements View.OnClickListener,
        ConnectionCallbacks, OnConnectionFailedListener {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_RESOLVE_ERR = 9000;

    private ProgressDialog mConnectionProgressDialog;
    private PlusClient mPlusClient;
    private ConnectionResult mConnectionResult;
    
    Button ShareButton,GetData,ok_btn;
    Dialog get_data_dialog;
    TextView name,url,id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlusClient = new PlusClient.Builder(this, this, this)
        .setActions("http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity")
        .build();
        setContentView(R.layout.activity_main);
        // Progress bar to be displayed if the connection failure is not resolved.
        mConnectionProgressDialog = new ProgressDialog(this);
        mConnectionProgressDialog.setMessage("Signing in...");
        
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        ShareButton = (Button) findViewById(R.id.post_button);
        GetData = (Button)findViewById(R.id.get_data_button);
        GetData.setOnClickListener(new View.OnClickListener() {
			String disp_name,disp_url,disp_id;
			
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if( mPlusClient.isConnected())
				{
				get_data_dialog = new Dialog(MainActivity.this);
				get_data_dialog.setContentView(R.layout.get_data_frag);
				get_data_dialog.setTitle("User Details");
				name = (TextView)get_data_dialog.findViewById(R.id.get_name);
				url = (TextView)get_data_dialog.findViewById(R.id.get_url);
				id = (TextView)get_data_dialog.findViewById(R.id.get_id);
				ok_btn = (Button)get_data_dialog.findViewById(R.id.ok_button);
				ok_btn.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						get_data_dialog.cancel();
					}
					
				});			
		            Person currentPerson = mPlusClient.getCurrentPerson();
		            disp_name = currentPerson.getDisplayName();
		            disp_url = currentPerson.getUrl();
		            disp_id = currentPerson.getId();
		            name.setText(disp_name);
		            url.setText(disp_url);
		            id.setText(disp_id);
					get_data_dialog.show();
			}else{
				Toast.makeText(getApplicationContext(), "Please Sign In", Toast.LENGTH_LONG).show();
			}}
		});
        ShareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              // Launch the Google+ share dialog with attribution to your app.
              Intent shareIntent = new PlusShare.Builder(MainActivity.this)
                  .setType("text/plain")
                  .setText("Learn2Crack is a website for beginners to learn Android Programming")
                  .setContentUrl(Uri.parse("http://www.learn2crack.com"))
                  .getIntent();
               startActivityForResult(shareIntent, 0);
            } 
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPlusClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPlusClient.disconnect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
      if (mConnectionProgressDialog.isShowing()) {
        // The user clicked the sign-in button already. Start to resolve
        // connection errors. Wait until onConnected() to dismiss the
        // connection dialog.
        if (result.hasResolution()) {
          try {
                   result.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
           } catch (SendIntentException e) {
                   mPlusClient.connect();
           }
        }
      }
      // Save the result and resolve the connection failure upon a user click.
      mConnectionResult = result;
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == REQUEST_CODE_RESOLVE_ERR && responseCode == RESULT_OK) {
            mConnectionResult = null;
            mPlusClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        String accountName = mPlusClient.getAccountName();
        Toast.makeText(this, accountName + " is connected.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this,  " Disconnected.", Toast.LENGTH_LONG).show();
        Log.d(TAG, "disconnected");
    }

	@Override
	 public void onClick(View view) {
		  if (view.getId() == R.id.sign_in_button && !mPlusClient.isConnected()) {
            if (mConnectionResult == null) {
                mConnectionProgressDialog.show();
            } else {
                try {
                    mConnectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
                } catch (SendIntentException e) {
                    // Try connecting again.
                    mConnectionResult = null;
                    mPlusClient.connect();
                }
            }
        }
    }
}