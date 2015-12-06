package com.rajat.tmdbsearch;

import com.bumptech.glide.Glide;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class MovieFragment extends Fragment {

    private MovieResult mCurrentMovie = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // If activity recreated (such as from screen rotate), restore
        // the previous article selection set by onSaveInstanceState().
        // This is primarily necessary when in the two-pane layout.
        if (savedInstanceState != null) {
            mCurrentMovie = savedInstanceState.getParcelable(TMDBSearchResultActivity.PAR_KEY);
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.movie_detail, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // During startup, check if there are arguments passed to the fragment.
        // onStart is a good place to do this because the layout has already been
        // applied to the fragment at this point so we can safely call the method
        // below that sets the article text.
        Bundle args = getArguments();
        if (args != null) {
            // Set article based on argument passed in
            MovieResult movie = args.getParcelable(TMDBSearchResultActivity.PAR_KEY);
            updateMovieView(movie);
        } else if (mCurrentMovie != null) {
            // Set article based on saved instance state defined during onCreateView
            updateMovieView(mCurrentMovie);
        }
    }

    public void updateMovieView(MovieResult movie) {
        TextView movie_title = (TextView) getActivity().findViewById(R.id.tvTitle);
        ImageView imageView = (ImageView) getActivity().findViewById(R.id.imageView);
        TextView movie_releasedate = (TextView) getActivity().findViewById(R.id.tvReleaseDate);
        TextView movie_runtime = (TextView) getActivity().findViewById(R.id.tvRunTime);
        TextView movie_voteAverage = (TextView) getActivity().findViewById(R.id.tvVoteAverage);
        TextView movie_overview = (TextView) getActivity().findViewById(R.id.tvOverview);

        String url = "https://image.tmdb.org/t/p/original" + movie.getPosterPath();

        Glide.with(this).load(url).into(imageView);
        movie_title.setText(movie.getTitle());
        movie_releasedate.setText(movie.getReleaseDate());
        movie_runtime.setText(movie.getRuntime() + " min");
        movie_voteAverage.setText(movie.getVoteAverage() + "/10");
        movie_overview.setText(movie.getOverview());

        mCurrentMovie = movie;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current article selection in case we need to recreate the fragment
        outState.putParcelable(TMDBSearchResultActivity.PAR_KEY, mCurrentMovie);
    }
}
