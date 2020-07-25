package com.example.instagramclone.Dialog;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.instagramclone.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ConfirmPasswordDialog extends DialogFragment {
    private static final String TAG = "ConfirmPasswordDialog";
    private TextView confirmDialog,cancelDialog;
    //vars
    private EditText mPassword;


    public interface OnConfirmPasswordListener{
        public void onConfirmPassword(String password);
    }
    OnConfirmPasswordListener mOnConfirmPasswordListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confirm_password,container,false);
        Log.d(TAG, "onCreateView: started ");

        confirmDialog = (TextView)view.findViewById(R.id.dialogConfirm);
        cancelDialog = (TextView)view.findViewById(R.id.dialogCancel);
        mPassword = (EditText)view.findViewById(R.id.input_confirm_password);
        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the dialog");
                 getDialog().dismiss();
            }
        });

        confirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: capture password and confirming");

                String password = mPassword.getText().toString();
                if(!password.equals("")){
                    //Toast.makeText(getContext(), "on confirm click", Toast.LENGTH_SHORT).show();
                    mOnConfirmPasswordListener.onConfirmPassword(password);
                    getDialog().dismiss();
                }
                else{
                    Toast.makeText(getActivity(), "You must enter a password ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            mOnConfirmPasswordListener = (OnConfirmPasswordListener)getTargetFragment();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException " + e.getMessage());
            //Toast.makeText(getContext(), "onAttach: ClassCastException", Toast.LENGTH_SHORT).show();
        }
    }
}
