package com.rajat.tmdbsearch;

import android.os.Parcel;
import android.os.Parcelable;

public class MovieResult implements Parcelable {
    private final String backdropPath;
    private final String originalTitle;
    private final int id;
    private final String popularity;
    private final String posterPath;
    private final String releaseDate;
    private final String title;
    private final int runtime;
    private final String overview;
    private final String voteAverage;

    private MovieResult(Builder builder) {
        backdropPath = builder.backdropPath;
        originalTitle = builder.originalTitle;
        id = builder.id;
        popularity = builder.popularity;
        posterPath = builder.posterPath;
        releaseDate = builder.releaseDate;
        title = builder.title;
        runtime = builder.runtime;
        overview = builder.overview;
        voteAverage = builder.voteAverage;
    }
    
    
    public static class Builder {
        private String backdropPath;
        private String originalTitle;
        private int id;
        private String popularity;
        private String posterPath;
        private String releaseDate;
        private String title;
        private int runtime;
        private String overview;
        private String voteAverage;
        
        public Builder(int id, String title) {
            this.id = id;
            this.title = title;
        }

        public Builder setBackdropPath(String backdropPath) {
            this.backdropPath = backdropPath;
            return this;
        }

        public Builder setOriginalTitle(String originalTitle) {
            this.originalTitle = originalTitle;
            return this;
        }

        public Builder setId(int id) {
            this.id = id;
            return this;
        }

        public Builder setPopularity(String popularity) {
            this.popularity = popularity;
            return this;
        }

        public Builder setPosterPath(String posterPath) {
            this.posterPath = posterPath;
            return this;
        }

        public Builder setReleaseDate(String releaseDate) {
            this.releaseDate = releaseDate;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setRuntime(int runTime) {
            this.runtime = runTime;
            return this;
        }

        public Builder setOverview(String overview) {
            this.overview = overview;
            return this;
        }

        public Builder setVoteAverage(String voteAverage) {
            this.voteAverage = voteAverage;
            return this;
        }

        public MovieResult build() {
            return new MovieResult(this);
        }
        
    }
    
    public static Builder newBuilder(int id, String title) {
        return new Builder(id, title);
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public int getId() {
        return id;
    }

    public String getPopularity() {
        return popularity;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getTitle() {
        return title;
    }

    public int getRuntime() {
        return runtime;
    }

    public String getOverview() {
        return overview;
    }

    public String getVoteAverage() {
        return voteAverage;
    }

    @Override
    public String toString() {
        return getTitle();
    }

    // This is where you write the values you want to save to the `Parcel`.
    // The `Parcel` class has methods defined to help you save all of your values.
    // Note that there are only methods defined for simple values, lists, and other Parcelable objects.
    // You may need to make several classes Parcelable to send the data you want.
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(title);
        parcel.writeString(backdropPath);
        parcel.writeString(originalTitle);
        parcel.writeString(popularity);
        parcel.writeString(posterPath);
        parcel.writeString(releaseDate);
        parcel.writeInt(runtime);
        parcel.writeString(overview);
        parcel.writeString(voteAverage);
    }

    // After implementing the `Parcelable` interface, we need to create the
    // `Parcelable.Creator<MyParcelable> CREATOR` constant for our class;
    // Notice how it has our class specified as its type.
    public static final Parcelable.Creator<MovieResult> CREATOR
            = new Parcelable.Creator<MovieResult>() {

        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public MovieResult createFromParcel(Parcel parcel) {

            Builder movieBuilder = MovieResult.newBuilder(
                    parcel.readInt(),
                    parcel.readString())
                    .setBackdropPath(parcel.readString())
                    .setOriginalTitle(parcel.readString())
                    .setPopularity(parcel.readString())
                    .setPosterPath(parcel.readString())
                    .setReleaseDate(parcel.readString())
                    .setRuntime(parcel.readInt())
                    .setOverview(parcel.readString())
                    .setVoteAverage(parcel.readString());

            return movieBuilder.build();
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public MovieResult[] newArray(int size) {
            return new MovieResult[size];
        }
    };

    public int describeContents() {
        return 0;
    }
}
