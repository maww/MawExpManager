package com.mawexpmanager.app;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.mawexpmanager.app.Dialog.DialogCaculater;
import com.mawexpmanager.app.Dialog.DialogClickListener;
import com.mawexpmanager.app.Dialog.DialogDatePicker;

import java.util.Calendar;
import java.util.Date;

public class EditBill extends ActionBarActivity implements DialogClickListener{

    private TextView tv_cost;
    private EditText btn_datePicker;
    private RadioGroup rg_category;
    private EditText et_note;
    private Button btn_billSign;
    private Button btn_billCancel;
    private int cost=0;
    private long date=0;
    private int cat_id=0;
    private String note=null;

    private Uri billUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bill);
        //setActionBar();
        findViews();
        setCategory();

        Bundle extras=getIntent().getExtras();
        if(extras==null){
            reset();
        }else{
            billUri=extras.getParcelable(DbProvider.CONTENT_ITEM_TYPE);
            setData(billUri);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        ActionBar actionBar=getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);

        if(billUri==null){

            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.action_bar_edit_bill);
            btn_billSign=(Button)findViewById(R.id.edit_bill_sign);
            btn_billCancel=(Button)findViewById(R.id.edit_bill_cancel);
            btn_billSign.setOnClickListener(actionBarClickListener);
            btn_billCancel.setOnClickListener(actionBarClickListener);
            return false;
        }else {
            getMenuInflater().inflate(R.menu.edit_bill, menu);
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.edit_bill_sign:
                note=et_note.getText().toString();
                ContentValues contentValues=new ContentValues();
                contentValues.put(DbProvider.KEY_COST,cost);
                contentValues.put(DbProvider.KEY_DATE,date);
                contentValues.put(DbProvider.KEY_CATEGORY,cat_id);
                contentValues.put(DbProvider.KEY_NOTE,note);
                getContentResolver().update(billUri,contentValues,null,null);
                finish();
                break;
            case R.id.edit_bill_delete:
                getContentResolver().delete(billUri,null,null);
                finish();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void setCategory(){
        RadioButton rb;
        Drawable rec;
        Cursor cursor;
        cursor=getContentResolver().query(DbProvider.CATEGORY_URI,null,null,null,null);
        cursor.moveToFirst();
        do{
            rb=(RadioButton)getLayoutInflater().inflate(R.layout.btn_radio_category,null);
            rb.setId(cursor.getInt(cursor.getColumnIndex(DbProvider.KEY_ID)));
            rb.setText(cursor.getString(cursor.getColumnIndex(DbProvider.KEY_CATEGORY_NAME)));
            rb.setTextColor(Color.parseColor(cursor.getString(cursor.getColumnIndex(DbProvider.KEY_CATEGORY_COLOR))));
            rec=rb.getCompoundDrawables()[0];
            rec.setColorFilter(Color.parseColor(cursor.getString(cursor.getColumnIndex(DbProvider.KEY_CATEGORY_COLOR))), PorterDuff.Mode.SRC);
            rg_category.addView(rb);
            rb.getLayoutParams().width= ViewGroup.LayoutParams.MATCH_PARENT;
        }while(cursor.moveToNext());
        rg_category.setOnCheckedChangeListener(categoryCheckedListener);
        cursor.close();
    }

    private void findViews(){
        tv_cost=(TextView)findViewById(R.id.activity_cost);
        btn_datePicker=(EditText)findViewById(R.id.dateDialog);
        rg_category=(RadioGroup)findViewById(R.id.categoryGroup);
        et_note=(EditText)findViewById(R.id.note);
    }

    private void reset(){
        cost=0;
        Calendar c=Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,0);
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0);
        c.set(Calendar.MILLISECOND,0);
        date=c.getTime().getTime();
        DialogCaculater caculater=new DialogCaculater();
        caculater.setClickListener(this);
        caculater.setCost(cost);
        caculater.show(getFragmentManager(),"dialog");
        btn_datePicker.setText("今日");
        RadioButton rb=(RadioButton)rg_category.getChildAt(cat_id);
        rb.setChecked(true);
    }

    private void setData(Uri uri){
        String[] projection=new String[]{"*"};
        Cursor cursor=getContentResolver().query(uri,projection,null,null,null);
        cursor.moveToFirst();
        cost=cursor.getInt(cursor.getColumnIndex(DbProvider.KEY_COST));
        tv_cost.setText(appFormater.df.format(cost));
        date=cursor.getLong(cursor.getColumnIndex(DbProvider.KEY_DATE));
        btn_datePicker.setText(appFormater.sdf_date.format(date));
        cat_id=cursor.getInt(cursor.getColumnIndex(DbProvider.KEY_CATEGORY));
        RadioButton rb=(RadioButton)rg_category.getChildAt(cat_id-1);
        rb.setChecked(true);
        note=cursor.getString(cursor.getColumnIndex(DbProvider.KEY_NOTE));
        et_note.setText(note);
        cursor.close();
    }

    public void openDialog(View v){
        switch(v.getId()){
            case R.id.calDialog:
                DialogCaculater caculater=new DialogCaculater();
                caculater.setClickListener(this);
                caculater.setCost(cost);
                caculater.show(getFragmentManager(),"dialog");
                break;
            case R.id.dateDialog:
                DialogDatePicker datePicker=new DialogDatePicker();
                datePicker.setClickListener(this);
                datePicker.setDate(date);
                datePicker.show(getFragmentManager(),"dialog");
                break;
        }

    }

    private RadioGroup.OnCheckedChangeListener categoryCheckedListener=new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
            cat_id=checkedId;
        }
    };
    private View.OnClickListener actionBarClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id=v.getId();
            switch (id){
                case R.id.edit_bill_sign:
                    note=et_note.getText().toString();
                    ContentValues contentValues=new ContentValues();
                    contentValues.put(DbProvider.KEY_COST,cost);
                    contentValues.put(DbProvider.KEY_DATE,date);
                    contentValues.put(DbProvider.KEY_CATEGORY,cat_id);
                    contentValues.put(DbProvider.KEY_NOTE,note);
                    getContentResolver().insert(DbProvider.CONTENT_URI, contentValues);
                    finish();
                    break;
                case R.id.edit_bill_cancel:
                    finish();
                    break;
            }
        }
    };

    public void onClick(int cost){
        this.cost=cost;
        //DecimalFormat dFormat=new DecimalFormat("#,###,###");
        tv_cost.setText(appFormater.df.format(cost));
    }
    public void onClick(long date){
        this.date=date;
        //SimpleDateFormat sdFormat=new SimpleDateFormat("yyyy/MM/dd");
        if(date==new Date().getTime()){
            btn_datePicker.setText("今日");
        }else{
            btn_datePicker.setText(appFormater.sdf_week.format(date));
        }
    }
}
