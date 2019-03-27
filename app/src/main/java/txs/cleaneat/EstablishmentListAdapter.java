package txs.cleaneat;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class EstablishmentListAdapter extends ArrayAdapter<Establishment> {

    private Context context;
    private List<Establishment> establishments;

    public EstablishmentListAdapter(Context context, int resource, ArrayList<Establishment> objects) {
        super(context, resource, objects);

        this.context = context;
        this.establishments = objects;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        Establishment establishment = establishments.get(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.establishment_list_layout, null);

        TextView description = view.findViewById(R.id.name);
        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        ImageView favourite = view.findViewById(R.id.listFavourite);

        description.setText(establishment.getName());
        ratingBar.setRating(Integer.parseInt(establishment.getRating()));
        if (establishment.isFavourite()) {
            favourite.setVisibility(View.VISIBLE);
        } else {
            favourite.setVisibility(View.GONE);
        }

        return view;
    }
}