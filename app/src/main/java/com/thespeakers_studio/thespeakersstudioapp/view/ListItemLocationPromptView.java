package com.thespeakers_studio.thespeakersstudioapp.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import java.util.ArrayList;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.activity.PresentationMainActivity;
import com.thespeakers_studio.thespeakersstudioapp.model.PresentationData;
import com.thespeakers_studio.thespeakersstudioapp.model.Prompt;
import com.thespeakers_studio.thespeakersstudioapp.model.PromptAnswer;

/**
 * Created by smcgi_000 on 6/8/2016.
 */
public class ListItemLocationPromptView extends ListItemPromptView implements PresentationMainActivity.LocationSelectedListener {

    private Place mPlace;

    public ListItemLocationPromptView(Context context, Prompt prompt) {
        super(context, prompt);
    }

    @Override
    void inflateView() {
        super.inflateView();
    }

    @Override
    protected void inflateEditContents(LayoutInflater inflater, LinearLayout editWrapper) {
        inflater.inflate(R.layout.presentation_prompt_card_location, editWrapper, true);
    }

    @Override
    void renderViews() {
        super.renderViews();

        /*
        String id = place.getId();

        GoogleApiClient api = ((PresentationMainActivity) getContext()).getGoogleApi();
        Places.GeoDataApi.getPlaceById(api, id)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
                            final Place myPlace = places.get(0);
                            Log.i("SS", "Place found! " + myPlace.getName());
                        } else {
                            Log.e("SS", "Place not found!");
                        }
                        places.release();
                    }
                });
               */

        /*
        mFragment = (PlaceAutocompleteFragment) ((AppCompatActivity) getContext()).getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        mFragment.setText(mPrompt.getProcessedAnswer(getContext()));
        mFragment.setOnPlaceSelectedListener(this);
        */

        setLocationButtonText();

        if (getContext() instanceof PresentationMainActivity) {
            ((PresentationMainActivity) getContext()).setOnLocationSelectedListener(this);
        }

        findViewById(R.id.prompt_location_button).setOnClickListener(this);

    }

    private void setLocationButtonText() {
        TextView button = (TextView) findViewById(R.id.prompt_location_button);
        String text;
        if (mPlace != null) {
            text = mPlace.getName().toString();
        } else if (!processAnswer().isEmpty()) {
            text = processAnswer();
        } else {
            text = getContext().getString(R.string.location_prompt);
        }
        button.setText(text);
    }

    @Override
    protected String processAnswer() {
        String name = mPrompt.getAnswerByKey("name").getValue();
        return name;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.prompt_location_button:
                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build((Activity) getContext());
                    ((Activity) getContext()).startActivityForResult(intent, PresentationData.LOCATION_INTENT_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    Log.e("SS", "Repairable exception");
                } catch (GooglePlayServicesNotAvailableException e) {
                    Log.e("SS", "Google Play services not available!");
                }
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    public ArrayList<PromptAnswer> getUserInput() {
        if (mPlace == null) {
            return mPrompt.getAnswer();
        } else {
            ArrayList<PromptAnswer> answers = new ArrayList<>();
            answers.add(new PromptAnswer("id", mPlace.getId(), mPrompt.getId()));
            answers.add(new PromptAnswer("name", mPlace.getName().toString(), mPrompt.getId()));
            answers.add(new PromptAnswer("address", mPlace.getAddress().toString(), mPrompt.getId()));
            answers.add(new PromptAnswer("website", mPlace.getWebsiteUri().toString(), mPrompt.getId()));

            return answers;
        }
    }

    @Override
    public void onLocationSelected(Place p) {
        mPlace = p;
        setLocationButtonText();
        setSaveButtonIcon();
    }
}
