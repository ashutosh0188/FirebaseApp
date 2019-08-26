package com.iadsa.firebaseapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

public class Utils {

    private Utils(){}

    public static ProgressDialog showDialog(Context context, String message, boolean isCancellable) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(isCancellable);
        progressDialog.show();
        return progressDialog;
    }

    public static void hideProgressDialog(ProgressDialog progressDialog) {
        if(progressDialog!=null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public static boolean validate(EditText editText, String message) {
        if(editText.getText().toString().isEmpty()) {
            editText.setError(message);
            return false;
        }
        return true;
    }

    public static void showToast(String message, Context context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
