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
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


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
    MultiAutoCompleteTextView main_order_set;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setNavButtons();
        setDailyEt();
        dailyListData = new ArrayList<>();

        dbHelper = new DBHelper(this);
        setDailyList(dbHelper.getReadableDatabase());

    }

    public void add(View view) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String order = main_order_set.getText().toString();
        String money = main_price.getText().toString();
        String comment =  main_comment.getText().toString();
        values.put(DBHelper.KEY_DATE, format.format(date));
        values.put(DBHelper.KEY_ORDER_TYPE, order);
        values.put(DBHelper.KEY_COEF, main_coeff.getText().toString());
        values.put(DBHelper.KEY_PAYMENT, money);
        values.put(DBHelper.KEY_COMMENT, comment);

        database.insert(DBHelper.TABLE_DAILY_NOTE, null, values);
        listItemData =  new HashMap<>();
        listItemData.put(DBHelper.KEY_ORDER_TYPE, order);
        listItemData.put(DBHelper.KEY_PAYMENT, money);
        listItemData.put(DBHelper.KEY_COMMENT, comment);
        dailyListData.add(listItemData);
        adapter.notifyDataSetChanged();
        main_daily_list.getLayoutParams().height += (rowHeight + 2);
        Toast toast = Toast.makeText(this,"Данные добавлены", Toast.LENGTH_SHORT);
        toast.show();

        main_day_total.setText(String.valueOf(Integer.parseInt(main_day_total.getText().toString()) + Integer.parseInt(money)));


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

        Toast toast = Toast.makeText(this,"высота: " + rowHeight, Toast.LENGTH_SHORT);
        toast.show();
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
        main_price.setText(getResources().getString(R.string.app_name));
        rowHeight = main_daily_list.getLayoutParams().height + 3;
    }

    private void setNavButtons() {
        nav_button_main = findViewById(R.id.nav_button_main);
        nav_button_month = findViewById(R.id.nav_button_month);
        nav_button_year = findViewById(R.id.nav_button_year);
        nav_button_preferences = findViewById(R.id.nav_button_preferences);
        startNavButtons(nav_button_main, MainActivity.class);
        startNavButtons(nav_button_month, CurrentMonthActivity.class);
        startNavButtons(nav_button_year, MainActivity.class);
        startNavButtons(nav_button_preferences, MainActivity.class);

    }
    private void startNavButtons (View v, Class<? extends Activity> c) {
        v.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, c);
            startActivity(intent);
        });

    }

}