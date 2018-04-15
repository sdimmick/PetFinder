package com.superconnected.petfinder;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.util.Log;

import com.superconnected.petfinder.models.PetImage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ImageClassifierService extends IntentService {
    private static final String TAG = "ImageClassifierService";
    private static final String ACTION_CLASSIFY_IMAGES = "com.superconnected.petfinder.action.CLASSIFY_IMAGES";

    // TensorFlow constants
    private static final String MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
    private static final String LABEL_FILE = "file:///android_asset/imagenet_comp_graph_label_strings.txt";
    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";

    private Classifier mClassifier;
    private List<String> mDogs;
    private List<String> mCats;

    public ImageClassifierService() {
        super("ImageClassifierService");
    }

    public static void startClassifer(Context context) {
        Intent intent = new Intent(context, ImageClassifierService.class);
        intent.setAction(ACTION_CLASSIFY_IMAGES);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mClassifier = TensorFlowImageClassifier.create(
                getAssets(),
                MODEL_FILE,
                LABEL_FILE,
                INPUT_SIZE,
                IMAGE_MEAN,
                IMAGE_STD,
                INPUT_NAME,
                OUTPUT_NAME
        );

        mDogs = getDogWordnetIDs();
        mCats = getCatWordnetIDs();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CLASSIFY_IMAGES.equals(action)) {
                classifyImages();
            }
        }
    }

    private void classifyImage(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if (bitmap == null) {
            return;
        }

        // Scale gallery image to dimensions TensorFlow is expecting
        Bitmap scaledBitmap = bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
        List<Classifier.Recognition> recognitions = mClassifier.recognizeImage(scaledBitmap);

        if (recognitions != null && !recognitions.isEmpty()) {
            for (Classifier.Recognition r : recognitions) {
                String title = r.getTitle();
                Log.d(TAG, "Recognized image: " + title);
                String normalizedTitle = normalizeWordNetLabel(title);

                if (isDog(normalizedTitle) || isCat(normalizedTitle)) {
                    Log.d(TAG, "Found a dog or cat!");
                    addPetImage(r, path);
                }
            }
        }
    }

    private void classifyImages() {
        Log.d(TAG, "Starting image classification");

        Cursor androidImagesCursor = null;

        try {
            androidImagesCursor = getImagesCursor();
            int imagePathIndex = androidImagesCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            while (androidImagesCursor.moveToNext()) {
                Log.d(TAG, String.format("Classifying image %d of %d", androidImagesCursor.getPosition() + 1, androidImagesCursor.getCount()));
                String path = androidImagesCursor.getString(imagePathIndex);
                classifyImage(path);
            }
        } finally {
            if (androidImagesCursor != null) {
                androidImagesCursor.close();
            }
        }

        Log.d(TAG, "Finished image classification");
    }

    private Cursor getImagesCursor() {
        return getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.ImageColumns.DATA},
                null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");
    }

    private List<String> parseWordnetIdFile(String filename) {
        BufferedReader reader = null;
        List<String> wnids = new ArrayList<>();
        try {
            reader = new BufferedReader(new InputStreamReader(getAssets().open(filename)));
            String line;
            while ((line = reader.readLine()) != null) {
                wnids.add(normalizeWordNetLabel(line));
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading WNID file: " + filename, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignored
                }
            }
        }
        return wnids;
    }

    private String normalizeWordNetLabel(String label) {
        return label.toLowerCase().replaceAll(" ", "_");
    }

    private List<String> getDogWordnetIDs() {
        return parseWordnetIdFile("dogs.txt");
    }

    private List<String> getCatWordnetIDs() {
        return parseWordnetIdFile("cats.txt");
    }

    private boolean isDog(String title) {
        return mDogs.contains(title);
    }

    private boolean isCat(String title) {
        return mCats.contains(title);
    }

    private void addPetImage(Classifier.Recognition recognition, String path) {
        final PetFinderDatabase db = PetFinderApplication.getDatabase();
        final PetImage newPet = new PetImage()
                .setPath(path)
                .setClassification(recognition.getTitle())
                .setClassificationId(recognition.getId())
                .setClassificationScore((double) recognition.getConfidence());
        db.persist(newPet);
    }
}
