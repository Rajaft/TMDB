package com.rajat.tmdbsearch;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bumptech.glide.Glide;
import com.rajat.tmdbsearch.MovieResult.Builder;

public class MoviesFragment extends ListFragment {
    OnMovieSelectedListener     mCallback;
    ListFragment                mThisFragment;
    MoviesAdapter               mMoviesAdapter;
    TMDBQueryManager            mTMDBtask;

    // The container Activity must implement this interface so the frag can deliver messages
    public interface OnMovieSelectedListener {
        /** Called by MoviesFragment when a list item is selected */
        public void onMovieSelected(MovieResult movie);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Store the context for later use
        mThisFragment = this;

        // Create ArrayAdapter using the planet list.
//        mMoviesAdapter = new ArrayAdapter<MovieResult>(getActivity().getApplicationContext(), R.layout.movie_result_list_item);
        mMoviesAdapter = new MoviesAdapter(getActivity().getApplicationContext());

        // Create an array adapter for the list view, using the Ipsum headlines array
        setListAdapter(mMoviesAdapter);

        // Get the intent to get the query.
        Intent intent = getActivity().getIntent();
        String query = intent.getStringExtra(MainActivity.EXTRA_QUERY);

        // Check if the NetworkConnection is active and connected.
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            mTMDBtask = (TMDBQueryManager)new TMDBQueryManager().execute(query);

        } else {
            Builder newMovie = MovieResult.newBuilder(0, "No network connection");
            mMoviesAdapter.add(newMovie.build());
        }
    }

    /**
     * Updates the View with the results. This is called asynchronously
     * when the results are ready.
     * @param result The results to be presented to the user.
     */
    public void updateViewWithResults(ArrayList<MovieResult> result) {
        Log.d("updateViewWithResults", result.toString());

        for (MovieResult aMovie : result) {
            mMoviesAdapter.add(aMovie);
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        // When in two-pane layout, set the listview to highlight the selected list item
        // (We do this during onStart because at the point the listview is available.)
        if (getFragmentManager().findFragmentById(R.id.movies_fragment) != null) {
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (OnMovieSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMovieSelectedListener");
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        MovieResult movie = (MovieResult)l.getAdapter().getItem(position);

        // Get the intent to get the query.
        Intent intent = getActivity().getIntent();
        String query = intent.getStringExtra(MainActivity.EXTRA_QUERY);

        // Check if the NetworkConnection is active and connected.
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new TMDBQueryMovieDetails().execute(movie.getId());
        }

        // Set the item as checked to be highlighted when in two-pane layout
        getListView().setItemChecked(position, true);
    }

    public class MoviesAdapter extends ArrayAdapter<MovieResult> {
        private final LayoutInflater mInflater;
        public MoviesAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_2);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        public void setData(List<MovieResult> movies){
            clear();
            if (movies!=null) {
                for (MovieResult movie : movies){
                    add(movie);
                }
            }
        }
        @Override
        public View getView (int position, View recycled, ViewGroup container){
            final ImageView myImageView;
            if (recycled == null) {
                myImageView = (ImageView) mInflater.inflate(R.layout.movie_item, container, false);
            } else {
                myImageView = (ImageView) recycled;
            }

            MovieResult movie = getItem(position);
            String url = "https://image.tmdb.org/t/p/original" + movie.getPosterPath();

            Glide.with(mThisFragment)
                    .load(url)
                    //.centerCrop()
                            //.placeholder(R.drawable.loading_spinner)
                    .crossFade()
                    .into(myImageView);

            return myImageView;
        }
    }


    private class TMDBQueryManager extends AsyncTask {

        private final String TMDB_API_KEY = "933cb76e91787f912945526d155bfa8e";
        private static final String DEBUG_TAG = "TMDBQueryManager";

        @Override
        protected ArrayList<MovieResult> doInBackground(Object... params) {
            try {
                return searchIMDB((String) params[0]);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Object result) {
            updateViewWithResults((ArrayList<MovieResult>) result);
        };

        /**
         * Searches IMDBs API for the given query
         * @param query The query to search.
         * @return A list of all hits.
         */
        public ArrayList<MovieResult> searchIMDB(String query) throws IOException {
            // Build URL
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("http://api.themoviedb.org/3/search/movie");
            stringBuilder.append("?api_key=" + TMDB_API_KEY);
            stringBuilder.append("&query=" + query);
            URL url = new URL(stringBuilder.toString());

            InputStream stream = null;
            try {
                // Establish a connection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.addRequestProperty("Accept", "application/json"); // Required to get TMDB to play nicely.
                conn.setDoInput(true);
                conn.connect();

                int responseCode = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response code is: " + responseCode + " " + conn.getResponseMessage());

                stream = conn.getInputStream();
                return parseResult(stringify(stream));
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        }

        private ArrayList<MovieResult> parseResult(String result) {
            String streamAsString = result;
            ArrayList<MovieResult> results = new ArrayList<MovieResult>();
            try {
                JSONObject jsonObject = new JSONObject(streamAsString);
                JSONArray array = (JSONArray) jsonObject.get("results");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonMovieObject = array.getJSONObject(i);
                    Builder movieBuilder = MovieResult.newBuilder(
                            Integer.parseInt(jsonMovieObject.getString("id")),
                            jsonMovieObject.getString("title"))
                            .setBackdropPath(jsonMovieObject.getString("backdrop_path"))
                            .setOriginalTitle(jsonMovieObject.getString("original_title"))
                            .setPopularity(jsonMovieObject.getString("popularity"))
                            .setPosterPath(jsonMovieObject.getString("poster_path"))
                            .setReleaseDate(jsonMovieObject.getString("release_date"));
                    results.add(movieBuilder.build());
                }
            } catch (JSONException e) {
                System.err.println(e);
                Log.d(DEBUG_TAG, "Error parsing JSON. String was: " + streamAsString);
            }
            return results;
        }

        public String stringify(InputStream stream) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(reader);
            return bufferedReader.readLine();
        }
    }

    //
    // Movie details is needed for now just to supply to runtime of the movie
    // that otherwise is not present is the brief description
    private class TMDBQueryMovieDetails extends AsyncTask {

        private final String TMDB_API_KEY = "933cb76e91787f912945526d155bfa8e";
        private static final String DEBUG_TAG = "TMDBQueryMovieDetails";

        @Override
        protected MovieResult doInBackground(Object... params) {
            try {
                return queryIMDBDetails((Integer) params[0]);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Object result) {
            // Notify the parent activity of selected item
            mCallback.onMovieSelected((MovieResult) result);
        };

        /**
         * Searches IMDBs API for the given query
         * @param query The query to search.
         * @return A list of all hits.
         */
        public MovieResult queryIMDBDetails(Integer query) throws IOException {
            // Build URL
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("http://api.themoviedb.org/3/movie/");
            stringBuilder.append(query);
            stringBuilder.append("?api_key=" + TMDB_API_KEY);
            URL url = new URL(stringBuilder.toString());

            InputStream stream = null;
            try {
                // Establish a connection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.addRequestProperty("Accept", "application/json"); // Required to get TMDB to play nicely.
                conn.setDoInput(true);
                conn.connect();

                int responseCode = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response code is: " + responseCode + " " + conn.getResponseMessage());

                stream = conn.getInputStream();
                return parseResult(stringify(stream));
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        }

        private MovieResult parseResult(String result) {
            String streamAsString = result;
            MovieResult movie = null;
            try {
                JSONObject jsonObject = new JSONObject(streamAsString);

                MovieResult.Builder movieBuilder = MovieResult.newBuilder(
                        Integer.parseInt(jsonObject.getString("id")),
                        jsonObject.getString("title"))
                        .setBackdropPath(jsonObject.getString("backdrop_path"))
                        .setOriginalTitle(jsonObject.getString("original_title"))
                        .setPopularity(jsonObject.getString("popularity"))
                        .setPosterPath(jsonObject.getString("poster_path"))
                        .setReleaseDate(jsonObject.getString("release_date"))
                        .setRuntime(Integer.parseInt(jsonObject.getString("runtime")))
                        .setOverview(jsonObject.getString("overview"))
                        .setVoteAverage(jsonObject.getString("vote_average"));

                movie = movieBuilder.build();

            } catch (JSONException e) {
                System.err.println(e);
                Log.d(DEBUG_TAG, "Error parsing JSON. String was: " + streamAsString);
            }
            return movie;
        }

        public String stringify(InputStream stream) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(reader);
            return bufferedReader.readLine();
        }
    }

}
