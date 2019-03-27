package txs.cleaneat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Activity for displaying favourite establishments
 */
public class FavouritesActivity extends AppCompatActivity {

    private ListView listView;
    private ProgressBar progressBar;
    private TextView noItems;

    private ArrayList<Establishment> establishments;
    private EstablishmentListAdapter establishmentsAdapter;

    // Favourites
    private Set<String> favourites;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);

        // Get views
        noItems = findViewById(R.id.favouritesNoItems);
        listView = findViewById(R.id.favouriteList);
        progressBar = findViewById(R.id.favouriteProgressBar);

        // Set up ListView
        establishments = new ArrayList<>();
        establishmentsAdapter = new EstablishmentListAdapter(this, 0, establishments);
        listView.setAdapter(establishmentsAdapter);

        // Set up listview event listeners
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Establishment e = (Establishment) parent.getItemAtPosition(position);
                Intent intent = new Intent(FavouritesActivity.this, EstablishmentActivity.class);
                intent.putExtra("id", e.getId());
                intent.putExtra("name", e.getName());
                intent.putExtra("index", position);
                startActivityForResult(intent, 1);
            }
        });

        // Set up favourites
        sharedPref = getSharedPreferences("CleanEatFavourites", Context.MODE_PRIVATE);

        favourites = sharedPref.getStringSet("favourites", new HashSet<String>());
        Log.e("SearchFavs", favourites.toString());
        requestAllFavourites();
    }

    private void requestAllFavourites() {
        establishments.clear();
        if (favourites.size() > 0) {
            progressBar.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);

            for (String id: favourites) {
                requestEstablishment(id);
            }
        } else {
            noItems.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }
    }

    private void requestEstablishment(String id) {
        final String url = "http://api.ratings.food.gov.uk/Establishments/" + id;
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try {
                            addFavourite(response);

                            Log.e(" url", url);
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
        requestQueue.add(getRequest);
    }

    private void addFavourite(JSONObject response) {
        try {
            int id = response.getInt("FHRSID");
            String name = response.getString("BusinessName");
            String rating = response.getString("RatingValue");
            Establishment e = new Establishment(id, name, rating, true);
            establishments.add(e);
        } catch(JSONException err) {
            Log.e("error", "Could not load item!");
        }

        if (establishments.size() == favourites.size()) {
            progressBar.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            establishmentsAdapter.notifyDataSetChanged();
        }
    }

    // Result intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                int index = data.getIntExtra("index", -1);
                int favChange = data.getIntExtra("favChange", -1);
                if (favChange == 0) {
                    establishments.remove(index);
                    if (establishments.isEmpty()) {
                        noItems.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                    }
                    establishmentsAdapter.notifyDataSetChanged();
                }
            }
        }
    }

}
