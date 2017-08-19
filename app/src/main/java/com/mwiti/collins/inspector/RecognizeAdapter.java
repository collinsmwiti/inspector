package com.mwiti.collins.inspector;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import clarifai2.dto.prediction.Concept;

/**
 * Created by collins on 8/19/17.
 */

//class RecognizeAdapter inheriting from RecyclerView.Adapter under RecognizeAdapter.Holder
public class RecognizeAdapter extends RecyclerView.Adapter<RecognizeAdapter.Holder> {

    @NonNull
    private List<Concept> concepts = new ArrayList<>();

    //constructor RecognizeAdapter is used to check the concepts and notify if data set is changed
    public RecognizeAdapter setData(@NonNull List<Concept> concepts) {
        this.concepts = concepts;
        notifyDataSetChanged();
        return this;
    }

    // class holder inheriting from RecyclerView
    static final class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.label)
        TextView label;
        @BindView(R.id.probability) TextView probability;


    //constructor Holder
        public Holder(View root) {
            super(root);
            ButterKnife.bind(this, root);
        }
    }

    //used for counting
    @Override public int getItemCount() {
        return concepts.size();
    }

    //used getContext method to augment telemetry I have sent from parent(inherited class).
    @Override public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recognize_concept, parent, false));
    }

    //used to bind the context by checking its position, give the final concept, select concept by name if not null and id and give the
    //probability of the positioned concept
    @Override public void onBindViewHolder(Holder holder, int position) {
        final Concept concept = concepts.get(position);
        holder.label.setText(concept.name() != null ? concept.name() : concept.id());
        holder.probability.setText(String.valueOf(concept.value()));
    }

}