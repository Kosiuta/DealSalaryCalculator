package slan.ru;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import slan.ru.models.MainPref;

public class CurrentMonthActivity extends AppCompatActivity {

    @SuppressLint("SimpleDateFormat")
    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate date;
    SimpleAdapter adapter;

    TextView deal_amount;
    ImageButton nav_button_main, nav_button_month, nav_button_year, nav_button_preferences;
    DBHelper dbHelper;


    ListView month_brif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_month);
        month_brif = findViewById(R.id.month_statistic);
        deal_amount = findViewById(R.id.deal_amount);

        dbHelper = new DBHelper(this);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        setNavButtons();
        setCurrentMonthBrif(database);

        database.close();
    }

    private void setCurrentMonthBrif(SQLiteDatabase database) {
        date = LocalDate.now();
        int sum = 0;
        String[] days = new String[2];
        days[0] = date.format(format).substring(0,8) + "01";
        days[1] = date.format(format).substring(0,8) + date.getMonth().maxLength();
        ArrayList<Map<String,String>> ordersBrifList = new ArrayList<>();
        Set<String> daysCount = new HashSet<>();
        Cursor cursor = database.query(DBHelper.TABLE_DAILY_NOTE, new String[]{DBHelper.KEY_DATE, DBHelper.KEY_ORDER_TYPE,
                DBHelper.KEY_PAYMENT},DBHelper.KEY_DATE + " BETWEEN ? AND ? ", days,null,null,null);

        if (cursor.moveToFirst()) {
            ArrayList<MainPref.PrefNote> prefNotes = MainPref.getInstance(CurrentMonthActivity.this).getOrderTypeList();
            Map<String, Integer> orderTypeBrif = new HashMap<>();
            for (MainPref.PrefNote note : prefNotes) {
                orderTypeBrif.put(note.getKey(), 0);
            }
            int dateIndex = cursor.getColumnIndex(DBHelper.KEY_DATE);
            int orderTypeIndex = cursor.getColumnIndex(DBHelper.KEY_ORDER_TYPE);
            int paymentIndex = cursor.getColumnIndex(DBHelper.KEY_PAYMENT);
            do {
                orderTypeBrif.compute(cursor.getString(orderTypeIndex), (a, b) -> b + cursor.getInt(paymentIndex));
                daysCount.add(cursor.getString(dateIndex));
            } while (cursor.moveToNext());

            Map<String, String> map = new HashMap<>();
            map.put(DBHelper.KEY_ORDER_TYPE, "Дней отработано ");
            map.put(DBHelper.KEY_PAYMENT, String.valueOf(daysCount.size()));
            ordersBrifList.add(map);
            for (Map.Entry<String, Integer> entry : orderTypeBrif.entrySet()) {
                if (entry.getValue() != 0) {
                    sum += entry.getValue();
                    map = new HashMap<>();
                    map.put(DBHelper.KEY_ORDER_TYPE, entry.getKey());
                    map.put(DBHelper.KEY_PAYMENT, entry.getValue().toString());
                    ordersBrifList.add(map);
                }
            }
        } else {
            Map<String, String> map = new HashMap<>();
            map.put(DBHelper.KEY_ORDER_TYPE, "Дней отработано ");
            map.put(DBHelper.KEY_PAYMENT, String.valueOf(daysCount.size()));
            ordersBrifList.add(map);
        }
        cursor.close();

        String[] from = {DBHelper.KEY_ORDER_TYPE, DBHelper.KEY_PAYMENT};
        int[] to = {R.id.order_list_name, R.id.order_list_price};
        adapter = new SimpleAdapter(this, ordersBrifList, R.layout.current_month_order_list_item, from, to);
        month_brif.setAdapter(adapter);
        deal_amount.setText(String.valueOf(sum));

        if (daysCount.size() > 1) {
            ContentValues value = new ContentValues();
            value.put(DBHelper.KEY_WORK_DAY, String.valueOf(daysCount.size()));
            database.update(DBHelper.TABLE_MONTHLY_NOTE, value, DBHelper.KEY_DATE + " = ?", new String[]{days[0]});
        }
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

    public void setMonthSalary(View view) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.KEY_EXPECTED_MS, MainPref.getInstance(CurrentMonthActivity.this).getOtherPreferences().get("salary"));
        database.update(DBHelper.TABLE_MONTHLY_NOTE, values, DBHelper.KEY_DATE + " = ?",
                new String[]{format.format(date).substring(0,8)+"01"});
    }
}