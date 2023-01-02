package slan.ru;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import slan.ru.models.MainPref;


public class MainActivity extends AppCompatActivity{



    @SuppressLint("SimpleDateFormat")
    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate date;
    DBHelper dbHelper;
    SimpleAdapter adapter;
    ArrayList<Map<String, String>> dailyListData;
    Map<String, String> listItemData;
    int rowHeight;

    ImageButton nav_button_main, nav_button_month, nav_button_year, nav_button_preferences;
    ListView main_daily_list;

    EditText main_today_date, main_coeff, main_price, main_comment;
    TextView main_day_total;
    Spinner main_order_set;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DBHelper(this);
        dailyListData = new ArrayList<>();

        setNavButtons();
        setDailyEt();
        setDailyList(dbHelper.getReadableDatabase());

    }

    public void add(View view) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        String order = main_order_set.getSelectedItem().toString();

        String money = main_price.getText().toString();
        String comment =  main_comment.getText().toString();
      //  values.put(DBHelper.KEY_DATE, format.format(date));
        values.put(DBHelper.KEY_DATE, main_today_date.getText().toString());
        values.put(DBHelper.KEY_ORDER_TYPE, order);
        values.put(DBHelper.KEY_COEF, main_coeff.getText().toString());
        values.put(DBHelper.KEY_PAYMENT, money);
        values.put(DBHelper.KEY_COMMENT, comment);


        database.insert(DBHelper.TABLE_DAILY_NOTE, null, values);
        listItemData = new HashMap<>();
        listItemData.put(DBHelper.KEY_ORDER_TYPE, order);
        listItemData.put(DBHelper.KEY_PAYMENT, money);
        listItemData.put(DBHelper.KEY_COMMENT, comment);
        dailyListData.add(listItemData);
        adapter.notifyDataSetChanged();
        main_daily_list.getLayoutParams().height += (rowHeight + 2);
        Toast toast = Toast.makeText(this,"Данные добавлены", Toast.LENGTH_SHORT);
        toast.show();
        main_comment.setText("");

        main_day_total.setText(String.valueOf(Integer.parseInt(main_day_total.getText().toString()) + Integer.parseInt(money)));

        String monthDate = format.format(date).substring(0,8) + "01";
        Cursor cursor = database.query(DBHelper.TABLE_MONTHLY_NOTE,null, DBHelper.KEY_DATE + " = ? ", new String[]{monthDate},null,null,null);
        if (cursor.moveToFirst()) {
            int expectedDealIndex = cursor.getColumnIndex(DBHelper.KEY_EXPECTED_MD);
            int expectedDeal = cursor.getInt(expectedDealIndex);
            ContentValues value = new ContentValues();
            value.put(DBHelper.KEY_EXPECTED_MD, String.valueOf(expectedDeal + Integer.parseInt(money)));
            database.update(DBHelper.TABLE_MONTHLY_NOTE, value, DBHelper.KEY_DATE + " = ?", new String[]{monthDate});
            cursor.close();
        } else {
            cursor.close();
            createMonthNote(database, monthDate, money);
        }

        database.close();
    }

    private void createMonthNote(SQLiteDatabase database, String monthDate, String money) {

        MainPref pref = MainPref.getInstance(MainActivity.this);


        ContentValues values = new ContentValues();
        values.put(DBHelper.KEY_DATE, monthDate);
        values.put(DBHelper.KEY_WORK_DAY, "1");
        values.put(DBHelper.KEY_EXPECTED_MS, pref.getOtherPreferences().get("salary"));
        values.put(DBHelper.KEY_EXPECTED_MD, money);
        values.put(DBHelper.KEY_FACT_MS, "0");
        values.put(DBHelper.KEY_FACT_MD, "0");
        database.insert(DBHelper.TABLE_MONTHLY_NOTE, null, values);

    }

    private void setDailyList(SQLiteDatabase database) {
        Cursor cursor = database.query(DBHelper.TABLE_DAILY_NOTE, new String[] {
                DBHelper.KEY_ID, DBHelper.KEY_ORDER_TYPE, DBHelper.KEY_PAYMENT, DBHelper.KEY_COMMENT},
                DBHelper.KEY_DATE + " = ?",new String[]{format.format(date)},null,null,null);
        int sum = 0;
        if (cursor.moveToFirst()) {
            int orderIndex = cursor.getColumnIndex(DBHelper.KEY_ORDER_TYPE);
            int payIndex = cursor.getColumnIndex(DBHelper.KEY_PAYMENT);
            int commentIndex = cursor.getColumnIndex(DBHelper.KEY_COMMENT);
            do {
                listItemData = new HashMap<>();
                int money = cursor.getInt(payIndex);
                listItemData.put(DBHelper.KEY_ORDER_TYPE, cursor.getString(orderIndex));
                listItemData.put(DBHelper.KEY_PAYMENT, String.valueOf(money));
                listItemData.put(DBHelper.KEY_COMMENT, cursor.getString(commentIndex));
                dailyListData.add(listItemData);
                sum += money;
            }while (cursor.moveToNext());
        }

        main_day_total.setText(String.valueOf(sum));
        String[] from = {DBHelper.KEY_ORDER_TYPE, DBHelper.KEY_PAYMENT, DBHelper.KEY_COMMENT};
        int[] to = {R.id.daily_item_type, R.id.daily_item_price, R.id.daily_item_comment};

        adapter = new SimpleAdapter(this, dailyListData, R.layout.daily_item, from, to);
        main_daily_list.setAdapter(adapter);

        ViewGroup.LayoutParams params = main_daily_list.getLayoutParams();
        params.height = cursor.getCount() * rowHeight;

        cursor.close();
        database.close();
    }

    private void setDailyEt() {
        main_daily_list = findViewById(R.id.main_day_table);
        main_today_date = findViewById(R.id.main_today_date);
        main_coeff = findViewById(R.id.main_coeff);
        main_price = findViewById(R.id.main_price);
        main_comment = findViewById(R.id.main_comment);
        main_day_total = findViewById(R.id.main_day_total);
        main_order_set = findViewById(R.id.main_order_set);

        date = LocalDate.now();
        main_today_date.setText(format.format(date));
        main_coeff.setText("1/2");
        rowHeight = main_daily_list.getLayoutParams().height + 3;

        MainPref orderSet = MainPref.getInstance(MainActivity.this);
        ArrayList<MainPref.PrefNote> list = orderSet.getOrderTypeList();
        String[] m = list.stream().map(MainPref.PrefNote::getKey).toArray(i -> new String[list.size()]);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, m);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        main_order_set.setAdapter(spinnerAdapter);
        main_order_set.setSelection(0);
        main_order_set.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                main_price.setText(list.get(i).value);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

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
            Intent intent = new Intent(MainActivity.this, c);
            startActivity(intent);
        });
    }

    @Override
    public boolean dispatchTouchEvent( MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                v.clearFocus();
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            }
        }

        return super.dispatchTouchEvent(event);
    }

}