package com.kadabra.agent.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kadabra.agent.R;
import com.kadabra.agent.model.Courier;
import com.kofigyan.stateprogressbar.StateProgressBar;

import java.util.ArrayList;
import java.util.List;

public class CourierListAdapter extends ArrayAdapter<Courier> {
    private List<Courier> courierListFull;
    private String pickUpData = "";
    private String dropOffData = "";


    public CourierListAdapter(@NonNull Context context, Integer resourceId, @NonNull List<Courier> courierList) {
        super(context, resourceId, courierList);
        courierListFull = new ArrayList<>(courierList);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return courierFilter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.courier_layout, parent, false
            );
        }

        ImageView imageViewFlag = convertView.findViewById(R.id.ivCourierImage);
        TextView tvName = convertView.findViewById(R.id.tvName);
        TextView tvMobile = convertView.findViewById(R.id.tvMobile);
        TextView tvHaveTaskNow = convertView.findViewById(R.id.tvHaveTaskNow);
        StateProgressBar progress = convertView.findViewById(R.id.progress);


        Courier courier = getItem(position);

        if (courier != null) {
            tvName.setText(courier.getCourierName());
            tvMobile.setText(courier.getCourierMobile());
            if (courier.getHasTasksNow()) {
                tvHaveTaskNow.setText("Has Task");
                tvHaveTaskNow.setTextColor(Color.parseColor("#E54728"));
            } else {
                tvHaveTaskNow.setText("No Task");
                tvHaveTaskNow.setTextColor(Color.parseColor("#ff000000"));
            }

            if (courier.getVehicleTypeID() == 2)
                imageViewFlag.setImageResource(R.drawable.bike);
            else
                imageViewFlag.setImageResource(R.drawable.car);

            if (courier.getPickupName() != null && courier.getDropoffName() != null)
            {

                pickUpData = courier.getPickupName();
                dropOffData = courier.getDropoffName();

//                if (pickUpData.length() > 20 || dropOffData.length() > 20) {
//                    String[] pickUpArrayData = pickUpData.split("\\W+");
//                    String[] dropOffArrayData = dropOffData.split("\\W+");
//                    pickUpData = pickUpArrayData.toString();
//                    dropOffData = dropOffArrayData.toString();
//                }

                pickUpData= ellipsize(pickUpData,20);
                dropOffData= ellipsize(dropOffData,20);

                String[] descriptionData = {pickUpData,
                        dropOffData};

                progress.setStateDescriptionData(descriptionData);
                progress.setVisibility(View.VISIBLE);

            } else
                progress.setVisibility(View.INVISIBLE);


        }
        return convertView;
    }

    private Filter courierFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Courier> suggestions = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                suggestions.addAll(courierListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Courier item : courierListFull) {
                    if (item.getCourierName().toLowerCase().contains(filterPattern)) {
                        suggestions.add(item);
                    }
                }
            }

            results.values = suggestions;
            results.count = suggestions.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((List) results.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((Courier) resultValue).getCourierName();
        }
    };

    private String ellipsize(String input, int maxLength) {
        String ellip = "...";
        if (input == null || input.length() <= maxLength
                || input.length() < ellip.length()) {
            return input;
        }
        return input.substring(0, maxLength - ellip.length()).concat(ellip);
    }
}
