package slan.ru;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import java.util.Comparator;
import java.util.HashMap;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import slan.ru.models.MainPref;

public class CurrentMonthActivity extends AppCompatActivity {

    @SuppressLint("SimpleDateFormat")
    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    Set<String> daysCount;
    ArrayList<Map<String, String>> currentListData;
    ArrayList<String> ids;
    Map<String, String> listItemData;
    LocalDate date;
    SimpleAdapter adapter;
    Spinner daysSpinner;

    TextView deal_amount, current_day_total;
    ImageButton nav_button_main, nav_button_month, nav_button_year, nav_button_preferences;
    DBHelper dbHelper;

    ListView month_brif, current_day_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_month);
        month_brif = findViewById(R.id.month_statistic);
        deal_amount = findViewById(R.id.deal_amount);
        daysSpinner = findViewById(R.id.days_spinner);
        current_day_list = findViewById(R.id.current_day_table);

        current_day_total = findViewById(R.id.current_day_total);

        dbHelper = new DBHelper(this);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        setNavButtons();
        setCurrentMonthBrif(database);

        setSpinnerList();

        database.close();
    }


    private void setSpinnerList() {

        if (daysCount.size() > 0) {

            String[] days = daysCount.toArray(new String[0]);

            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            daysSpinner.setAdapter(spinnerAdapter);
            daysSpinner.setSelection(0);
            daysSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    setCurrentDayList(dbHelper.getReadableDatabase(), days[i]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }

    }

    private void setCurrentDayList(SQLiteDatabase database, String day) {
        Cursor cursor = database.query(DBHelper.TABLE_DAILY_NOTE, new String[] {
                        DBHelper.KEY_ID, DBHelper.KEY_ORDER_TYPE, DBHelper.KEY_PAYMENT},
                DBHelper.KEY_DATE + " = ?",new String[]{day},null,null,null);
        int sum = 0;
        if (cursor.moveToFirst()) {
            int orderIndex = cursor.getColumnIndex(DBHelper.KEY_ORDER_TYPE);
            int payIndex = cursor.getColumnIndex(DBHelper.KEY_PAYMENT);
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
            currentListData = new ArrayList<>();
            ids = new ArrayList<>();
            do {
                listItemData = new HashMap<>();
                int money = cursor.getInt(payIndex);
                listItemData.put(DBHelper.KEY_ORDER_TYPE, cursor.getString(orderIndex));
                listItemData.put(DBHelper.KEY_PAYMENT, String.valueOf(money));
                currentListData.add(listItemData);
                ids.add(cursor.getString(idIndex));
                sum += money;
            }while (cursor.moveToNext());
        }

        current_day_total.setText(String.valueOf(sum));
        String[] from = {DBHelper.KEY_ORDER_TYPE, DBHelper.KEY_PAYMENT};
        int[] to = {R.id.order_list_name, R.id.order_list_price};

        adapter = new SimpleAdapter(this, currentListData, R.layout.current_month_order_list_item, from, to);
        current_day_list.setAdapter(adapter);
        registerForContextMenu(current_day_list);

        cursor.close();
        database.close();
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0,1,0, R.string.delete_note);
    }

    public boolean onContextItemSelected (MenuItem item) {
        if (item.getItemId() == 1) {
            AdapterView.AdapterContextMenuInfo acmi =(AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            database.delete(DBHelper.TABLE_DAILY_NOTE, DBHelper.KEY_ID + " = " + ids.get((int)acmi.id), null);
            Toast toast = Toast.makeText(this,"Данные удалены. Запись " + ids.get((int)acmi.id), Toast.LENGTH_LONG);
            toast.show();
            currentListData.remove((int) acmi.id);
            ids.remove((int) acmi.id);
            adapter.notifyDataSetChanged();
            database.close();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void setCurrentMonthBrif(SQLiteDatabase database) {
        date = LocalDate.now();
        int sum = 0;
        String[] days = new String[2];
        days[0] = date.format(format).substring(0,8) + "01";
        days[1] = date.format(format).substring(0,8) + date.getMonth().maxLength();
        ArrayList<Map<String,String>> ordersBrifList = new ArrayList<>();
        daysCount = new TreeSet<>(Comparator.reverseOrder());
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

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//
//        return gestureDetector.onTouchEvent(event);
//    }
//    GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
//
//        @Override
//        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
//                               float velocityY) {
//
//            float sensitvity = 50;
//            if ((e1.getX() - e2.getX()) > sensitvity) {
//                Intent intent = new Intent(CurrentMonthActivity.this, BrifActivity.class);
//                startActivity(intent);
//            } else if ((e2.getX() - e1.getX()) > sensitvity) {
//                Intent intent = new Intent(CurrentMonthActivity.this, MainActivity.class);
//                startActivity(intent);
//            }
//            return true;
//        }
//    };
//    GestureDetector gestureDetector = new GestureDetector(getBaseContext(), simpleOnGestureListener);
}