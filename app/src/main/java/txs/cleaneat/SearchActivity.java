package txs.cleaneat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Spinner;

import com.android.volley.AuthFailureError;
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
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    private enum RequestType {ESTABLISHMENT, BUSINESS, REGION, AUTHORITY};

    public final String RELEVANCE = "relevance";
    public final String RATING_DESC = "rating";
    public final String RATING_ASC = "desc_rating";
    public final String DISTANCE = "distance";
    public final String ALPHA_ASC = "alpha";
    public final String ALPHA_DESC = "desc_alpha";

    private ListView listView;
    private ProgressBar progressBar;
    private SearchView search;
    private LinearLayout sortPanel;
    private LinearLayout filterPanel;
    private Button sortButton;
    private Button filterButton;

    private ArrayList<Establishment> establishments = new ArrayList<Establishment>();
    private EstablishmentListAdapter establishmentsAdapter;

    private ArrayList<SpinnerRecord> businessTypes = new ArrayList<SpinnerRecord>();
    private ArrayAdapter<SpinnerRecord> businessTypesAdapter;

    private ArrayList<SpinnerRecord> regions = new ArrayList<SpinnerRecord>();
    private ArrayAdapter<SpinnerRecord> regionsAdapter;

    private ArrayList<SpinnerRecord> authorities = new ArrayList<SpinnerRecord>();
    private ArrayAdapter<SpinnerRecord> authoritiesAdapter;

    private String sortType = "relevance";
    private String searchTerm = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        listView = (ListView) findViewById(R.id.establishmentList);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        sortPanel = (LinearLayout) findViewById(R.id.sortPanel);
        filterPanel = (LinearLayout) findViewById(R.id.filterPanel);

        sortButton = (Button) findViewById(R.id.sortButton);
        filterButton = (Button) findViewById(R.id.filterButton);

        RadioGroup sortGroup = (RadioGroup) findViewById(R.id.sortGroup);

        establishmentsAdapter = new EstablishmentListAdapter(this, 0, establishments);
        listView.setAdapter(establishmentsAdapter);

        Spinner businessTypeSpinner = (Spinner) findViewById(R.id.spinnerBusiness);
        businessTypesAdapter = new ArrayAdapter<SpinnerRecord>(this,
                android.R.layout.simple_spinner_item, businessTypes);
        businessTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        businessTypeSpinner.setAdapter(businessTypesAdapter);
        Spinner regionSpinner = (Spinner) findViewById(R.id.spinnerRegion);
        regionsAdapter = new ArrayAdapter<SpinnerRecord>(this,
                android.R.layout.simple_spinner_item, regions);
        regionSpinner.setAdapter(regionsAdapter);
        regionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner authoritySpinner = (Spinner) findViewById(R.id.spinnerAuthority);
        authoritiesAdapter = new ArrayAdapter<SpinnerRecord>(this,
                android.R.layout.simple_spinner_item, authorities);
        authoritySpinner.setAdapter(authoritiesAdapter);
        authoritiesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        search = (SearchView) findViewById(R.id.searchBox);
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

        sortType = RELEVANCE;
        requestItems();

        requestSpinnerRecords(RequestType.BUSINESS,
                "http://api.ratings.food.gov.uk/BusinessTypes/basic");
        requestSpinnerRecords(RequestType.REGION,
                "http://api.ratings.food.gov.uk/Regions/basic");
        requestSpinnerRecords(RequestType.AUTHORITY,
                "http://api.ratings.food.gov.uk/Authorities/basic");
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
        String base = "http://api.ratings.food.gov.uk/Establishments?";
        String name = "name=";
        name += searchTerm;
        String sort = "&sortOptionKey=" + sortType;
        String url = base + name + sort;
        url += "&pageNumber=1&pageSize=30";
        return url;
    }

    private void requestItems() {
        progressBar.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, getURL(),null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try {
                            populateList(response);

                            progressBar.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);

                            Log.e(" url", getURL().toString());
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
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                //headers.put("Content-Type", "application/json");
                headers.put("x-api-version", "2");
                return headers;
            }
        };
        requestQueue.add(getRequest);
    }

    private void populateList(JSONObject response) {
        establishments.clear();
        try{
            JSONArray items = response.getJSONArray("establishments");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                String name = item.getString("BusinessName");
                String rating = item.getString("RatingValue");
                Establishment e = new Establishment(name, rating);
                establishments.add(e);
            }
        }
        catch(JSONException err){
            Log.e("error", "Could not load items!");
        }
        establishmentsAdapter.notifyDataSetChanged();
    }

    private void requestSpinnerRecords(RequestType requestType, String url) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        final RequestType type = requestType;
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try {
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
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
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
        regions.add(new SpinnerRecord(-1, "Any"));
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
        authorities.add(new SpinnerRecord("", "Any"));
        try {
            JSONArray array = response.getJSONArray("authorities");
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                String code = object.getString("LocalAuthorityIdCode");
                String name = object.getString("Name");
                authorities.add(new SpinnerRecord(code, name));
            }
        } catch (JSONException e) {
            Log.e("error", e.toString());
        }
        authoritiesAdapter.notifyDataSetChanged();
    }
}
