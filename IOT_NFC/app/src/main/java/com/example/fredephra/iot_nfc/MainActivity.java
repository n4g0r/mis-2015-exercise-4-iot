package com.example.fredephra.iot_nfc;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    private TextView t_view;
    private NdefRecord[] records;
    private String hex = "";
    private String rawdata = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        t_view=(TextView) findViewById(R.id.tview);
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getIntent();
        // get tag
        Tag dat_tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        // get id out of tag
        byte[] nfc_id = dat_tag.getId();


        t_view.setText("");
        t_view.append(dat_tag.toString() + "\n");
        t_view.append("ID:"  + bytesToHexString(nfc_id) + "\n");

        Ndef ndef_tag = Ndef.get(dat_tag);
        if (ndef_tag != null) {

            NdefMessage ndef_tag_message = null;
            // try to get tag
            try {
                ndef_tag_message = ndef_tag.getCachedNdefMessage();
                t_view.append("Ndef Message: \n");
            } catch (Exception e) {
                System.out.println(e.getCause().toString());
            }


            if (ndef_tag_message != null) { // check if message is ndef
                records = ndef_tag_message.getRecords();
                t_view.append("still worrks ");
                // get messages out of the records
                for (int i = 0; i < records.length; ++i) {
                    byte[] byteText = records[i].getPayload();

                    String clearText = "";
                    for (byte sym : byteText) {
                        clearText += Byte.toString(sym);
                    }
                    t_view.append("Ndef Record Nr." + i + ": " + clearText + "\n\n");
                }
                // t_view.setText(tag.toString()+message+ record_string);
            }
            else t_view.append("Ndef has no message.\n\n");
        }

        MifareUltralight milf_tag = MifareUltralight.get(dat_tag);
        if(milf_tag != null){
            t_view.append("MifareUltralight Message:");
            readMifareUltralightTag(milf_tag);
            t_view.append("Hex: "  +hex +"\n");
            t_view.append("Rawdata: "+rawdata+"\n");
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

        return super.onOptionsItemSelected(item);
    }

    // helper fuctions
    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }

        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            stringBuilder.append(buffer);
        }

        return stringBuilder.toString();
    } // Source: http://stackoverflow.com/questions/6060312/
      // how-do-you-read-the-unique-id-of-an-nfc-tag-on-android

    public void readMifareUltralightTag(MifareUltralight mul){

        try {
            mul.connect();

            for (int i = 1; i < 16; ++i) {//64Byte/4Byte
                byte[] msg = mul.readPages(i*4);

                int endIndex = -1;

                for (int j = 0; j < msg.length; ++j)
                {
                    if (msg[j] == (byte) 0) {
                        endIndex = j-1;
                        break;
                    }
                }

                boolean toBreak = false;
                if (endIndex != -1)
                {
                    byte[] endMessage = new byte[endIndex + 1];
                    for (int j = 0; j <= endIndex; ++j)
                    {
                        endMessage[j] = msg[j];
                    }
                    msg = endMessage;
                    toBreak = true;
                }
                hex += bytesToHexString(msg);

                try {
                    String ascii_text = new String(msg, "US-ASCII");
                    t_view.append("ASCII: " + ascii_text+"\n");
                } catch (Exception e) {}
                for (byte b : msg) {
                    rawdata += Byte.toString(b) + " ";
                }

                if (toBreak)
                    break;
            }
        }
        catch (IOException e) {}
        finally
        {
            if (mul != null) {
                try {
                    mul.close();
                }
                catch (IOException e) {}
            }
        }
    }
    //Source: http://www.programcreek.com/java-api-examples/index.php?api=android.nfc.tech.MifareUltralight
}
