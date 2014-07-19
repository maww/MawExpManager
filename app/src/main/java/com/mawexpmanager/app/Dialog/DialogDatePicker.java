package com.mawexpmanager.app.Dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CalendarView;

import com.mawexpmanager.app.R;

/**
 * Created by maww on 2014/4/30.
 */
public class DialogDatePicker extends DialogFragment{
    private CalendarView cal;
    private DialogClickListener mlistener;
    private long date;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        LayoutInflater inflater=getActivity().getLayoutInflater();
        View v=inflater.inflate(R.layout.dialog_datepicker,null);
        builder.setView(v)
                .setPositiveButton(R.string.sign, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mlistener.onClick(cal.getDate());
                    }
                })
                .setNegativeButton(R.string.cancel,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                });
        findViews(v);
        return builder.create();
    }
    public void findViews(View v){
        cal=(CalendarView)v.findViewById(R.id.dialog_calendar);
        cal.setDate(date);
    }
    public void setClickListener(DialogClickListener lis){this.mlistener=lis;}
    public void setDate(long date){
        this.date=date;
    }
}
