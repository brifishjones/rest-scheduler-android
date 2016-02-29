package com.bitfoxy.restscheduler;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ArrayList<String> mApiKeyList = null;  // contains the "autocomplete" list for user api keys

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "You Need a Budget (as well as a Rest Scheduler)", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        GetApiResponse api_task = new GetApiResponse();
        api_task.execute("", "/users");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_github:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/brifishjones/rest-scheduler"));
                startActivity(browserIntent);
                return true;

            default:
                // User's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        TextView edit_api_key = (TextView)findViewById(R.id.edit_api_key);
        String api_key = edit_api_key.getText().toString();
        GetApiResponse api_task = new GetApiResponse();

        // employee GET requests
        if (id == R.id.nav_shifts) {
            api_task.execute(api_key, "/users/" + api_key.substring(0, 1) + "/shifts");
        } else if (id == R.id.nav_coworkers) {
            api_task.execute(api_key, "/co-workers/" + api_key.substring(0, 1));
        } else if (id == R.id.nav_weekly_hours) {
            api_task.execute(api_key, "/weekly-hours/" + api_key.substring(0, 1));
        } else if (id == R.id.nav_managers) {
            api_task.execute(api_key, "/managers/" + api_key.substring(0, 1));
        // general GET requests (for testing only, no API key required)
        } else if (id == R.id.nav_users) {
            api_task.execute(api_key, "/users");
        } else if (id == R.id.nav_all_shifts) {
            api_task.execute(api_key, "/shifts");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void CreateApiKeyList(String users, int viewid, int resourceid)
    // Create an autocomplete list for the edit text given by the viewid.
    // view id for the edit text: viewid = R.id.edit_api_key
    // resource id for the activity: resourceid = R.layout.list_item

    {
        mApiKeyList = new ArrayList<String>();
        JSONArray jsonArray = null;
        AutoCompleteTextView list;
        ArrayAdapter<String> adapter;

        try {
            jsonArray = new JSONArray(users);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0, count = jsonArray.length(); i < count; i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                mApiKeyList.add(jsonObject.getString("id") + ":" + jsonObject.getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        list = (AutoCompleteTextView) findViewById(viewid);

        adapter = new ArrayAdapter<String>(this, resourceid, mApiKeyList);
        list.setAdapter(adapter);   // note: call setAdapter after setText otherwise autocomplete dropdown will show
        Log.i("INFO: ", String.valueOf(mApiKeyList));
    }

    class GetApiResponse extends AsyncTask<String, Integer, String>
    // Separate thread to process the API call
    {
        private Exception exception;

        @Override
        protected void onPreExecute() {
            TextView apiReturnValue;
            ProgressBar progressBar;

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            else
            {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            apiReturnValue = (TextView)findViewById(R.id.api_return_value);
            apiReturnValue.setText("");
            progressBar = (ProgressBar)findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... urls) {
        // url[0] is the auth_key and url[1] is the request
            final String API_URL = "https://gentle-brushlands-1205.herokuapp.com";

            try {
                URL url = new URL(API_URL + urls[1]);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                String auth_key = urls[0];
                Log.i("INFO", auth_key);
                urlConnection.setRequestProperty("authorization", auth_key);
                urlConnection.setRequestMethod("GET");

                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                }
                finally {
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            TextView apiReturnValue;
            ProgressBar progressBar;

            if(response == null) {
                response = "There was an error processing the API request";
            }

            progressBar = (ProgressBar)findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.GONE);

            apiReturnValue = (TextView)findViewById(R.id.api_return_value);
            apiReturnValue.setText(response);
            Log.i("INFO", response);

            if (mApiKeyList == null)
            {
                CreateApiKeyList(response, R.id.edit_api_key, R.layout.list_item);
            }

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }
}
