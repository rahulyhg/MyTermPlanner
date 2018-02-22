package com.proj.abhi.mytermplanner.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.TimePicker;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.services.AlarmClient;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.CustomException;
import com.proj.abhi.mytermplanner.utils.MaskWatcher;
import com.proj.abhi.mytermplanner.utils.Utils;

import java.util.Calendar;
import java.util.Date;

public class GenericActivity extends AppCompatActivity
{

    protected Uri currentUri;
    protected CoordinatorLayout mCoordinatorLayout;
    protected String intentMsg;
    protected AlarmClient alarmClient;
    protected int defaultTabIndex=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    save();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        alarmClient = new AlarmClient(this);
        alarmClient.doBindService();
    }

    protected void addLayout(int id){
        RelativeLayout home = findViewById(R.id.content_home);
        LayoutInflater inflater = LayoutInflater.from(this);
        View inflatedLayout= inflater.inflate(id, null, false);
        home.addView(inflatedLayout);
    }

    protected void initTabs(){
        TabHost tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setup();

        final TabWidget tabWidget = tabHost.getTabWidget();
        final FrameLayout tabContent = tabHost.getTabContentView();

        // Get the original tab textviews and remove them from the viewgroup.
        TextView[] originalTextViews = new TextView[tabWidget.getTabCount()];
        for (int index = 0; index < tabWidget.getTabCount(); index++) {
            originalTextViews[index] = (TextView) tabWidget.getChildTabViewAt(index);
        }
        tabWidget.removeAllViews();

        // Ensure that all tab content childs are not visible at startup.
        for (int index = 0; index < tabContent.getChildCount(); index++) {
            tabContent.getChildAt(index).setVisibility(View.GONE);
        }

        // Create the tabspec based on the textview childs in the xml file.
        // Or create simple tabspec instances in any other way...
        for (int index = 0; index < originalTextViews.length; index++) {
            final TextView tabWidgetTextView = originalTextViews[index];
            final View tabContentView = tabContent.getChildAt(index);
            TabHost.TabSpec tabSpec = tabHost.newTabSpec((String) tabWidgetTextView.getTag());
            tabSpec.setContent(new TabHost.TabContentFactory() {
                @Override
                public View createTabContent(String tag) {
                    return tabContentView;
                }
            });
            if (tabWidgetTextView.getBackground() == null) {
                tabSpec.setIndicator(tabWidgetTextView.getText());
            } else {
                tabSpec.setIndicator(tabWidgetTextView.getText(), tabWidgetTextView.getBackground());
            }
            tabHost.addTab(tabSpec);
        }
        selectDefaultTab();

    }

    protected void handleRotation(Bundle savedInstanceState,boolean refresh){
        // Recreate state if applicable.
        if (savedInstanceState != null) {
            // Get selected uri from saved state.
            if(savedInstanceState.containsKey("savedUri"))
                currentUri = Uri.parse((String) savedInstanceState.get("savedUri"));
            if(refresh){
                refreshPage(getCurrentUriId());
                refreshMenu();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Persist selected uri across orientation changes.
        if(currentUri!=null)
            outState.putString("savedUri", currentUri.toString());
    }

    protected int getCurrentUriId(){
        try{
            return Integer.parseInt(currentUri.getLastPathSegment());
        }catch (Exception e){
            return 0;
        }
    }

    protected void selectNavItem(SubMenu subMenu) {
        MenuItem item = subMenu.findItem(getCurrentUriId());
        if(item!=null){
            item.setCheckable(true);
            item.setChecked(true);
        }
    }

    protected void unSelectCurrNavItem(int groupId) {
        int prevId = getCurrentUriId();
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navView.getMenu();
        SubMenu subMenu = menu.findItem(groupId).getSubMenu();
        MenuItem prevItem = subMenu.findItem(prevId);
        prevItem.setChecked(false);
    }

    protected void refreshMenu(){}

    protected void save() throws Exception{}

    protected void addItemsInNavMenuDrawer(Cursor c) {}

    protected void doAbout(MenuItem item){
        int id = item.getItemId();

        if (id==R.id.nav_about) {
            LayoutInflater li = LayoutInflater.from(this);
            final View aboutView = li.inflate(R.layout.about_alert, null);
            DialogInterface.OnCancelListener dialogClickListener =
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {}
                    };

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setView(aboutView);
            alertDialogBuilder.setTitle(R.string.about)
                    .setOnCancelListener(dialogClickListener);
            alertDialogBuilder.show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
        refreshPage(getCurrentUriId());
    }

    protected void emptyPage(){}

    protected void refreshPage(int id){}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    // Call to update the share intent
    protected void doShare() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,intentMsg);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "From "+Constants.APP_NAME);

        startActivity(Intent.createChooser(shareIntent, "From "+Constants.APP_NAME));
    }

    protected void selectDefaultTab(){
        TabHost tabHost = (TabHost)findViewById(R.id.tabHost);
        if(tabHost.getTabWidget().getTabCount()<defaultTabIndex){
            defaultTabIndex=0;
        }
        tabHost.setCurrentTab(defaultTabIndex);
    }

    protected void setDefaultTabIndex(int index){
        defaultTabIndex=index;
    }

    @Override
    protected void onStop() {
        stopAlertClient();
        super.onStop();
    }

    protected void stopAlertClient(){
        if(alarmClient != null)
            alarmClient.doUnbindService();
    }

    public void setAlarmForDate(Date date,Bundle userBundle){
       if(date.before(new Date())){
            Snackbar.make(mCoordinatorLayout, R.string.error_alarm_before, Snackbar.LENGTH_LONG).show();
        }else{
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            alarmClient.setAlarmForNotification(date,userBundle);
            Snackbar.make(mCoordinatorLayout, "Notification set for "+c.getTime(), Snackbar.LENGTH_LONG).show();
        }
    }

    protected void createReminder(String[] list, int[] listIds,Bundle userBundle){
        try{
            final int[] ids = listIds;
            final Bundle b=userBundle;
            LayoutInflater li = LayoutInflater.from(this);
            final View promptsView = li.inflate(R.layout.reminder_alert, null);
            final Spinner mSpinner= (Spinner) promptsView.findViewById(R.id.reminderDropdown);
            final TextView customDate = (TextView) promptsView.findViewById(R.id.reminderDate);
            final TimePicker timePicker =promptsView.findViewById(R.id.timePicker);

            list[list.length-1]=getString(R.string.custom_date);
            listIds[listIds.length-1]=R.id.reminderDate;
            timePicker.setCurrentMinute(timePicker.getCurrentMinute()+1);

            mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    if(parentView.getItemAtPosition(position).toString().equals(getString(R.string.custom_date))){
                        customDate.setVisibility(View.VISIBLE);
                        customDate.addTextChangedListener(new MaskWatcher("##/##/####"));
                        customDate.setText(Utils.getCurrentDate());
                    }else{
                        customDate.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }

            });

            DialogInterface.OnClickListener dialogClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int button) {
                            if (button == DialogInterface.BUTTON_POSITIVE) {
                                try{
                                    TextView date = findViewById(ids[mSpinner.getSelectedItemPosition()]);
                                    if(date==null){
                                        date = promptsView.findViewById(ids[mSpinner.getSelectedItemPosition()]);
                                        b.putString(Constants.PersistAlarm.CONTENT_TEXT,getString(R.string.notification_date));
                                    }else{
                                        b.putString(Constants.PersistAlarm.CONTENT_TEXT,mSpinner.getSelectedItem().toString());
                                    }
                                    if(Utils.isValidDate(date.getText().toString())) {
                                        Date alarmDate = Utils.getDate(date.getText().toString());
                                        alarmDate.setHours(timePicker.getCurrentHour());
                                        alarmDate.setMinutes(timePicker.getCurrentMinute());
                                        setAlarmForDate(alarmDate,b);
                                    }
                                }catch (Exception e){
                                    if(e instanceof CustomException){
                                        Snackbar.make(mCoordinatorLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
                                    }else{
                                        Snackbar.make(mCoordinatorLayout, R.string.error_alarm_failed, Snackbar.LENGTH_LONG).show();
                                    }
                                    e.printStackTrace();
                                }
                            }
                        }
                    };

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setView(promptsView);
            alertDialogBuilder.setTitle(R.string.reminder_header)
                    .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                    .setNegativeButton(getString(android.R.string.no), dialogClickListener);

            final ArrayAdapter<String> adp = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, list);
            adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            final AlertDialog alertDialog = alertDialogBuilder.create();
            mSpinner.setAdapter(adp);
            alertDialog.show();
            alertDialog.setCanceledOnTouchOutside(false);
        }catch(Exception e){
            Snackbar.make(mCoordinatorLayout, R.string.error_alarm_failed, Snackbar.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    protected void doAlert(DialogInterface.OnClickListener dialogClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.are_you_sure))
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();
    }
}
