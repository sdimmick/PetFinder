package com.superconnected.petfinder;

import android.Manifest;
import android.app.LoaderManager;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.superconnected.petfinder.models.PetImage;
import com.yahoo.squidb.android.SquidCursorLoader;
import com.yahoo.squidb.data.SquidCursor;
import com.yahoo.squidb.sql.Query;

public class PetListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<SquidCursor<PetImage>> {
    private static final String TAG = "PetListActivity";
    private static final int PERMISSION_CODE_READ_EXTERNAL_STORAGE = 0;

    private RecyclerView mRecyclerView;
    private PetListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_list);

        mRecyclerView = findViewById(R.id.pet_list_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new PetListAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        if (checkAndRequestExternalStoragePermission()) {
            startImageClassifier();
        }

        getLoaderManager().restartLoader(0, null, this);
    }

    private boolean checkAndRequestExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_CODE_READ_EXTERNAL_STORAGE);

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE_READ_EXTERNAL_STORAGE:
            default:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startImageClassifier();
                }
                break;
        }
    }

    private void startImageClassifier() {
        ImageClassifierService.startClassifer(this);
    }

    @Override
    public Loader<SquidCursor<PetImage>> onCreateLoader(int i, Bundle bundle) {
        Query query = Query.select(PetImage.PROPERTIES);
        SquidCursorLoader<PetImage> loader =
                new SquidCursorLoader<>(this, PetFinderApplication.getDatabase(), PetImage.class, query);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<SquidCursor<PetImage>> loader, SquidCursor<PetImage> petImageSquidCursor) {
        Log.d(TAG, "Load finished - count:" + petImageSquidCursor.getCount());
        mAdapter.swapCursor(petImageSquidCursor);
    }

    @Override
    public void onLoaderReset(Loader<SquidCursor<PetImage>> loader) {
        mAdapter.swapCursor(null);
    }
}
