package com.mawexpmanager.app;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Overview extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private TextView tv_costSum;
    private TextView tv_currentMonth;
    private ExpandableListView elv;
    private int monthlySum = 0;

    private ExpandableAdapter adapter;

    private void findViews() {
        tv_costSum = (TextView) findViewById(R.id.costSum);
        tv_currentMonth = (TextView) findViewById(R.id.currentMonth);
        elv = (ExpandableListView) findViewById(R.id.overview_elv);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        findViews();

        getLoaderManager().initLoader(-1,null,this);
        getLoaderManager().initLoader(-2,null,this);
        //setMonthlySum();
        adapter = new ExpandableAdapter(null, this);
        elv.setAdapter(adapter);
        elv.setOnChildClickListener(new ExpandableListView.OnChildClickListener(){
            @Override
            public boolean onChildClick(ExpandableListView parents,View v,int groupPosition,int childPosition,long id){
                //.d("id",String.valueOf(id));
                Uri uri=Uri.parse(DbProvider.CONTENT_URI+"/"+id);
                Intent intent=new Intent(Overview.this,EditBill.class);
                intent.putExtra(DbProvider.CONTENT_ITEM_TYPE,uri);
                startActivity(intent);
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_new) {
            Intent intent = new Intent(Overview.this, EditBill.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        CursorLoader cursorLoader;
        Uri uri=null;
        String[] projection=null;
        String selection=null;
        String[] selectionArgs=null;
        String sortOrder=null;
        switch (id){
            case -1:
                uri= DbProvider.CONTENT_URI;
                projection=new String[]{"sum(cost) as sum"};
                selection="strftime('%Y-%m',date/1000,'unixepoch')=?";
                selectionArgs=new String[]{appFormater.sdf.format(System.currentTimeMillis())};
                break;
            case -2:
                uri= Uri.parse(DbProvider.CONTENT_URI+"/categorySum");
                projection=new String[]{"cat_id AS _id", "sum(cost) AS sum", "cat_name", "cat_color" };
                selection="strftime('%Y-%m',date/1000,'unixepoch')=?";
                selectionArgs=new String[]{appFormater.sdf.format(System.currentTimeMillis())};
                sortOrder=DbProvider.KEY_SUM+" DESC";
                break;
            default:
                uri= DbProvider.CONTENT_URI;
                projection=new String[]{DbProvider.KEY_ID, DbProvider.KEY_COST, DbProvider.KEY_DATE, DbProvider.KEY_NOTE};
                selection="strftime('%Y-%m',date/1000,'unixepoch')=? AND cat_id=?";
                selectionArgs=new String[]{appFormater.sdf.format(System.currentTimeMillis()),args.getString(DbProvider.KEY_CATEGORY)};
        }
        cursorLoader=new CursorLoader(this,uri,projection,selection,selectionArgs,sortOrder);
        return cursorLoader;
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader,Cursor data){
        int id=loader.getId();
        switch (id){
            case -1:
                data.moveToFirst();
                monthlySum = data.getInt(data.getColumnIndex(DbProvider.KEY_SUM));
                tv_costSum.setText(appFormater.df.format(monthlySum));
                break;
            case -2:
                data.moveToFirst();
                adapter.setGroupCursor(data);
                break;
            default:
                data.moveToFirst();
                if(adapter.getGroup(loader.getId())!=null) {
                    adapter.setChildrenCursor(loader.getId(), data);
                }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        int id=loader.getId();
        switch (id){
            case -1:
                break;
            case -2:
                adapter.setGroupCursor(null);
                break;
            default:
                if(adapter.getGroup(loader.getId())!=null) {
                    adapter.setChildrenCursor(loader.getId(), null);
                }
        }

    }

    class ExpandableAdapter extends CursorTreeAdapter {
        private LayoutInflater mInflater;

        public ExpandableAdapter(Cursor cursor, Context context) {
            super(cursor, context);
        }

        public View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
            mInflater = getLayoutInflater();
            View v = mInflater.inflate(R.layout.listview_group_overview, null);
            return v;
        }

        public void bindGroupView(View view, Context context, Cursor cursor, boolean isLastChild) {
            if(monthlySum!=0) {
                TextView tv_catName = (TextView) view.findViewById(R.id.cat_name);
                ProgressBar pb_catPercentage = (ProgressBar) view.findViewById(R.id.cat_percentage);
                tv_catName.setText(cursor.getString(2) + " " + appFormater.df.format(cursor.getInt(1)) + " NT$");
                pb_catPercentage.setProgress(100 * cursor.getInt(1) / monthlySum);
                tv_catName.setTextColor(Color.parseColor(cursor.getString(3)));
                pb_catPercentage.getProgressDrawable().setColorFilter(Color.parseColor(cursor.getString(3)), PorterDuff.Mode.SRC);
            }
        }

        public View newChildView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
            mInflater = getLayoutInflater();
            View v = mInflater.inflate(R.layout.listview_child_overview, null);
            return v;
        }

        public void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
            TextView tv_dayOfWeek = (TextView) view.findViewById(R.id.expense_dayOfWeek);
            TextView tv_date = (TextView) view.findViewById(R.id.expense_date);
            TextView tv_note = (TextView) view.findViewById(R.id.expense_note);
            TextView tv_cost = (TextView) view.findViewById(R.id.expense_cost);
            tv_dayOfWeek.setText(appFormater.sdf_week.format(cursor.getLong(2)));
            tv_date.setText(appFormater.sdf_day.format(cursor.getLong(2)));
            tv_note.setText(cursor.getString(3));
            tv_cost.setText(appFormater.df.format(cursor.getInt(1)) + " NT$");
        }

        public Cursor getChildrenCursor(Cursor groupCursor) {
            int id=groupCursor.getPosition();
            Bundle args=new Bundle();
            String cat_id=groupCursor.getString(groupCursor.getColumnIndex(DbProvider.KEY_ID));
            args.putString(DbProvider.KEY_CATEGORY,cat_id);

            Loader loader=Overview.this.getLoaderManager().getLoader(id);
            if(loader!=null&&!loader.isReset()){
                Overview.this.getLoaderManager().restartLoader(id,args,Overview.this);
            }else {
                getLoaderManager().initLoader(id,args,Overview.this);
            }

            return null;
        }
    }


    /*private void setMonthlySum() {
        Cursor cursor;

        Uri uri= DbProvider.CONTENT_URI;
        String[] projection=new String[]{"sum(cost) as sum"};
        String selection="strftime('%Y-%m',date/1000,'unixepoch')=?";
        String[] selectionArgs=new String[]{appFormater.sdf.format(System.currentTimeMillis())};

        cursor=getContentResolver().query(uri,projection,selection,selectionArgs,null);
        cursor.moveToFirst();
        monthlySum = cursor.getInt(cursor.getColumnIndex(DbProvider.KEY_SUM));
        cursor.close();
        tv_costSum.setText(appFormater.df.format(monthlySum));

        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int StringID = this.getResources().getIdentifier("month_" + Integer.toString(month), "string", "com.mawexpmanager.app");
        tv_currentMonth.setText(StringID);
    }*/
}
