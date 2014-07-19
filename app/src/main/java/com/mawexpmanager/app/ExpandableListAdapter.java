package com.mawexpmanager.app;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * Created by maww on 2014/6/5.
 */
public class ExpandableListAdapter extends CursorTreeAdapter {
    private Context context=null;

    private LayoutInflater mInflater;
    private int maxCatSum;
    private DecimalFormat df=new DecimalFormat("#,###,###");
    private SimpleDateFormat sdf_date=new SimpleDateFormat("MM/dd");
    private SimpleDateFormat sdf_week=new SimpleDateFormat("E");
    //private QueryHandler queryHandler;

    private static final int TOKEN_GROUP=0;
    private static final int TOKEN_CHILD=1;

    /*private static final class QueryHandler extends AsyncQueryHandler{
        private CursorTreeAdapter adapter;

        public QueryHandler(Context context,CursorTreeAdapter adapter){
            super(context.getContentResolver());
            this.adapter=adapter;
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor){
            switch (token){
                case TOKEN_GROUP:
                    adapter.setGroupCursor(cursor);
                    break;
                case TOKEN_CHILD:
                    int groupPosition=(Integer)cookie;
                    adapter.setChildrenCursor(groupPosition,cursor);
                    break;
            }
        }

    }*/

    public ExpandableListAdapter(Cursor cursor, Context context){
        super(cursor,context);
        this.context=context;
        mInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //cursor.moveToFirst();
        //maxCatSum=cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_SUM));
    }
    @Override
    public void bindGroupView(View v, Context context, Cursor cursor, boolean isLastChild){
        TextView tv_catName=(TextView)v.findViewById(R.id.cat_name);
        ProgressBar pb_catPercentage=(ProgressBar)v.findViewById(R.id.cat_percentage);
        tv_catName.setText(cursor.getString(cursor.getColumnIndex(DbProvider.KEY_CATEGORY_NAME))+" "+df.format(cursor.getInt(cursor.getColumnIndex(DbProvider.KEY_SUM)))+" NT$");
        //pb_catPercentage.setProgress(100*cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_SUM))/maxCatSum);
        tv_catName.setTextColor(Color.parseColor(cursor.getString(cursor.getColumnIndex(DbProvider.KEY_CATEGORY_COLOR))));
        pb_catPercentage.getProgressDrawable().setColorFilter(Color.parseColor(cursor.getString(cursor.getColumnIndex(DbProvider.KEY_CATEGORY_COLOR))), PorterDuff.Mode.SRC);
        cursor.close();
    }
    @Override
    public void bindChildView(View v, Context context, Cursor cursor, boolean isLastChild){
        TextView tv_dayOfWeek=(TextView)v.findViewById(R.id.expense_dayOfWeek);
        TextView tv_date=(TextView)v.findViewById(R.id.expense_date);
        TextView tv_note=(TextView)v.findViewById(R.id.expense_note);
        TextView tv_cost=(TextView)v.findViewById(R.id.expense_cost);
        tv_dayOfWeek.setText(sdf_week.format(cursor.getLong(2)));
        tv_date.setText(sdf_date.format(cursor.getLong(2)));
        tv_note.setText(cursor.getString(3));
        tv_cost.setText(df.format(cursor.getInt(1))+" NT$");
    }
    @Override
    public View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent){
        View v=mInflater.inflate(R.layout.listview_group_overview,null);
        return  v;
    }
    @Override
    public View newChildView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent){
        View v=mInflater.inflate(R.layout.listview_child_overview,null);
        return v;
    }
    @Override
    public Cursor getChildrenCursor(Cursor groupCursor){
        String cat_id=groupCursor.getString(groupCursor.getColumnIndex(DbProvider.KEY_ID));
        //getContentResolver
        return null;
    }

        /*@Override
        public int getGroupCount(){}
        @Override
        public int getChildrenCount(int groupPosition){}
        @Override
        public Cursor getGroup(int groupPosition){}
        @Override
        public View getGroupView(int groupPosition, boolean isLastGroup, View convertView, ViewGroup parent){}
        @Override
        public Cursor getChild(int groupPosition, int childPosition){}
        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent){}
        @Override
        public long getGroupId(int groupPosition){}
        @Override
        public long getChildId(int groupPosition, int childPosition){}


        @Override
        public boolean hasStableIds(){}
        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition){}*/
}

