package com.gofoodgo.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import java.text.NumberFormat;
import java.util.Locale;

public final class Ui {
    private Ui() {}
    public static String value(TextInputEditText e) { return e.getText() == null ? "" : e.getText().toString().trim(); }
    public static String money(long v) { return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(v) + "đ"; }
    public static void toast(Context c, String message) { Toast.makeText(c, message, Toast.LENGTH_LONG).show(); }
    public static void open(Activity a, Class<?> dest) { a.startActivity(new Intent(a, dest)); }
}
