package com.scsvn.whc_2016.main.mms.detail;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.scsvn.whc_2016.R;
import com.scsvn.whc_2016.preferences.LoginPref;
import com.scsvn.whc_2016.retrofit.MaintenanceJobDetailUpdateParameter;
import com.scsvn.whc_2016.retrofit.MyRetrofit;
import com.scsvn.whc_2016.retrofit.NoInternet;
import com.scsvn.whc_2016.retrofit.RetrofitError;
import com.scsvn.whc_2016.utilities.WifiHelper;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by Trần Xuân Lộc on 1/26/2016.
 */
public class Layout4Adapter extends RecyclerView.Adapter<Layout4Adapter.VH> implements MJDetailAdapter {
    private static final String TAG = Layout4Adapter.class.getSimpleName();
    private LayoutInflater inflater;
    private ArrayList<Object> objects;
    private Context context;
    private View snackBarView;
    private String userName;
    private SparseBooleanArray itemSelected = new SparseBooleanArray();

    public Layout4Adapter(Context context, ArrayList<Object> objects, View view) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.objects = objects;
        this.context = context;
        snackBarView = view;
        userName = LoginPref.getUsername(context);
    }

    @Override
    public int getItemViewType(int position) {
        return objects.get(position) instanceof Header ? 0 : 1;
    }

    public void setSelected(int position) {
        if (!itemSelected.get(position, false)) {
            if (itemSelected.size() > 0) {
                int key = itemSelected.keyAt(0);
                itemSelected.put(key, false);
                notifyItemChanged(key);
                itemSelected.delete(key);
            }
            itemSelected.put(position, true);
            notifyItemChanged(position);

        }
    }

    @Override
    public void updateAll() {

    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = inflater.inflate(R.layout.item_mj_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_mj_detail_layout_4, parent, false);
            return new DetailViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            Header header = (Header) objects.get(position);
            ((HeaderViewHolder) holder).titleTV.setText(header.getTitle());
        } else {
            final MaintenanceJobDetail detail = (MaintenanceJobDetail) objects.get(position);
            final DetailViewHolder detailHolder = (DetailViewHolder) holder;

            detailHolder.nameTV.setText(detail.getItemName());
            detailHolder.noteET.setText(detail.getRemark());
            detailHolder.resultCB.setChecked(detail.isCheckResult());
            detailHolder.noteET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        detail.setRemark(detailHolder.noteET.getText().toString());
                    }
                    return false;
                }
            });
            detailHolder.resultCB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    detail.setCheckResult(detailHolder.resultCB.isChecked());
                }
            });
            detailHolder.updateIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateMaintenanceJobDetail(detail.getId(), detail.isCheckResult(), detail.getRemark(), userName);
                }
            });
            detailHolder.root.setSelected(itemSelected.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    private void updateMaintenanceJobDetail(int maintenanceJobDetailID, boolean result, String remark, String userName) {
        if (!WifiHelper.isConnected(context)) {
            RetrofitError.errorNoAction(context, new NoInternet(), TAG, snackBarView);
            return;
        }
        MyRetrofit.initRequest(context)
                .updateMaintenanceJobDetail(new MaintenanceJobDetailUpdateParameter(maintenanceJobDetailID, remark, userName, result, '2'))
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Response<String> response, Retrofit retrofit) {
                        if (response.isSuccess() && response.body() != null) {

                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        RetrofitError.errorNoAction(context, t, TAG, snackBarView);
                    }
                });
    }

    class VH extends RecyclerView.ViewHolder {

        public VH(View itemView) {
            super(itemView);
        }
    }

    class DetailViewHolder extends VH implements View.OnClickListener {
        TextView nameTV;
        EditText noteET;
        CheckBox resultCB;
        ImageView updateIV;
        LinearLayout root;

        public DetailViewHolder(View view) {
            super(view);
            nameTV = (TextView) view.findViewById(R.id.item_mj_detail_bt_ht_bao_chay_name);
            resultCB = (CheckBox) view.findViewById(R.id.item_mj_detail_bt_ht_bao_chay_result);
            noteET = (EditText) view.findViewById(R.id.item_mj_detail_bt_ht_bao_chay_note);
            updateIV = (ImageView) view.findViewById(R.id.item_mj_detail_update);
            root = (LinearLayout) view.findViewById(R.id.item_mj_detail_root);
            root.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            setSelected(getAdapterPosition());
        }
    }

    class HeaderViewHolder extends VH {
        TextView titleTV;

        public HeaderViewHolder(View view) {
            super(view);
            titleTV = (TextView) view.findViewById(R.id.item_mj_detail_header_title);
        }
    }
}