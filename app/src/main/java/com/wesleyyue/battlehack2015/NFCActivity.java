package com.wesleyyue.battlehack2015;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.nfc.Tag;
import android.os.Build;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.android.sdk.ab;

import java.util.Arrays;


public class NFCActivity extends Activity {

    NfcAdapter nfcDevice;


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        nfcDevice = NfcAdapter.getDefaultAdapter(this);

        nfcDevice.setNdefPushMessageCallback(new CreateNdefMessageCallback() {
            @Override
            public NdefMessage createNdefMessage(NfcEvent event) {
                NdefRecord uriRecord = NdefRecord.createUri(Uri.encode("21"));
                return new NdefMessage(new NdefRecord[]{uriRecord});
            }

        }, this, this);

        TextView text = (TextView) findViewById(R.id.info);
        Bundle b = getIntent().getExtras();
        int value = b.getInt("lock");



        if(value == 1){
            text.setText("Put your phone up to the lock to lock your bike.");
        } else {
            text.setText("Put your phone up to the lock to unlock your bike.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {

    }


    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nfc, menu);
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

        return super.onOptionsItemSelected(item);
    }

}
