package com.scsvn.whc_2016.main.lichlamviec;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;
import com.scsvn.whc_2016.R;
import com.scsvn.whc_2016.preferences.LoginPref;
import com.scsvn.whc_2016.retrofit.MyRetrofit;
import com.scsvn.whc_2016.retrofit.NoInternet;
import com.scsvn.whc_2016.retrofit.RetrofitError;
import com.scsvn.whc_2016.retrofit.WorkingSchedulesParameter;
import com.scsvn.whc_2016.utilities.Utilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class CalendarViewActivity extends AppCompatActivity {
    private final String TAG = CalendarViewActivity.class.getSimpleName();
    private ProgressDialog dialog;
    private LinkedHashMap<String, List<WorkingSchedulesEmployeePlanInfo>> employeePlan = new LinkedHashMap<>();
    private List<String> employeePlanGroup = new LinkedList<>();
    private ArrayList<WorkingSchedulesInfo> listJob = new ArrayList<>();
    private CaldroidFragment caldroidFragment;
    private String reportDate;
    private Date tDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Utilities.showBackIcon(getSupportActionBar());
        caldroidFragment = new CaldroidFragment();

        if (savedInstanceState != null) {
            caldroidFragment.restoreStatesFromKey(savedInstanceState, "CALDROID_SAVED_STATE");
        } else {
            Bundle args = new Bundle();
            Calendar cal = Calendar.getInstance();
            args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
            args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
            args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true);
            args.putBoolean(CaldroidFragment.ENABLE_CLICK_ON_DISABLED_DATES, false);
            args.putInt(CaldroidFragment.START_DAY_OF_WEEK, CaldroidFragment.MONDAY);
            args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, false);
            caldroidFragment.setArguments(args);
        }
        setCustomResourceForDates();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.calendar, caldroidFragment).commit();
        caldroidFragment.setCaldroidListener(new CaldroidListener() {
            @Override
            public void onSelectDate(Date date, View view) {
                if (tDate != null)
                    caldroidFragment.clearSelectedDate(tDate);
                reportDate = Utilities.formatDateTime_yyyyMMddHHmmssFromMili(date.getTime());
                caldroidFragment.setSelectedDate(date);
                caldroidFragment.refreshView();
                getWorkingSchedules(findViewById(R.id.view));
                tDate = date;
            }

            @Override
            public void onChangeMonth(int month, int year) {
            }

            @Override
            public void onCaldroidViewCreated() {
                TextView monthTitle = caldroidFragment.getMonthTitleTextView();
                Log.d(TAG, "onCaldroidViewCreated() returned: " + monthTitle.getText().toString());
                monthTitle.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            }
        });

    }

    private void setCustomResourceForDates() {
        Calendar cal = Calendar.getInstance();

        // Min date is last 7 days
        cal.add(Calendar.DATE, -7);
        Date blueDate = cal.getTime();

        if (caldroidFragment != null) {
            ColorDrawable blue = new ColorDrawable(Color.BLUE);
            caldroidFragment.setBackgroundDrawableForDate(blue, blueDate);
            caldroidFragment.setTextColorForDate(R.color.white, blueDate);
        }
    }

    private void getWorkingSchedules(final View view) {
        dialog = Utilities.getProgressDialog(this, "Đang tải dữ liệu...");
        dialog.show();
        if (!Utilities.isConnected(this)) {
            RetrofitError.errorNoAction(this, new NoInternet(), TAG, view);
            dialog.dismiss();
        }
        WorkingSchedulesParameter parameter = new WorkingSchedulesParameter(reportDate, LoginPref.getUsername(this));
        MyRetrofit.initRequest(this).getWorkingSchedules(parameter).enqueue(new Callback<List<WorkingSchedulesInfo>>() {
            @Override
            public void onResponse(Response<List<WorkingSchedulesInfo>> response, Retrofit retrofit) {
                Log.e(TAG, "onResponse: " + new Gson().toJson(response.body()));
                if (response.isSuccess() && response.body() != null && response.body().size() > 0) {
                    listJob.clear();
                    listJob.addAll(response.body());
                    getWorkingSchedulesEmployeePlan(view);
                } else
                    dialog.dismiss();
            }

            @Override
            public void onFailure(Throwable t) {
                RetrofitError.errorNoAction(CalendarViewActivity.this, t, TAG, view);
            }
        });
    }

    private void getWorkingSchedulesEmployeePlan(final View view) {
        if (!Utilities.isConnected(this)) {
            RetrofitError.errorNoAction(this, new NoInternet(), TAG, view);
            dialog.dismiss();
        }
        WorkingSchedulesParameter parameter = new WorkingSchedulesParameter(reportDate, LoginPref.getUsername(this));
        MyRetrofit.initRequest(this).getWorkingSchedulesEmployeePlan(parameter).enqueue(new Callback<List<WorkingSchedulesEmployeePlanInfo>>() {
            @Override
            public void onResponse(Response<List<WorkingSchedulesEmployeePlanInfo>> response, Retrofit retrofit) {
                Log.e(TAG, "onResponse: " + new Gson().toJson(response.body()));
                if (response.isSuccess() && response.body() != null && response.body().size() > 0) {
                    employeePlanGroup.clear();
                    employeePlan.clear();
                    for (WorkingSchedulesEmployeePlanInfo info : response.body()) {
                        String key = info.VietnamPosition;
                        if (!employeePlan.containsKey(key)) {
                            List<WorkingSchedulesEmployeePlanInfo> item = new LinkedList<>();
                            item.add(info);
                            employeePlan.put(key, item);
                        } else employeePlan.get(key).add(info);
                    }
                    employeePlanGroup.addAll(employeePlan.keySet());

                    showAlertDialog();
                }
                dialog.dismiss();
            }

            @Override
            public void onFailure(Throwable t) {
                RetrofitError.errorNoAction(CalendarViewActivity.this, t, TAG, view);
                dialog.dismiss();

            }
        });
    }

    private void showAlertDialog() {
        ListView listView = new ListView(this);
        ExpandableListView workingEmployeePlan = new ExpandableListView(this);
        LinearLayout viewDialog = new LinearLayout(this);
        viewDialog.setOrientation(LinearLayout.VERTICAL);
        viewDialog.addView(listView);
        viewDialog.addView(workingEmployeePlan);
        workingEmployeePlan.setGroupIndicator(null);
        WorkingSchedulesAdapter adapter = new WorkingSchedulesAdapter(this, listJob);
        WorkingSchedulesEmployeePlanAdapter planAdapter = new WorkingSchedulesEmployeePlanAdapter(employeePlan, employeePlanGroup);
        listView.setAdapter(adapter);
        workingEmployeePlan.setAdapter(planAdapter);
        int size = employeePlanGroup.size();
        for (int i = 0; i < size; ++i) {
            workingEmployeePlan.expandGroup(i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(viewDialog);
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home)
            onBackPressed();
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (caldroidFragment != null) {
            caldroidFragment.saveStatesToKey(outState, "CALDROID_SAVED_STATE");
        }
    }
}
