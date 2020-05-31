package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Utils.HttpHandler;
import com.example.myapplication.model.Country;
import com.example.myapplication.viewmodel.MyListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.myapplication.model.Country.ascendingOrder;
import static com.example.myapplication.model.Country.decendingOrder;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    public LinkedList<Country> mCountryList;
    public List<Country> mTempCountryList;
    public MyListAdapter mAdapter;
    private static final String JSON_URL = "https://api.covid19api.com/summary";

    private String mGTotalCases;
    private String mGTotalDeaths;
    private String mGTotalRecovered;
    private TextView mVTotalCases;
    private TextView mVTotalDeaths;
    private TextView mVTotalRecovered;
    private Timer mTimer;

    private boolean mAsc = false;
    private boolean mCmorethan = false;
    private boolean mClessthan = false;
    private boolean mDmorethan = false;
    private boolean mDlessthan = false;
    private boolean mRmorethan = false;
    private boolean mRlessthan = false;

    private EditText mEditTextMTCases;
    private EditText mEditTextLTCases;
    private EditText mEditTextMTDeaths;
    private EditText mEditTextLTDeaths;
    private EditText mEditTextMTRecovered;
    private EditText mEditTextLTRecovered;

    private boolean mEnableFilter = false;

    private String mCountryName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVTotalCases = findViewById(R.id.textViewVGTotalCases);
        mVTotalDeaths = findViewById(R.id.textViewVGTotalDeaths);
        mVTotalRecovered = findViewById(R.id.textViewVGTotalRecovered);
        mCountryList = new LinkedList<>();
        mTempCountryList = new ArrayList<Country>();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mAdapter = new MyListAdapter(mCountryList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
        mTimer = new Timer();
        if (ContextCompat.checkSelfPermission(MainActivity.this,
            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(MainActivity.this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            Geocoder geocoder = new Geocoder(getApplicationContext());
            for (String provider : lm.getAllProviders()) {
                @SuppressWarnings("ResourceType") Location location = lm.getLastKnownLocation(provider);
                if (location != null) {
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses != null && addresses.size() > 0) {
                            mCountryName = addresses.get(0).getCountryName();
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            Toast.makeText(getApplicationContext(), mCountryName, Toast.LENGTH_LONG).show();
        }

        final Handler handler = new Handler();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            new GetCountryList().execute();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        mTimer.schedule(doAsynchronousTask, 0, 1000 * 120);//execute in every 2 min
        Button applyFilter = findViewById(R.id.buttonApplyFilter);
        applyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEnableFilter = true;
                setupApplyFilterScreen(view);
            }
        });
        Button clearFilter = findViewById(R.id.buttonClearFilter);
        clearFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTempCountryList.size() != 0 && mEnableFilter) {
                    mEnableFilter = false;
                    mCmorethan = false;
                    mClessthan = false;
                    mDmorethan = false;
                    mDlessthan = false;
                    mRmorethan = false;
                    mRlessthan = false;
                    mCountryList.clear();
                    mCountryList.addAll(mTempCountryList);
                    moveCountryToTop(mCountryList);
                    mAdapter.notifyDataSetChanged();
                    mTempCountryList.clear();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTimer.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_options_menu, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ascDes:
                if (item.isChecked()) {
                    // If item already checked then unchecked it
                    item.setChecked(false);
                    mAsc = false;
                } else {
                    // If item is unchecked then checked it
                    item.setChecked(true);
                    mAsc = true;
                }
                sortList(mAsc);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sortList(boolean asc) {
        if (asc) {
            mCountryList.sort(ascendingOrder);
        } else {
            mCountryList.sort(decendingOrder);
        }
        moveCountryToTop(mCountryList);
        mAdapter.notifyDataSetChanged();
    }

    public void setupApplyFilterScreen(View view) {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog, null);

        mEditTextMTCases = (EditText) dialogView.findViewById(R.id.editTextMTCases);
        mEditTextMTCases.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enableOnlyEditText(mEditTextMTCases);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString().trim())) {
                    enableAllEditText();
                }
            }
        });
        mEditTextLTCases = (EditText) dialogView.findViewById(R.id.editTextLTCases);
        mEditTextLTCases.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enableOnlyEditText(mEditTextLTCases);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString().trim())) {
                    enableAllEditText();
                }
            }
        });
        mEditTextMTDeaths = (EditText) dialogView.findViewById(R.id.editTextMTDeaths);
        mEditTextMTDeaths.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enableOnlyEditText(mEditTextMTDeaths);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString().trim())) {
                    enableAllEditText();
                }
            }
        });
        mEditTextLTDeaths = (EditText) dialogView.findViewById(R.id.editTextLTDeaths);
        mEditTextLTDeaths.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enableOnlyEditText(mEditTextLTDeaths);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString().trim())) {
                    enableAllEditText();
                }
            }
        });
        mEditTextMTRecovered = (EditText) dialogView.findViewById(R.id.editTextMTRecovered);
        mEditTextMTRecovered.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enableOnlyEditText(mEditTextMTRecovered);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString().trim())) {
                    enableAllEditText();
                }
            }
        });
        mEditTextLTRecovered = (EditText) dialogView.findViewById(R.id.editTextLTRecovered);
        mEditTextLTRecovered.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enableOnlyEditText(mEditTextLTRecovered);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString().trim())) {
                    enableAllEditText();
                }
            }
        });

        Button button1 = (Button) dialogView.findViewById(R.id.buttonSubmit);
        Button button2 = (Button) dialogView.findViewById(R.id.buttonCancel);

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBuilder.dismiss();
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyFilter();
                dialogBuilder.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    private void applyFilter() {
        mTempCountryList.addAll(mCountryList);
        LinkedList<Country> countries = new LinkedList<>();
        if (mCmorethan) {
            long cMoreThan = Long.parseLong(mEditTextMTCases.getText().toString());
            for (int i = 0; i < mCountryList.size(); i++) {
                Country c = mCountryList.get(i);
                if (c.getmTotalCases() >= cMoreThan) {
                    if (c.getmName().equalsIgnoreCase(mCountryName)) {
                        countries.addFirst(c);
                    } else {
                        countries.add(c);
                    }
                }
            }
            mCountryList.clear();
            mCountryList.addAll(countries);
            mAdapter.notifyDataSetChanged();
        } else if (mClessthan) {
            long cLessThan = Long.parseLong(mEditTextLTCases.getText().toString());
            for (int i = 0; i < mTempCountryList.size(); i++) {
                Country c = mTempCountryList.get(i);
                if (c.getmTotalCases() <= cLessThan) {
                    if (c.getmName().equalsIgnoreCase(mCountryName)) {
                        countries.addFirst(c);
                    } else {
                        countries.add(c);
                    }
                }
            }
            mCountryList.clear();
            mCountryList.addAll(countries);
            mAdapter.notifyDataSetChanged();
        } else if (mDmorethan) {
            long dMorethan = Long.parseLong(mEditTextMTDeaths.getText().toString());
            for (int i = 0; i < mTempCountryList.size(); i++) {
                Country c = mTempCountryList.get(i);
                if (c.getmTotalDeaths() >= dMorethan) {
                    if (c.getmName().equalsIgnoreCase(mCountryName)) {
                        countries.addFirst(c);
                    } else {
                        countries.add(c);
                    }
                }
            }
            mCountryList.clear();
            mCountryList.addAll(countries);
            mAdapter.notifyDataSetChanged();
        } else if (mDlessthan) {
            long dLessThan = Long.parseLong(mEditTextLTDeaths.getText().toString());
            for (int i = 0; i < mTempCountryList.size(); i++) {
                Country c = mTempCountryList.get(i);
                if (c.getmTotalDeaths() <= dLessThan) {
                    if (c.getmName().equalsIgnoreCase(mCountryName)) {
                        countries.addFirst(c);
                    } else {
                        countries.add(c);
                    }
                }
            }
            mCountryList.clear();
            mCountryList.addAll(countries);
            mAdapter.notifyDataSetChanged();
        } else if (mRmorethan) {
            long rMorethan = Long.parseLong(mEditTextMTRecovered.getText().toString());
            for (int i = 0; i < mTempCountryList.size(); i++) {
                Country c = mTempCountryList.get(i);
                if (c.getmTotalRecovered() >= rMorethan) {
                    if (c.getmName().equalsIgnoreCase(mCountryName)) {
                        countries.addFirst(c);
                    } else {
                        countries.add(c);
                    }
                }
            }
            mCountryList.clear();
            mCountryList.addAll(countries);
            mAdapter.notifyDataSetChanged();
        } else if (mRlessthan) {
            long rLessThan = Long.parseLong(mEditTextLTRecovered.getText().toString());
            for (int i = 0; i < mTempCountryList.size(); i++) {
                Country c = mTempCountryList.get(i);
                if (c.getmTotalRecovered() <= rLessThan) {
                    if (c.getmName().equalsIgnoreCase(mCountryName)) {
                        countries.addFirst(c);
                    } else {
                        countries.add(c);
                    }
                }
            }
            mCountryList.clear();
            mCountryList.addAll(countries);
            mAdapter.notifyDataSetChanged();
        }
        countries.clear();
    }

    private void enableOnlyEditText(EditText editText) {

        if (editText.getId() == R.id.editTextMTCases) {
            mEditTextMTCases.setEnabled(true);
            mCmorethan = true;
        } else {
            mEditTextMTCases.setEnabled(false);
            mCmorethan = false;
        }
        if (editText.getId() == R.id.editTextLTCases) {
            mEditTextLTCases.setEnabled(true);
            mClessthan = true;
        } else {
            mEditTextLTCases.setEnabled(false);
            mClessthan = false;
        }
        if (editText.getId() == R.id.editTextMTDeaths) {
            mEditTextMTDeaths.setEnabled(true);
            mDmorethan = true;
        } else {
            mEditTextMTDeaths.setEnabled(false);
            mDmorethan = false;
        }

        if (editText.getId() == R.id.editTextLTDeaths) {
            mEditTextLTDeaths.setEnabled(true);
            mDlessthan = true;
        } else {
            mEditTextLTDeaths.setEnabled(false);
            mDlessthan = false;
        }
        if (editText.getId() == R.id.editTextMTRecovered) {
            mEditTextMTRecovered.setEnabled(true);
            mRmorethan = true;
        } else {
            mEditTextMTRecovered.setEnabled(false);
            mRmorethan = false;
        }
        if (editText.getId() == R.id.editTextLTRecovered) {
            mEditTextLTRecovered.setEnabled(true);
            mRlessthan = true;
        } else {
            mEditTextLTRecovered.setEnabled(false);
            mRlessthan = false;
        }

    }

    private void enableAllEditText() {
        mEditTextMTCases.setEnabled(true);
        mEditTextLTCases.setEnabled(true);
        mEditTextMTDeaths.setEnabled(true);
        mEditTextLTDeaths.setEnabled(true);
        mEditTextMTRecovered.setEnabled(true);
        mEditTextLTRecovered.setEnabled(true);
    }

    private class GetCountryList extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Json Data is downloading",
                Toast.LENGTH_LONG).show();

        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(Void... arg0) {

            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(getApplicationContext(), JSON_URL);
            if (jsonStr != null) {
                try {


                    JSONObject jsonObject = new JSONObject(jsonStr);
                    mGTotalCases = jsonObject.getJSONObject("Global").getString("TotalConfirmed");
                    mGTotalDeaths = jsonObject.getJSONObject("Global").getString("TotalDeaths");
                    mGTotalRecovered = jsonObject.getJSONObject("Global").getString("TotalRecovered");
                    // Getting JSON Array node
                    JSONArray obj = jsonObject.getJSONArray("Countries");

                    // looping through All Contacts
                    mCountryList.clear();
                    Log.e(TAG, " mCountryName : " + mCountryName);
                    for (int i = 0; i < obj.length(); i++) {
                        //getting the json object of the particular index inside the array
                        Country country = new Country();
                        String name = obj.getJSONObject(i).getString("Country");
                        long totalConfirmed = obj.getJSONObject(i).getLong("TotalConfirmed");
                        if (totalConfirmed == 0) continue;
                        long totalDeaths = obj.getJSONObject(i).getLong("TotalDeaths");
                        long totalRecovered = obj.getJSONObject(i).getLong("TotalRecovered");

                        country.setmName(name);
                        country.setmTotalCases(totalConfirmed);
                        country.setmTotalDeaths(totalDeaths);
                        country.setmTotalRecovered(totalRecovered);
                        mCountryList.add(country);
                    }
                    mCountryList.sort(decendingOrder);
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                "Json parsing error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        }
                    });

                }

            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                            "Couldn't get json from server. Check LogCat for possible errors!",
                            Toast.LENGTH_LONG).show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // progressBar.setVisibility(View.INVISIBLE);
            mVTotalCases.setText(mGTotalCases);
            mVTotalDeaths.setText(mGTotalDeaths);
            mVTotalRecovered.setText(mGTotalRecovered);
            if (mEnableFilter) {
                applyFilter();
            } else {
                moveCountryToTop(mCountryList);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private void moveCountryToTop(LinkedList<Country> countries) {
        LinkedList<Country> tempCountries = new LinkedList<>();
        tempCountries.addAll(countries);
        mCountryList.clear();
        for (int i = 0; i < tempCountries.size(); i++) {
            Country c = tempCountries.get(i);
            if (c.getmName().equalsIgnoreCase(mCountryName)) {
                mCountryList.addFirst(c);
            } else {
                mCountryList.add(c);
            }
        }
    }
}
