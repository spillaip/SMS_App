package com.example.spillaip.sms_app;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    final int REQUEST_CODE_ASK_PERMISSIONS = 123;
    ListView listitem;
    String TAG = "MYSMS";
    double NAV;
    String _scheme;
    double _units;
    Date _msgDate;
    long _eventId = -1;
    List<Scheme> scheme = new ArrayList<>();
    List<String> finallv = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Validate and get Permissions
        validatePermission();
        //Get Calendars
        //int calStatus = get_calendars();
        //_eventId = insert_event("Hello");


        listitem = (ListView) findViewById(R.id.SMSList);

        if (ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED) {
            OrganizeInBox();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.READ_SMS"}, REQUEST_CODE_ASK_PERMISSIONS);
        }

    }

    public void OrganizeInBox() {
        String _hdfc = "_Blank"; //HDFC
        String _absl = "_Blank"; //ABCINV ABSL
        String _dspim = "_Blank"; //DSPAMC DSPIM
        String _ftindia = "_Blank"; //FTALRT FT India
        String _icicipruamc = "_Blank"; //ICICI PRU AMC IPRUMF
        String _idfc = "_Blank"; // IDFC Mutual
        Uri mSmsQueryUri = Uri.parse("content://sms/inbox");
        //List<String> messages = new ArrayList<String>();

        Cursor cursor = null;
        try {
            // Add SMS Read Permision At Runtime
            // Todo : If Permission Is Not GRANTED

            cursor = getContentResolver().query(mSmsQueryUri, null, null, null, null);
            if (cursor == null) {
                Log.i(TAG, "cursor is null. uri: " + mSmsQueryUri);

            }
            for (boolean hasData = cursor.moveToFirst(); hasData; hasData = cursor.moveToNext()) {

                final String _person = cursor.getString(cursor.getColumnIndexOrThrow("person"));

                final String _id = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                final String _thread_id = cursor.getString(cursor.getColumnIndexOrThrow("thread_id"));
                final String _address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                final String _date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                _msgDate = new Date(cursor.getLong(4));
                String formattedDate = new SimpleDateFormat("dd/MM/yyyy").format(_msgDate);
                final String _protocol = cursor.getString(cursor.getColumnIndexOrThrow("protocol"));
                final String _read = cursor.getString(cursor.getColumnIndexOrThrow("read"));
                final String _status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                final String _type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                final String _subject = cursor.getString(cursor.getColumnIndexOrThrow("subject"));
                final String _body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                final String _service_center = cursor.getString(cursor.getColumnIndexOrThrow("service_center"));
                final String _error_code = cursor.getString(cursor.getColumnIndexOrThrow("error_code"));
                final String _seen = cursor.getString(cursor.getColumnIndexOrThrow("seen"));
                final String _personName = getContactName(this.getApplicationContext(), _address);
                //Log.d("TAG","After contact Name");
                final String body = _person + ";" + _personName + ";" + _id + ";" + _thread_id + ";" + _address + ";" + formattedDate + ";" + _protocol + ";" + _read + ";" + _status + ";" + _type + ";" + _subject + ";" + _body + ";" + _service_center + ";" + _error_code + ";" + _seen;

                if (_address.contains("ABCINV")) {
                    if (_body.contains("processed"))
                        _absl = getBIRLA(body, scheme, _msgDate);

                }

                if (_address.contains("DSPAMC")) {
                    if (_body.contains("processed"))
                        _dspim = getDSP(body, scheme, _msgDate);

                }

                if (_address.contains("HDFC")) {
                    if (_body.contains("processed"))
                        _hdfc = getHDFC(body, scheme, _msgDate);

                }

                if (_address.contains("IDFCMF")) {


                    if (_body.contains("processed"))
                        _idfc = getIDFC(body, scheme, _msgDate);

                }

                if (_address.contains("IPRUMF")) {

                    if (_body.contains("processed"))
                        _icicipruamc = getICICIPRUAMC(body, scheme, _msgDate);

                }



            }
            //ToDo: Get distinct schemes from all the transactions by copying to Set
            //ToDo: Get sum of Cost, Units, Avg Price for each scheme
            //Todo: create a ArrayList with this date
            //Todo: Create a ListView to show the ArrayList
            for (int i = 0; i < scheme.size(); i++) {
                Log.d(TAG, scheme.get(i).toString());
            }
            processData((ArrayList<Scheme>) scheme);
            listitem.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, finallv));
        } catch (Exception e) {
            Log.e(TAG, "Error is " + e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    public String getContactName(Context context, String phoneNumber) {
        String contactName = "Unknown";
        if (ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.READ_CONTACTS") == PackageManager.PERMISSION_GRANTED) {

            ContentResolver cr = context.getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(phoneNumber));
            Cursor cursor = cr.query(uri,
                    new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            if (cursor == null) {
                return null;
            }

            if (cursor.moveToFirst()) {
                contactName = cursor.getString(cursor
                        .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                if (contactName == null) {
                    contactName = "Unknown";
                }
            }
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return contactName;
    }

    public String getHDFC(String body, List<Scheme> scheme, Date trxDate) {
        String _output = "Unknown";
        String[] first = body.split(" under ");
        String[] second = first[1].split(" for ");

        String _schemeName = second[0];
        String _folio[] = first[0].split("Folio");
        String _rs[] = body.split("Rs. ");
        String _amount[] = _rs[1].split(" has ");
        String _getNav[] = body.split("NAV of ");
        String _NAV[] = _getNav[1].split(" for ");
        String _getUnits[] = _NAV[1].split(" units");
        String _units = _getUnits[0];

        _output = _folio[1] + "," + _schemeName + "," + _amount[0] + "," + _NAV[0] + "," + _units;
        scheme.add(new Scheme("HDFC", _schemeName, Double.parseDouble(_NAV[0].replaceAll(",", "")), Double.parseDouble(_units.replaceAll(",", "")), Double.parseDouble(_amount[0].replaceAll(",", "")), trxDate));

        return _output;
    }

    public String getBIRLA(String body, List<Scheme> scheme, Date trxDate) {
        // Ur request in Folio 1013037701 for Purchase-SIP in ABSL Tax Relef '96 Fund-ELSS-Growth-DIR of Rs. 500.00 with NAV of Rs.31.99 has been processed.
        String _output = "Unknown";
        String[] first = body.split(" processed."); //Ur request in Folio 1013037701 for Purchase-SIP in ABSL Tax Relef '96 Fund-ELSS-Growth-DIR of Rs. 500.00 with NAV of Rs.31.99 has been processed.
        String[] second = first[0].split("in "); //["Ur request ","Folio 1013037701 for Purchase-SIP ","ABSL Tax Relef '96 Fund-ELSS-Growth-DIR of Rs. 500.00 with NAV of Rs.31.99 has been processed."]

        String _schemeName[] = second[2].split("of"); //"ABSL Tax Relef '96 Fund-ELSS-Growth-DIR "," Rs. 500.00 with NAV "," Rs.31.99 has been processed."
        String _prefolio[] = body.split("for");
        String _folio[] = _prefolio[0].split("Folio ");
        String _rs[] = body.split("Rs. "); //["Ur request in Folio 1013037701 for Purchase-SIP in ABSL Tax Relef '96 Fund-ELSS-Growth-DIR of ","500.00 with NAV of Rs.31.99 has been processed."]
        String _amount[] = _rs[1].split(" with ");
        String _getNav[] = body.split("NAV of Rs.");
        String _NAV[] = _getNav[1].split(" has ");
        //String _getUnits[] =
        double _units = Double.parseDouble(_NAV[0]) / Double.parseDouble(_amount[0].replaceAll(",", ""));

        _output = _folio[1] + "," + _schemeName[0] + "," + _amount[0] + "," + _NAV[0] + "," + _units;

        scheme.add(new Scheme("ABSL", _schemeName[0], Double.parseDouble(_NAV[0].replaceAll(",", "")), _units, Double.parseDouble(_amount[0].replaceAll(",", "")), trxDate));
        return _output;
    }

    public String getDSP(String body, List<Scheme> scheme, Date trxDate) {
        // Transaction CONFIRMATION Alert: SIP Purchase request of Rs 500.00 in T.I.G.E.R Fund - Dir = G in your folio ***915/26, has been processed with an NAV of Rs.87.671 on 14-Jan-2019. T&C Apply.
        String _output = "Unknown";
        String[] first = body.split(" in "); //Ur request in Folio 1013037701 for Purchase-SIP in ABSL Tax Relef '96 Fund-ELSS-Growth-DIR of Rs. 500.00 with NAV of Rs.31.99 has been processed.

        String _schemeName = first[1];
        String _prefolio[] = body.split("folio ");
        String _folio[] = _prefolio[1].split(",");
        String _rs[] = body.split("Rs "); //["Ur request in Folio 1013037701 for Purchase-SIP in ABSL Tax Relef '96 Fund-ELSS-Growth-DIR of ","500.00 with NAV of Rs.31.99 has been processed."]
        String _amount[] = _rs[1].split(" in ");
        String _getNav[] = body.split("NAV of Rs.");
        String _NAV[] = _getNav[1].split(" on ");
        //String _getUnits[] =
        double _units = Double.parseDouble(_NAV[0]) / Double.parseDouble(_amount[0].replaceAll(",", ""));

        _output = _folio[1] + "," + _schemeName + "," + _amount[0] + "," + _NAV[0] + "," + _units;

        scheme.add(new Scheme("DSPIM", _schemeName, Double.parseDouble(_NAV[0].replaceAll(",", "")), _units, Double.parseDouble(_amount[0].replaceAll(",", "")), trxDate));
        return _output;
    }

    public String getICICIPRUAMC(String body, List<Scheme> scheme, Date trxDate) {
        // Greetings, Your SIP Purchase request for amount of Rs.1,000.00 in Folio 878159/43 in LTEF (Tax Saving) - DP - Growth has been processed for Net Asset Value of 385.35 ICICI Prudential MF.
        String _output = "Unknown";
        String[] first = body.split(" in "); //Ur request in Folio 1013037701 for Purchase-SIP in ABSL Tax Relef '96 Fund-ELSS-Growth-DIR of Rs. 500.00 with NAV of Rs.31.99 has been processed.
        String[] second = first[2].split(" has");
        String _schemeName = second[0];
        //String _prefolio[] = body.split("folio ");
        String _folio = second[1];
        String _rs[] = body.split("Rs."); //["Ur request in Folio 1013037701 for Purchase-SIP in ABSL Tax Relef '96 Fund-ELSS-Growth-DIR of ","500.00 with NAV of Rs.31.99 has been processed."]
        String _amount[] = _rs[1].split(" in ");
        String _getNav[] = body.split("Net Asset Value of ");
        String _NAV[] = _getNav[1].split(" ICICI");
        //String _getUnits[] =
        double _units = Double.parseDouble(_NAV[0]) / Double.parseDouble(_amount[0].replaceAll(",", ""));

        _output = _folio + "," + _schemeName + "," + _amount[0] + "," + _NAV[0] + "," + _units;

        scheme.add(new Scheme("ICICIPRU", _schemeName, Double.parseDouble(_NAV[0].replaceAll(",", "")), _units, Double.parseDouble(_amount[0].replaceAll(",", "")), trxDate));
        return _output;
    }

    public String getIDFC(String body, List<Scheme> scheme, Date trxDate) {
        // Your Purchase in Folio 903033/14 in IDFC Multi Cap Fund-Growth-(Direct Plan) (formerly known as IDFC Premier Equity Fund-Growth-(Direct Plan) for Rs 2,000.00 at NAV of 93.14 is processed
        String _output = "Unknown";
        String[] first = body.split(" in "); //Ur request in Folio 1013037701 for Purchase-SIP in ABSL Tax Relef '96 Fund-ELSS-Growth-DIR of Rs. 500.00 with NAV of Rs.31.99 has been processed.
        String[] second = first[2].split(" has");
        String _schemeName = second[0];
        //String _prefolio[] = body.split("folio ");
        String _folio = second[1];
        String _rs[] = body.split("Rs."); //["Ur request in Folio 1013037701 for Purchase-SIP in ABSL Tax Relef '96 Fund-ELSS-Growth-DIR of ","500.00 with NAV of Rs.31.99 has been processed."]
        String _amount[] = _rs[1].split(" in ");
        String _getNav[] = body.split("NAV of ");
        String _NAV[] = _getNav[1].split(" ICICI");
        //String _getUnits[] =
        double _units = Double.parseDouble(_NAV[0]) / Double.parseDouble(_amount[0].replaceAll(",", ""));

        _output = _folio + "," + _schemeName + "," + _amount[0] + "," + _NAV[0] + "," + _units;

        scheme.add(new Scheme("IDFCMF", _schemeName, Double.parseDouble(_NAV[0].replaceAll(",", "")), _units, Double.parseDouble(_amount[0].replaceAll(",", "")), trxDate));
        return _output;
    }

    public int get_calendars() {

        // Projection array. Creating indices for this array instead of doing
        // dynamic lookups improves performance.
        final String[] EVENT_PROJECTION = new String[]{
                CalendarContract.Calendars._ID,                           // 0
                CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
                CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
        };

        // The indices for the projection array above.
        final int PROJECTION_ID_INDEX = 0;
        final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
        final int PROJECTION_DISPLAY_NAME_INDEX = 2;
        final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

        // Run query
        Cursor cur = null;
        ContentResolver cr = getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?))";
        //+ CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
        //+ CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
        String[] selectionArgs = new String[]{"spillaip@gmail.com"};
        // Submit the query and get a Cursor object back.
        // ToDo : Query MF_TRX only
        cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

        // Use the cursor to step through the returned records
        while (cur.moveToNext()) {
            long calID = 0;
            String displayName = null;
            String accountName = null;
            String ownerName = null;

            // Get the field values
            calID = cur.getLong(PROJECTION_ID_INDEX);
            displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
            accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
            ownerName = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX);


            Log.d(TAG, "Cal Info: " + calID + "-" + displayName + "-" + accountName + "-" + ownerName);

        }


        return -1;
    }

    public int new_calendar() {
        //Todo : Create a calendar named MF_TRX and return the calendar ID and store in STORAGE
        //https://developer.android.com/guide/topics/providers/calendar-provider
        return -1;
    }

    public long insert_event(Date calDate, String calData) {

        long calID = 14;
        long startMillis = 0;
        long endMillis = 0;
        int dt = calDate.getDay();
        int mm = calDate.getMonth();
        int yr = calDate.getYear();
        int minute = calDate.getMinutes();
        int hr = calDate.getHours();
        Calendar beginTime = Calendar.getInstance();
        //beginTime.set(yr, mm, dt);
        beginTime.setTime(calDate);


        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        //endTime.set(yr, mm, dt);
        endTime.setTime(calDate);
        endMillis = endTime.getTimeInMillis();

        //Log.d(TAG, "After Date parsing");

        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, calData);
        values.put(CalendarContract.Events.DESCRIPTION, calData);
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.ALL_DAY, 1);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Calcutta");
        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

        // get the event ID that is the last element in the Uri
        // Todo: Uncomment to update to the calendar
        //long eventID = Long.parseLong(uri.getLastPathSegment());


        return -1;
    }

    public int validate_event(int eventID) {
        //Todo : Retrieve eventId and return the status
        return -1;

    }

    public void validatePermission() {
        if (ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.READ_CALENDAR") == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "READ_CALENDAR present");
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.READ_CALENDAR"}, REQUEST_CODE_ASK_PERMISSIONS);
        }
        if (ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.WRITE_CALENDAR") == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "WRITE_CALENDAR present");
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.WRITE_CALENDAR"}, REQUEST_CODE_ASK_PERMISSIONS);
        }
        if (ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "READ_SMS present");
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.READ_SMS"}, REQUEST_CODE_ASK_PERMISSIONS);
        }
        if (ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.READ_CONTACTS") == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "READ_CONTACTS present");
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.READ_CONTACTS"}, REQUEST_CODE_ASK_PERMISSIONS);
        }

    }

    public void processData(ArrayList<Scheme> alSchemes) {

        //Get distinct schemes
        Set<String> setSchemes = new HashSet<String>();

        for (int counter = 0; counter < alSchemes.size(); counter++) {
            Log.d(TAG, String.valueOf(alSchemes.get(counter)));
            setSchemes.add(alSchemes.get(counter).Fund);
        }

        Log.d(TAG, "Unique gas count: " + setSchemes.size());
        //Get sum of cost, units, avg price for each scheme

        double sum_cost = 0.00;
        double sum_units = 0.00;
        double sum_price = 0.00;
        double avg_price = 0.00;

        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);
        DecimalFormat df2 = new DecimalFormat("#.##");
        df2.setRoundingMode(RoundingMode.CEILING);

        int n = setSchemes.size();
        String arr[] = new String[n];
        arr = setSchemes.toArray(arr);
        String AMC = "_unknown";


        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < alSchemes.size(); j++) {
                if (arr[i] == alSchemes.get(j).Fund) {
                    sum_cost += alSchemes.get(j).Amount;
                    sum_units += alSchemes.get(j).Units;
                    sum_price += alSchemes.get(j).NAV;
                    AMC = alSchemes.get(j).AMC;
                } //if

            } // for
            avg_price = sum_cost / sum_units;
            Log.d(TAG, AMC + "-" + arr[i] + " Cost: " + df2.format(sum_cost) + " Units: " + df.format(sum_units) + " Avg Price:" + df.format(avg_price));
            //create arraylist and return
            finallv.add(AMC + "-" + arr[i] + " Cost: " + df2.format(sum_cost) + " Units: " + df.format(sum_units) + " Avg Price:" + df.format(avg_price));
        }
    }
}
