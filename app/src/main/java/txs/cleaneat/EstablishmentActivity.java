package txs.cleaneat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EstablishmentActivity extends AppCompatActivity {

    private LinearLayout layout;
    private ProgressBar progress;
    private RatingBar detailsRating;
    private TextView detailsBusiness;
    private TextView detailsAuthority;
    private TextView detailsAddress1;
    private TextView detailsAddress2;
    private TextView detailsAddress3;
    private TextView detailsAddress4;
    private TextView detailsPostCode;

    private Menu menu;
    private int id = -1;
    private int rating = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_establishment);

        String name = getIntent().getStringExtra("name");
        id = getIntent().getIntExtra("id", -1);

        if (id == -1) {
            goBack();
        }

        if (name.isEmpty() || name == null) {
            name = "No available name";
        }
//        getActionBar().setTitle("Test Title");
        getSupportActionBar().setTitle(name);

        layout = findViewById(R.id.detailsLayout);
        progress = findViewById(R.id.detailsProgressBar);
        layout.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        detailsRating = findViewById(R.id.detailsRating);
        detailsBusiness = findViewById(R.id.detailsBusiness);
        detailsAuthority = findViewById(R.id.detailsAuthority);
        detailsAddress1 = findViewById(R.id.detailsAddress1);
        detailsAddress2 = findViewById(R.id.detailsAddress2);
        detailsAddress3 = findViewById(R.id.detailsAddress3);
        detailsAddress4 = findViewById(R.id.detailsAddress4);
        detailsPostCode = findViewById(R.id.detailsPostCode);

        requestDetails(id);

    }

    private void requestDetails(int id) {
        final String url = "http://api.ratings.food.gov.uk/Establishments/" + id;
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try {
                            populateDetails(response);
                            Log.e("requesturl", url);
                            Log.e("request", response.toString());
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

    private void populateDetails(JSONObject response) {
        try{
            String ratingString = response.getString("RatingValue");
            String address1 = response.getString("AddressLine1");
            String address2 = response.getString("AddressLine2");
            String address3 = response.getString("AddressLine3");
            String address4 = response.getString("AddressLine4");
            String postCode = response.getString("PostCode");
            String authority = response.getString("LocalAuthorityName");
            String business = response.getString("BusinessType");

            if (ratingString.isEmpty() || ratingString == null) {
                // TODO
            } else {
                rating = Integer.parseInt(ratingString);
                MenuItem item = menu.findItem(R.id.favouritesButton);
                if (rating > 2) {
                    item.setIcon(getResources().getDrawable(R.drawable.ic_favorite_white_24dp));
                } else {
                    item.setIcon(getResources().getDrawable(R.drawable.ic_favorite_border_white_24dp));
                }
                detailsRating.setRating(Integer.parseInt(ratingString));
            }

            if (business.isEmpty() || business == null) {
                detailsBusiness.setText("No business type available");
            } else {
                detailsBusiness.setText(business);
            }

            int addressMissing = 0;
            if (address1.isEmpty() || address1 == null) {
                detailsAddress1.setVisibility(View.GONE);
                addressMissing++;
            } else {
                detailsAddress1.setText(address1);
            }
            if (address2.isEmpty() || address2 == null) {
                detailsAddress2.setVisibility(View.GONE);
                addressMissing++;
            } else {
                detailsAddress2.setText(address2);
            }
            if (address3.isEmpty() || address3 == null) {
                detailsAddress3.setVisibility(View.GONE);
                addressMissing++;
            } else {
                detailsAddress3.setText(address3);
            }
            if (address4.isEmpty() || address4 == null) {
                detailsAddress4.setVisibility(View.GONE);
                addressMissing++;
            } else {
                detailsAddress4.setText(address4);
            }
            if (postCode.isEmpty() || postCode == null) {
                if (addressMissing == 4) {
                    detailsPostCode.setText("No address available");
                }
            } else {
                detailsPostCode.setText(postCode);
            }

            if (authority.isEmpty() || authority == null) {
                detailsAuthority.setText("No local authority available");
            } else {
                detailsAuthority.setText(authority);
            }

            layout.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
        }
        catch(JSONException err){
            Log.e("error", "Could not load details!");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.favourite_menu, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.favouritesButton) {
            item.setIcon(getResources().getDrawable(R.drawable.ic_favorite_white_24dp));
        }
        return super.onOptionsItemSelected(item);
    }

    private void goBack(){

    }
}
