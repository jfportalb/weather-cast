package br.com.ejcm.weathercast;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        String details = intent.getStringExtra(ListForecastFragment.DETAIL_STRING);
        View rootView =  inflater.inflate(R.layout.fragment_detail, container, false);
        TextView detailsText = (TextView) rootView.findViewById(R.id.details_text_view);
        detailsText.setText(details);
        return rootView;
    }
}
