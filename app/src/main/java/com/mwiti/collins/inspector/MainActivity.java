package com.mwiti.collins.inspector;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import clarifai2.api.ClarifaiResponse;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.image.ClarifaiImage;
import clarifai2.dto.model.ConceptModel;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by collins on 8/19/17.
 */
//class main activity inheriting from abstract class wrapper activity
public class MainActivity extends WrapperActivity {
    public static final int PICK_IMAGE = 100;

    // the list of results that were returned from the API
    @BindView(R.id.resultsList) RecyclerView resultsList;

    // the view where the image the user selected is displayed
    @BindView(R.id.image) ImageView imageView;

    // switches between the text prompting the user to hit the FAB, and the loading spinner
    @BindView(R.id.switcher) ViewSwitcher switcher;

    // the FAB that the user clicks to select an image
    @BindView(R.id.fab) View fab;

     //on click for fab icon in order to select image when clicked
    @OnClick(R.id.fab)
    void pickImage() {
        startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"), PICK_IMAGE);
    }

    // check the retrieved selected image if is corresponding to the given result. If true break
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch(requestCode) {
            case PICK_IMAGE:
                final byte[] imageBytes = InspectorUtil.retrieveSelectedImage(this, data);
                if (imageBytes != null) {
                    onImagePicked(imageBytes);
                }
                break;
        }
    }

    // instantiation of Recognize adapter
    @NonNull
    private final RecognizeAdapter adapter = new RecognizeAdapter();

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    //
    @Override protected void onStart() {
        super.onStart();

        resultsList.setLayoutManager(new LinearLayoutManager(this));
        resultsList.setAdapter(adapter);
    }


    @Override protected int layoutRes() { return R.layout.activity_main; }

    private void setBusy(final boolean busy) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                switcher.setDisplayedChild(busy ? 1 : 0);
                imageView.setVisibility(busy ? GONE : VISIBLE);
                fab.setEnabled(!busy);
            }
        });
    }

    private void onImagePicked(@NonNull final byte[] imageBytes) {
        // Now we will upload our image to the Clarifai API
        setBusy(true);

        // Make sure we don't show a list of old concepts while the image is being uploaded
        adapter.setData(Collections.<Concept>emptyList());

        new AsyncTask<Void, Void, ClarifaiResponse<List<ClarifaiOutput<Concept>>>>() {
            @Override protected ClarifaiResponse<List<ClarifaiOutput<Concept>>> doInBackground(Void... params) {
                // The default Clarifai model that identifies concepts in images
                final ConceptModel generalModel = App.get().clarifaiClient().getDefaultModels().generalModel();

                // Use this model to predict, with the image that the user just selected as the input
                return generalModel.predict()
                        .withInputs(ClarifaiInput.forImage(ClarifaiImage.of(imageBytes)))
                        .executeSync();
            }
    // is to notify to the user if the response has failed or is successful
            @Override protected void onPostExecute(ClarifaiResponse<List<ClarifaiOutput<Concept>>> response) {
                setBusy(false);
                if (!response.isSuccessful()) {
                    showErrorSnackbar(R.string.error_while_contacting_api);
                    return;
                }
                final List<ClarifaiOutput<Concept>> predictions = response.get();
                if (predictions.isEmpty()) {
                    showErrorSnackbar(R.string.no_results_from_api);
                    return;
                }
                adapter.setData(predictions.get(0).data());
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
            }
    // show error inform of snackbar
            private void showErrorSnackbar(@StringRes int errorString) {
                Snackbar.make(
                        root,
                        errorString,
                        Snackbar.LENGTH_INDEFINITE
                ).show();
            }
        }.execute();
    }
    //make logout appear in menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    //enable user to logout the app
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //when the user has logged out, he is been returned to the login page
    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
