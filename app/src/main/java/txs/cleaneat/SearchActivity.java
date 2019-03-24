package txs.cleaneat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SearchActivity extends AppCompatActivity {

    // Request types for a multi-purpose request method
    private enum RequestType {ESTABLISHMENT, BUSINESS, REGION, AUTHORITY}

    // API URL
    private final String BUSINESSTYPES_URL = "http://api.ratings.food.gov.uk/BusinessTypes/basic";
    private final String REGIONS_URL = "http://api.ratings.food.gov.uk/Regions/basic";
    private final String AUTHORITIES_URL = "http://api.ratings.food.gov.uk/Authorities";

    // Sorting options
    private final String RELEVANCE = "relevance";
    private final String RATING_DESC = "rating";
    private final String RATING_ASC = "desc_rating";
    private final String DISTANCE = "distance";
    private final String ALPHA_ASC = "alpha";
    private final String ALPHA_DESC = "desc_alpha";

    // Views
    private ListView listView;
    private ProgressBar progressBar;
    private SearchView search;
    private LinearLayout sortPanel;
    private LinearLayout filterPanel;
    private ImageButton sortButton;
    private ImageButton filterButton;
    private Spinner authoritySpinner;
    private TextView noItems;

    // ListView elements
    private ArrayList<Establishment> establishments = new ArrayList<>();
    private EstablishmentListAdapter establishmentsAdapter;

    // Spinners
    private ArrayList<SpinnerRecord> businessTypes = new ArrayList<>();
    private ArrayAdapter<SpinnerRecord> businessTypesAdapter;

    private ArrayList<SpinnerRecord> regions = new ArrayList<>();
    private ArrayAdapter<SpinnerRecord> regionsAdapter;

    private ArrayList<SpinnerRecord> authorities = new ArrayList<>();
    private ArrayAdapter<SpinnerRecord> authoritiesAdapter;

    // Location
    private final int FINE_LOCATION_PERMISSION = 1;
    private LocationManager locManager;
    private LocationListener locListener;
    private double longitude = 0 , latitude = 0;

    // Filters
    private String searchTerm = "";
    private String sortType = RELEVANCE;
    private int minRating = 0;
    private int radius = -1;
    private SpinnerRecord businessType = new SpinnerRecord(-1, "All");
    private SpinnerRecord region = new SpinnerRecord(-1, "Any");
    private SpinnerRecord authority = new SpinnerRecord(-1, "Any", "");

    // Favourites
    private Set<String> favourites;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Get views
        noItems = findViewById(R.id.searchNoItems);

        listView = findViewById(R.id.establishmentList);
        progressBar = findViewById(R.id.progressBar);

        sortPanel = findViewById(R.id.sortPanel);
        filterPanel = findViewById(R.id.filterPanel);

        sortButton = findViewById(R.id.sortButton);
        filterButton = findViewById(R.id.filterButton);

        RadioGroup sortGroup = findViewById(R.id.sortGroup);

        // Set up ListView
        establishmentsAdapter = new EstablishmentListAdapter(this, 0, establishments);
        listView.setAdapter(establishmentsAdapter);

        // Set up spinners
        Spinner businessTypeSpinner = findViewById(R.id.spinnerBusiness);
        businessTypesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, businessTypes);
        businessTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        businessTypeSpinner.setAdapter(businessTypesAdapter);
        Spinner regionSpinner = findViewById(R.id.spinnerRegion);
        regionsAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, regions);
        regionSpinner.setAdapter(regionsAdapter);
        regionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        authoritySpinner = findViewById(R.id.spinnerAuthority);
        authoritiesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, authorities);
        authoritySpinner.setAdapter(authoritiesAdapter);
        authoritiesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner minRatingSpinner = findViewById(R.id.spinnerMinRating);
        ArrayAdapter<String> ratingAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[] {"0", "1", "2", "3", "4", "5"});
        minRatingSpinner.setAdapter(ratingAdapter);
        ratingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Search input event listener
        search = findViewById(R.id.searchBox);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchTerm = search.getQuery().toString().trim();
                requestItems();
                return true;
            }
        });

        // Radius input event listener
        final EditText editMiles = findViewById(R.id.editMiles);
        editMiles.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    editMiles.clearFocus();
                    return true;
                }
                return false;
            }
        });
        if (TextUtils.isEmpty(editMiles.getText().toString().trim())) {
            radius = -1;
        }
        editMiles.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    EditText et = (EditText) v;
                    String text = et.getText().toString();
                    boolean changed = false;
                    if (text.isEmpty()) {
                        if (radius != -1) {
                            radius = -1;
                            changed = true;
                        }
                    } else {
                        int newRadius = Integer.parseInt(text);
                        if (newRadius != radius) {
                            radius = newRadius;
                            changed = true;
                        }
                    }
                    if (changed) {
                        if (latitude == 0 && longitude == 0){
                            Toast.makeText(SearchActivity.this, getResources().getString(R.string.toast_location), Toast.LENGTH_SHORT).show();
                        }
                        requestItems();
                    }
                }
            }
        });

        // Sorting option event listener
        sortGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioAlphaAsc:
                        sortType = ALPHA_ASC;
                        break;
                    case R.id.radioAlphaDesc:
                        sortType = ALPHA_DESC;
                        break;
                    case R.id.radioDistance:
                        sortType = DISTANCE;
                        if (latitude == 0 && longitude == 0){
                            Toast.makeText(SearchActivity.this, getResources().getString(R.string.toast_location), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.radioRatingAsc:
                        sortType = RATING_ASC;
                        break;
                    case R.id.radioRatingDesc:
                        sortType = RATING_DESC;
                        break;
                    case R.id.radioRelevance:
                        sortType = RELEVANCE;
                        break;
                }
                requestItems();
            }
        });

        // Set up spinners event listeners
        businessTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                businessType = (SpinnerRecord) parent.getItemAtPosition(position);
                requestItems();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        regionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                region = (SpinnerRecord) parent.getItemAtPosition(position);
                requestSpinnerRecords(RequestType.AUTHORITY, AUTHORITIES_URL);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        authoritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                authority = (SpinnerRecord) parent.getItemAtPosition(position);
                requestItems();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        minRatingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                minRating = position;
                requestItems();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Set up listview event listeners
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Establishment e = (Establishment) parent.getItemAtPosition(position);
                Intent intent = new Intent(SearchActivity.this, EstablishmentActivity.class);
                intent.putExtra("id", e.getId());
                intent.putExtra("name", e.getName());
                intent.putExtra("index", position);
                startActivityForResult(intent, 1);
            }
        });

        // Location setup
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.e("location", location.getLatitude() + " " + location.getLongitude());
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }
            @Override
            public void onProviderEnabled(String provider) { }
            @Override
            public void onProviderDisabled(String provider) { }
        };
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setMessage(getResources().getString(R.string.dialog_permission))
                        .setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestLocPerms();
                            }
                        })
                        .create()
                        .show();
            } else {
                requestLocPerms();
            }
        } else {
            attachLocManager();
        }

        // Set up favourites
        sharedPref = getSharedPreferences("CleanEatFavourites", Context.MODE_PRIVATE);

        // Initial requests
        requestSpinnerRecords(RequestType.BUSINESS, BUSINESSTYPES_URL);
        requestSpinnerRecords(RequestType.REGION, REGIONS_URL);
        requestSpinnerRecords(RequestType.AUTHORITY, AUTHORITIES_URL);
    }

    // Location setup
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    attachLocManager();
                }
            }
        }
    }
    public void attachLocManager() {
        try {
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locListener);
        } catch (SecurityException err) {
            Log.e("error", err.toString());
        }
    }
    public void requestLocPerms() {
        ActivityCompat.requestPermissions(SearchActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION);
    }

    public void onClickSort(View view) {
        filterPanel.setVisibility(View.GONE);
        filterButton.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorBackgroundLight));
        if (sortPanel.getVisibility() == View.VISIBLE) {
            sortPanel.setVisibility(View.GONE);
            sortButton.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorBackgroundLight));
        } else {
            sortPanel.setVisibility(View.VISIBLE);
            sortButton.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorPrimary));
        }
    }

    public void onClickFilter(View view) {
        sortPanel.setVisibility(View.GONE);
        sortButton.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorBackgroundLight));
        if (filterPanel.getVisibility() == View.VISIBLE) {
            filterPanel.setVisibility(View.GONE);
            filterButton.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorBackgroundLight));
        } else {
            filterPanel.setVisibility(View.VISIBLE);
            filterButton.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorPrimary));
        }
    }

    private String getURL() {
        String url = "http://api.ratings.food.gov.uk/Establishments?";
        url += "name=" + searchTerm;
        if (latitude != 0 && longitude != 0) {
            url += "&longitude=" + longitude + "&latitude=" + latitude;
        }
        url += "&sortOptionKey=" + sortType;
        if (!businessType.getId().equals("-1")) {
            url += "&businessTypeId=" + businessType.getId();
        }
        if (!authority.getId().equals("-1")) {
            url += "&localAuthorityId=" + authority.getId();
        }
        if (radius != -1) {
            url += "&maxDistanceLimit=" + radius;
        } else {
            url += "&maxDistanceLimit=" + 9999;
        }
        url += "&ratingKey=" + minRating + "&ratingOperatorKey=GreaterThanOrEqual";
        url += "&pageNumber=1&pageSize=30";
        return url;
    }

    private void requestItems() {
        progressBar.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        Log.e(" url", getURL());
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, getURL(),null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        Log.e("raw result", response.toString());
                        try {
                            populateList(response);

                            progressBar.setVisibility(View.GONE);
                            if (establishments.isEmpty()) {
                                noItems.setVisibility(View.VISIBLE);
                                listView.setVisibility(View.GONE);
                            } else {
                                noItems.setVisibility(View.GONE);
                                listView.setVisibility(View.VISIBLE);
                            }
                            Log.e(" result", response.toString());
                        } catch (Exception e) {
                            Log.e(" error","ERROR");
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                }
        ){
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("x-api-version", "2");
                return headers;
            }
        };
        getRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(getRequest);
    }

    private void populateList(JSONObject response) {
        establishments.clear();
        favourites = sharedPref.getStringSet("favourites", new HashSet<String>());
        Log.e("SearchFavs", favourites.toString());

        try{
            JSONArray items = response.getJSONArray("establishments");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                int id = item.getInt("FHRSID");
                String name = item.getString("BusinessName");
                String rating = item.getString("RatingValue");
                Establishment e = new Establishment(id, name, rating, favourites.contains(String.valueOf(id)));
                establishments.add(e);
            }
        }
        catch(JSONException err){
            Log.e("error", "Could not load items!");
        }
        establishmentsAdapter.notifyDataSetChanged();
    }

    private void requestSpinnerRecords(RequestType requestType, final String url) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        final RequestType type = requestType;
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try {
                            Log.e("requesturl", url);
                            Log.e("request", response.toString());
                            switch(type) {
                                case BUSINESS:
                                    populateBusinessType(response);
                                    break;
                                case REGION:
                                    populateRegion(response);
                                    break;
                                case AUTHORITY:
                                    populateAuthority(response);
                                    break;
                            }
                        } catch (Exception e) {
                            Log.e(" responseerror",e.toString());
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("requesterror", error.toString());
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("x-api-version", "2");
                return headers;
            }
        };
        requestQueue.add(getRequest);
    }

    private void populateBusinessType(JSONObject response) {
        businessTypes.clear();
        try {
            JSONArray array = response.getJSONArray("businessTypes");
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                int id = object.getInt("BusinessTypeId");
                String name = object.getString("BusinessTypeName");
                businessTypes.add(new SpinnerRecord(id, name));
            }
        } catch (JSONException e) {
            Log.e("error", e.toString());
        }
        businessTypesAdapter.notifyDataSetChanged();
    }

    private void populateRegion(JSONObject response) {
        regions.clear();
        regions.add(new SpinnerRecord(-1, getResources().getString(R.string.option_any)));
        try {
            JSONArray array = response.getJSONArray("regions");
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                int id = object.getInt("id");
                String name = object.getString("name");
                regions.add(new SpinnerRecord(id, name));
            }
        } catch (JSONException e) {
            Log.e("error", e.toString());
        }
        regionsAdapter.notifyDataSetChanged();
    }

    private void populateAuthority(JSONObject response) {
        authorities.clear();
        if (region.getId().equals("-1")) {
            authorities.add(new SpinnerRecord(-1, getResources().getString(R.string.option_any), ""));
        }
        try {
            JSONArray array = response.getJSONArray("authorities");
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                int id = object.getInt("LocalAuthorityId");
                String name = object.getString("Name");
                String regionName = object.getString("RegionName");
                if (region.getId().equals("-1") || region.getName().equals(regionName)) {
                    authorities.add(new SpinnerRecord(id, name, regionName));
                }
            }
        } catch (JSONException e) {
            Log.e("error", e.toString());
        }
        authoritiesAdapter.notifyDataSetChanged();
        authoritySpinner.setSelection(0);
        authority = authorities.get(0);
        requestItems();
    }

    // Result intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                int index = data.getIntExtra("index", -1);
                int favChange = data.getIntExtra("favChange", -1);
                Establishment e = establishments.get(index);
                if ((favChange == 0 && e.isFavourite()) || (favChange == 1 && !e.isFavourite())) {
                    e.toggleFavourite();
                    establishmentsAdapter.notifyDataSetChanged();
                }
            }
        }
    }
}
