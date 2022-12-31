package com.example.nfcadapter;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.nfc.tech.TagTechnology.*;
import android.nfc.tech.Ndef;
import android.os.Parcelable;
import android.util.Log;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements NfcAdapter.CreateNdefMessageCallback {

    Button read, write;
    EditText message;
    private static final String TAG = MainActivity.class.getSimpleName();
    Tag tag;
    NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    TextView nfc_contents;
    String input = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        read = (Button) findViewById(R.id.read);
        write = (Button) findViewById(R.id.write);
        message = (EditText) findViewById(R.id.message);

        message.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;

                return handled;

            }
        });
        write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                input = message.getText().toString();
                writeTag(tag, input);
                //or...
                createNFCMessage(input);
            }
        });
        read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //onActivityResult will handle any incoming NFC beam with 4
                //different read methods.
                Toast.makeText(getApplicationContext(), "Hold your phone close to the NFC signal now", Toast.LENGTH_LONG).show();

            }
        });

        }
    public void writeTag(Tag tag, String tagText) {
        MifareUltralight ultralight = MifareUltralight.get(tag);
        try {
            ultralight.connect();
            ultralight.writePage(4, "abcd".getBytes(Charset.forName("US-ASCII")));
            ultralight.writePage(5, "efgh".getBytes(Charset.forName("US-ASCII")));
            ultralight.writePage(6, "ijkl".getBytes(Charset.forName("US-ASCII")));
            ultralight.writePage(7, "mnop".getBytes(Charset.forName("US-ASCII")));
        } catch (IOException e) {
            Log.e(TAG, "IOException while writing MifareUltralight...", e);
        } finally {
            try {
                ultralight.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException while closing MifareUltralight...", e);
            }
        }
    }



    public void createNFCMessage(String text) {
        try{

            NdefRecord records = new NdefRecord(
                    NdefRecord.TNF_MIME_MEDIA ,
                    "application/vnd.com.example.android.beam".getBytes(Charset.forName("US-ASCII")),
                    new byte[0], "Beam me up, Android!".getBytes(Charset.forName("US-ASCII")));

            // NdefRecord[] records = { createRecord(text) };
            NdefMessage message = new NdefMessage(records);
            Ndef ndef = Ndef.get(tag);

            ndef.connect();

            ndef.writeNdefMessage(message);

        }catch(Exception e)
        {

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        try{
            processIntent(intent);
        }catch(Exception e)
        {
            Toast.makeText(getApplicationContext(), "Cannot do processIntents() ", Toast.LENGTH_LONG).show();
        }
        try{
            readFromIntent(intent);
        }catch(Exception e)
        {
            Toast.makeText(getApplicationContext(), "Cannot do readFromIntent() ", Toast.LENGTH_LONG).show();
        }
        try{
            String input = Arrays.toString(getNdefMessages(intent));
            Toast.makeText(getApplicationContext(), input,
                    Toast.LENGTH_LONG).show();
            //setNdefMessages(input);
        }catch(Exception e)
        {
            Toast.makeText(getApplicationContext(), "Cannot do setNdefMessages() ", Toast.LENGTH_LONG).show();
        }
        try{
            String input = readTag(tag);
            Toast.makeText(getApplicationContext(), input, Toast.LENGTH_LONG).show();
        }catch(Exception e)
        {
            Toast.makeText(getApplicationContext(), "Cannot do setNdefMessages() ", Toast.LENGTH_LONG).show();
        }
    }


    NdefMessage[] getNdefMessages(Intent intent) {
        // Parse the intent
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            else {
                // Unknown tag type
                byte[] empty = new byte[] {};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[] {record});
                msgs = new NdefMessage[] {msg};
            }
        }
        else {
            finish();
        }
        return msgs;
    }

    public void setNdefMessages(String input)
    {
        Toast.makeText(getApplicationContext(), input,
                Toast.LENGTH_LONG).show();
    }
    private void readFromIntent(Intent intent)
    {
        try{

            String action = intent.getAction();
            if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                    || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                    || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
                Parcelable[] rawMsg = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                NdefMessage[] msgs = null;
                if (rawMsg != null) {
                    msgs = new NdefMessage[rawMsg.length];
                    for (int i = 0; i < rawMsg.length; i++) {
                        msgs[i] = (NdefMessage) rawMsg[i];
                    }
                    //insert into our database
                    buildTagView(msgs);
                }

            }
        }catch(Exception e)
        {
            Toast.makeText(getApplicationContext(), "Cannot do readFromIntent() ",
                    Toast.LENGTH_LONG).show();
        }
    }
    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        Toast.makeText(getApplicationContext(),new String(msg.getRecords()[0].getPayload()), Toast.LENGTH_LONG).show();
        //insert into our database

        Toast.makeText(getApplicationContext(), String.valueOf(msg), Toast.LENGTH_LONG).show();
    }
    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private void buildTagView(NdefMessage[] msgs)
    {
        if(msgs == null || msgs.length == 0)
        {
            return;
        }

        String text = "";
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
        int languageCodeLength = payload[0] & 0063;

        try{
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
            //nfc_contents.setText(text);
            //insert into our database
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();


        }catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "Cannot do readFromIntent() ",
                    Toast.LENGTH_LONG).show();
        }

    }

    public String readTag(Tag tag) {
        MifareUltralight mifare = MifareUltralight.get(tag);
        try {
            mifare.connect();
            byte[] payload = mifare.readPages(4);
            return new String(payload, Charset.forName("US-ASCII"));
        } catch (IOException e) {
            Log.e(TAG, "IOException while reading MifareUltralight message...", e);
        } finally {
            if (mifare != null) {
                try {
                    mifare.close();
                }
                catch (IOException e) {
                    Log.e(TAG, "Error closing tag...", e);
                }
            }
        }
        return null;
    }
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null)
        {
            //NdefMessage msg = new NdefMessage();
            //nfcAdapter.enableForegroundNdefPush(this, msg);
        }
    }
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundNdefPush(this);
    }


    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
        return null;
    }


}
