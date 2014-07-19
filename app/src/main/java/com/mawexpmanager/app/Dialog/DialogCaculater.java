package com.mawexpmanager.app.Dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mawexpmanager.app.R;

import java.text.DecimalFormat;

/**
 * Created by maww on 2014/3/24.
 */
public class DialogCaculater extends DialogFragment {
    private TextView tv_cost;
    private Button[] btn_numeber=new Button[10];
    private ImageButton btn_bacspace;
    private DialogClickListener mlistener;
    private DecimalFormat costFm=new DecimalFormat("#,###,###");
    private int cost;
    private View.OnClickListener numericKeyPad=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //DecimalFormat nf=new DecimalFormat("#,###,###");
            switch (v.getId()){
                case R.id.btn_backspace:
                    cost=cost/10;
                    tv_cost.setText(costFm.format(cost));
                    break;
                default:
                    if(Integer.toString(cost).length()<9) {
                        Button b = (Button) v;
                        cost = cost * 10 + Integer.parseInt(b.getText().toString());
                        tv_cost.setText(costFm.format(cost));
                    }
            }
        }
    };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        LayoutInflater inflater=getActivity().getLayoutInflater();
        View v=inflater.inflate(R.layout.dialog_caculater, null);
        builder.setView(v)
                .setPositiveButton(R.string.sign, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        TextView textView=(TextView)getActivity().findViewById(R.id.activity_cost);
                        textView.setText(costFm.format(cost));
                        mlistener.onClick(cost);
                    }
                })
                .setNegativeButton(R.string.cancel,null);
        findViews(v);
        return builder.create();
    }

    private void findViews(View v){
        tv_cost=(TextView)v.findViewById(R.id.dialog_cost);
        tv_cost.setText(costFm.format(cost));
        for(int i=0;i<=9;i++){
            String btnId="btn_"+i;
            int resId=getResources().getIdentifier(btnId,"id","com.mawexpmanager.app");
            btn_numeber[i]=(Button)v.findViewById(resId);
            btn_numeber[i].setOnClickListener(numericKeyPad);
        }
        btn_bacspace=(ImageButton)v.findViewById(R.id.btn_backspace);
        btn_bacspace.setOnClickListener(numericKeyPad);
    }

    public void setClickListener(DialogClickListener lis){
        this.mlistener=lis;
    }
    public void setCost(int cost){
        this.cost=cost;
    }
}
