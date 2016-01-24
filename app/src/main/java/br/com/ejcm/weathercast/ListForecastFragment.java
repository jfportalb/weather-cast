package br.com.ejcm.weathercast;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;

/**
 * A placeholder fragment containing a simple view.
 */
public class ListForecastFragment extends Fragment {

    private static final String LOG_TAG = ListForecastFragment.class.getName();
    public static final String DETAIL_STRING = "DetailActivity";
    private ArrayAdapter<String> mForecastAdapter;

    public ListForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mForecastAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.forecast_list_item,
                R.id.list_item_text_view);

        View rootView = inflater.inflate(R.layout.fragment_list_forecast, container, false);
        ListView list = (ListView) rootView.findViewById(R.id.list_forecast);
        list.setAdapter(mForecastAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                TextView tv = (TextView) view;
                intent.putExtra(DETAIL_STRING, tv.getText());
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FetchWeatherTask fwt = new FetchWeatherTask();
        fwt.execute();
    }

    private class FetchWeatherTask extends AsyncTask<Void, Void, String[]> {

        private String formatDate(long dateInMillis) {
            DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
            return df.format(new Date(dateInMillis));
        }

        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays) throws JSONException {

            // Location information
            final String OWM_CITY = "city";
            final String OWM_CITY_NAME = "name";
            final String OWM_COORD = "coord";

            // Location coordinate
            final String OWM_LATITUDE = "lat";
            final String OWM_LONGITUDE = "lon";

            // Weather information.  Each day's forecast info is an element of the "list" array.
            final String OWM_LIST = "list";

            final String OWM_DATE = "dt";

            final String OWM_PRESSURE = "pressure";
            final String OWM_HUMIDITY = "humidity";
            final String OWM_WINDSPEED = "speed";
            final String OWM_WIND_DIRECTION = "deg";

            // All temperatures are children of the "temp" object.
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";

            final String OWM_WEATHER = "weather";
            final String OWM_DESCRIPTION = "main";
            final String OWM_WEATHER_ID = "id";

            String[] result = new String[numDays];

            JSONObject forecast = new JSONObject(forecastJsonStr);
            JSONArray list = forecast.getJSONArray(OWM_LIST);
            for (int i = 0; i < numDays; i++) {
                // Gets the weather data for that day.
                JSONObject dayForecast = list.getJSONObject(i);

                //Gets the date and formats it.
                long dateInMillis = dayForecast.getLong(OWM_DATE)*1000;
                String date = formatDate(dateInMillis);

                // Gets the weather description.
                String desc = dayForecast.getJSONArray(OWM_WEATHER)
                        .getJSONObject(0).getString(OWM_DESCRIPTION);

                //Retrieves the temperature information.
                JSONObject temp = dayForecast.getJSONObject(OWM_TEMPERATURE);
                String min = temp.getString(OWM_MIN);
                String max = temp.getString(OWM_MAX);

                StringBuilder sb = new StringBuilder();
                sb
                        .append(date)
                        .append(" - ")
                        .append(desc)
                        .append(" - ")
                        .append(min)
                        .append("/")
                        .append(max);

                result[i] = sb.toString();
            }
            return result;
        }

        @Override
        protected String[] doInBackground(Void... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            String location = "Rio de Janeiro";
            String format = "json";
            String units = "metric";
            String lang = "fr";
            int numDays = 14;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String FORECAST_BASE_URL =
                        "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String LANGUAGE_PARAM = "lang";
                final String APPID_PARAM = "appid";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, location)
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(LANGUAGE_PARAM, lang)
                        .appendQueryParameter(APPID_PARAM, "232763e5b836e83388caca0749577747")
                        .build();

                URL url = new URL(builtUri.toString());
//                Log.v(LOG_TAG, url.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder builder = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // builder for debugging.
                    builder.append(line).append("\n");
                }

                if (builder.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = builder.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error " + e, e);

                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getWeatherDataFromJson(forecastJsonStr, numDays);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) { //To avoid NullPointerException when trying to iterate
                mForecastAdapter.clear();
                for (String forecast : result)
                    mForecastAdapter.add(forecast);
            }
        }
    }

}
