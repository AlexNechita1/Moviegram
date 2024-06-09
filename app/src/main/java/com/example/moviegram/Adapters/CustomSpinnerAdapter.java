package com.example.moviegram.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.moviegram.R;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private String[] mOptions;

    public CustomSpinnerAdapter(Context context, int resource, String[] options) {
        super(context, resource, options);
        this.mContext = context;
        this.mOptions = options;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView = inflater.inflate(R.layout.spinner_item_layout, parent, false);

        TextView optionTextView = itemView.findViewById(R.id.option_text_view);
        optionTextView.setText(mOptions[position]);

        return itemView;
    }
}
