package com.scsvn.whc_2016.main.nangsuatnhanvien;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.scsvn.whc_2016.R;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Trần Xuân Lộc on 1/26/2016.
 */
public class DepartmentAdapter extends ArrayAdapter<ComboDepartmentID> {
    private LayoutInflater inflater;

    public DepartmentAdapter(Context context, ArrayList<ComboDepartmentID> objects) {
        super(context, 0, objects);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view;
        if (convertView == null)
            view = (TextView) inflater.inflate(R.layout.simple_list_item_2, parent, false);
        else view = (TextView) convertView;
        ComboDepartmentID item = getItem(position);
        view.setText(String.format(Locale.getDefault(), "%s", item.getNameShort()));
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) inflater.inflate(R.layout.simple_list_item_2, parent, false);
        ComboDepartmentID item = getItem(position);
        view.setText(String.format(Locale.getDefault(), "%s", item.getNameShort()));
        return view;

    }
}