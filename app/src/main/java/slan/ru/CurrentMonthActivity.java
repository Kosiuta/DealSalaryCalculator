package slan.ru;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import slan.ru.models.MainPref;

public class CurrentMonthActivity extends AppCompatActivity {

    @SuppressLint("SimpleDateFormat")
    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate date;

    ImageButton nav_button_main, nav_button_month, nav_button_year, nav_button_preferences;
    DBHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_month);

        dbHelper = new DBHelper(this);
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        setNavButtons();
        setCurrentMonthBrif(database);

        database.close();
    }

    private void setCurrentMonthBrif(SQLiteDatabase database) {
        ArrayList<MainPref.PrefNote> prefNotes = MainPref.getInstance(CurrentMonthActivity.this).getOrderTypeList();
        Map<String,Integer> orderTypeBrif = new HashMap<>();
        for (MainPref.PrefNote note : prefNotes) {
            orderTypeBrif.put(note.getKey(), 0);
        }
        date = LocalDate.now();
        String[] days = new String[2];
        days[0] = date.format(format).substring(0,8) + "01";
        days[1] = date.format(format).substring(0,8) + date.getMonth().maxLength();
        Cursor cursor = database.query(DBHelper.TABLE_DAILY_NOTE, new String[]{DBHelper.KEY_ORDER_TYPE, DBHelper.KEY_PAYMENT},
                DBHelper.KEY_DATE + " BETWEEN ? AND ? ", days,null,null,null);
        String s = "";
        int orderTypeIndex = cursor.getColumnIndex(DBHelper.KEY_ORDER_TYPE);
        int paymentIndex = cursor.getColumnIndex(DBHelper.KEY_PAYMENT);
        if (cursor.moveToFirst()) {
            do {
                    orderTypeBrif.compute(cursor.getString(orderTypeIndex), (a,b) -> b + cursor.getInt(paymentIndex));
            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    private void setNavButtons() {
        nav_button_main = findViewById(R.id.nav_button_main);
        nav_button_month = findViewById(R.id.nav_button_month);
        nav_button_year = findViewById(R.id.nav_button_year);
        nav_button_preferences = findViewById(R.id.nav_button_preferences);
        startNavButtons(nav_button_main, MainActivity.class);
        startNavButtons(nav_button_month, CurrentMonthActivity.class);
        startNavButtons(nav_button_year, BrifActivity.class);
        startNavButtons(nav_button_preferences, PreferencesActivity.class);

    }
    private void startNavButtons (View v, Class<? extends Activity> c) {
        v.setOnClickListener(view -> {
            Intent intent = new Intent(CurrentMonthActivity.this, c);
            startActivity(intent);
        });
    }
}