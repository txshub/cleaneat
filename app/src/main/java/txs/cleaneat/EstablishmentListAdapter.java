package txs.cleaneat;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class EstablishmentListAdapter extends ArrayAdapter<Establishment> {

    private Context context;
    private List<Establishment> establishments;

    //constructor, call on creation
    public EstablishmentListAdapter(Context context, int resource, ArrayList<Establishment> objects) {
        super(context, resource, objects);

        this.context = context;
        this.establishments = objects;
    }

    //called when rendering the list
    public View getView(int position, View convertView, ViewGroup parent) {

        Establishment establishment = establishments.get(position);

        //get the inflater and inflate the XML layout for each item
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.establishment_list_layout, null);

        TextView description = (TextView) view.findViewById(R.id.name);
        TextView rating = (TextView) view.findViewById(R.id.rating);

        description.setText(establishment.getName());
        rating.setText("Rating: " + establishment.getRating());

        return view;
    }
}