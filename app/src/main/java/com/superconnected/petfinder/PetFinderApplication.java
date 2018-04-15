package com.superconnected.petfinder;

import android.app.Application;

public class PetFinderApplication extends Application {
    private static PetFinderApplication sApplication;
    private static PetFinderDatabase sDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        sDatabase = new PetFinderDatabase();
    }

    public static PetFinderApplication getInstance() {
        return sApplication;
    }

    public static PetFinderDatabase getDatabase() {
        return sDatabase;
    }
}
