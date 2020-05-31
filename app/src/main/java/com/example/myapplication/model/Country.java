package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.Comparator;
import java.util.Objects;

public class Country {
    @SerializedName("Country")
    private String mName;
    @SerializedName("TotalConfirmed")
    private long mTotalCases;
    @SerializedName("TotalRecovered")
    private long mTotalRecovered;
    @SerializedName("TotalDeaths")
    private long mTotalDeaths;

    public Country() {
    }

    public Country(String mName, long mTotalCases, long mTotalRecovered, long mTotalDeaths) {
        this.mName = mName;
        this.mTotalCases = mTotalCases;
        this.mTotalRecovered = mTotalRecovered;
        this.mTotalDeaths = mTotalDeaths;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public long getmTotalCases() {
        return mTotalCases;
    }

    public void setmTotalCases(long mTotalCases) {
        this.mTotalCases = mTotalCases;
    }

    public long getmTotalRecovered() {
        return mTotalRecovered;
    }

    public void setmTotalRecovered(long mTotalRecovered) {
        this.mTotalRecovered = mTotalRecovered;
    }

    public long getmTotalDeaths() {
        return mTotalDeaths;
    }

    public void setmTotalDeaths(long mTotalDeaths) {
        this.mTotalDeaths = mTotalDeaths;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Country)) return false;
        Country country = (Country) o;
        return getmName().equals(country.getmName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getmName());
    }
    public static Comparator<Country> decendingOrder = new Comparator<Country>() {

        @Override
        public int compare(Country c1, Country c2) {

            long totalCase1 =  c1.getmTotalCases();
            long totalCase2 = c2.getmTotalCases();
            return (int) (totalCase2-totalCase1);
        }
    };

    public static Comparator<Country> ascendingOrder = new Comparator<Country>() {

        @Override
        public int compare(Country c1, Country c2) {

            long totalCase1 =  c1.getmTotalCases();
            long totalCase2 = c2.getmTotalCases();
            return (int) (totalCase1-totalCase2);
        }
    };
}
