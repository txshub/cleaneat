package txs.cleaneat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Activity displaying establishment details
 */
public class EstablishmentActivity extends AppCompatActivity {

    private LinearLayout layout;
    private ProgressBar progress;
    private RatingBar detailsRating;
    private TextView detailsRatingDate;
    private TextView detailsBusiness;
    private TextView detailsAuthority;
    private TextView detailsAddress1;
    private TextView detailsAddress2;
    private TextView detailsAddress3;
    private TextView detailsAddress4;
    private TextView detailsPostCode;

    private int id = -1;
    private int index = -1;
    private int favChange = -1;

    private Menu menu;

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_establishment);

        sharedPref = getSharedPreferences("CleanEatFavourites", Context.MODE_PRIVATE);

        String name = getIntent().getStringExtra("name");
        id = getIntent().getIntExtra("id", -1);
        index = getIntent().getIntExtra("index", -1);

        if (name.isEmpty() || name == null) {
            name = "No available name";
        }

        getSupportActionBar().setTitle(name);

        layout = findViewById(R.id.detailsLayout);
        progress = findViewById(R.id.detailsProgressBar);
        layout.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        detailsRating = findViewById(R.id.detailsRating);
        detailsRatingDate = findViewById(R.id.detailsRatingDate);
        detailsBusiness = findViewById(R.id.detailsBusiness);
        detailsAuthority = findViewById(R.id.detailsAuthority);
        detailsAddress1 = findViewById(R.id.detailsAddress1);
        detailsAddress2 = findViewById(R.id.detailsAddress2);
        detailsAddress3 = findViewById(R.id.detailsAddress3);
        detailsAddress4 = findViewById(R.id.detailsAddress4);
        detailsPostCode = findViewById(R.id.detailsPostCode);

        requestDetails(id);
    }

    private boolean isFavourite() {
        Set<String> favourites = sharedPref.getStringSet("favourites", null);
        final String ID = String.valueOf(id);
        return favourites != null && favourites.contains(ID);
    }

    private void setFavourite(boolean fav) {
        Set<String> favourites = sharedPref.getStringSet("favourites", null);
        SharedPreferences.Editor editor = sharedPref.edit();

        final String ID = String.valueOf(id);

        if (favourites == null) {
            favourites = new HashSet<>();
        }
        if (fav && !favourites.contains(ID)) {
            favourites.add(ID);
            favChange = 1;
        } else if (!fav && favourites.contains(ID)){
            favourites.remove(ID);
            favChange = 0;
        } else {
            return;
        }

        editor.clear();
        editor.putStringSet("favourites", favourites);
        editor.commit();
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
            String ratingDateString = response.getString("RatingDate");
            String address1 = response.getString("AddressLine1");
            String address2 = response.getString("AddressLine2");
            String address3 = response.getString("AddressLine3");
            String address4 = response.getString("AddressLine4");
            String postCode = response.getString("PostCode");
            String authority = response.getString("LocalAuthorityName");
            String business = response.getString("BusinessType");

            detailsRating.setRating(Integer.parseInt(ratingString));

            try {
                SimpleDateFormat inDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                SimpleDateFormat outDate = new SimpleDateFormat("dd-MM-yyy");
                String dateText = outDate.format(inDate.parse(ratingDateString));
                detailsRatingDate.setText(
                        getResources().getString(R.string.details_label_rating_date) + ": " + dateText);
            } catch (ParseException e) {
                Log.e("error", e.toString());
                detailsRatingDate.setVisibility(View.GONE);
            }

            if (business.trim().isEmpty()) {
                detailsBusiness.setText("No business type available");
            } else {
                detailsBusiness.setText(business);
            }

            int addressMissing = 0;
            if (address1.trim().isEmpty()) {
                detailsAddress1.setVisibility(View.GONE);
                addressMissing++;
            } else {
                detailsAddress1.setText(address1);
            }
            if (address2.trim().isEmpty()) {
                detailsAddress2.setVisibility(View.GONE);
                addressMissing++;
            } else {
                detailsAddress2.setText(address2);
            }
            if (address3.trim().isEmpty()) {
                detailsAddress3.setVisibility(View.GONE);
                addressMissing++;
            } else {
                detailsAddress3.setText(address3);
            }
            if (address4.trim().isEmpty()) {
                detailsAddress4.setVisibility(View.GONE);
                addressMissing++;
            } else {
                detailsAddress4.setText(address4);
            }
            if (postCode.trim().isEmpty()) {
                if (addressMissing == 4) {
                    detailsPostCode.setText("No address available");
                }
            } else {
                detailsPostCode.setText(postCode);
            }

            if (authority.trim().isEmpty()) {
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.favouritesButton);
        if (isFavourite()) {
            item.setIcon(getResources().getDrawable(R.drawable.ic_favorite_white_24dp));
        } else {
            item.setIcon(getResources().getDrawable(R.drawable.ic_favorite_border_white_24dp));
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.favouritesButton) {
            if (isFavourite()) {
                item.setIcon(getResources().getDrawable(R.drawable.ic_favorite_border_white_24dp));
                setFavourite(false);
                Toast.makeText(this, getResources().getString(R.string.favourites_toast_remove), Toast.LENGTH_SHORT).show();
            } else {
                item.setIcon(getResources().getDrawable(R.drawable.ic_favorite_white_24dp));
                setFavourite(true);
                Toast.makeText(this, getResources().getString(R.string.favourites_toast_add), Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("index", index);
        returnIntent.putExtra("favChange", favChange);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

}
