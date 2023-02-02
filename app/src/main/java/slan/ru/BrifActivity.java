package slan.ru;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import slan.ru.models.MainPref;

public class BrifActivity extends AppCompatActivity {

    ImageButton nav_button_main, nav_button_month, nav_button_year, nav_button_preferences;
    SimpleAdapter adapter;
    DBHelper dbHelper;
    Spinner monthList;

    TextView brif_expect_deal, brif_expect_salary, brif_expect_total, brif_fact_total, brif_fact_salary, brif_fact_deal;
    ListView monthBrif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brif);
        monthList = findViewById(R.id.brif_spinner);
        monthBrif = findViewById(R.id.brif_list);
        brif_expect_deal = findViewById(R.id.brif_expect_deal);
        brif_expect_salary = findViewById(R.id.brif_expect_salary);
        brif_expect_total = findViewById(R.id.brif_expect_total);
        brif_fact_salary = findViewById(R.id.brif_fact_salary);
        brif_fact_deal = findViewById(R.id.brif_fact_deal);
        brif_fact_total = findViewById(R.id.brif_fact_total);

        dbHelper = new DBHelper(this);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        setNavButtons();
        setSpinnerList(database);
        setCurrentMonthBrif(database, monthList.getSelectedItem().toString());

//        database.close();
    }

    private void setSpinnerList(SQLiteDatabase database) {
        Cursor cursor = database.query(DBHelper.TABLE_MONTHLY_NOTE, new String[]{DBHelper.KEY_DATE},null,
                null,null,null,null);
        if (cursor.moveToFirst()) {
            int i=cursor.getCount();
            String[] months = new String[i];
            int monthsIndex = cursor.getColumnIndex(DBHelper.KEY_DATE);

            do {
                i--;
                months[i] = cursor.getString(monthsIndex).substring(0,7);
            }while (cursor.moveToNext());
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            monthList.setAdapter(spinnerAdapter);
            monthList.setSelection(0);
            monthList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    setCurrentMonthBrif(dbHelper.getWritableDatabase(),months[i] + "-");
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }
        cursor.close();

    }

    private void setCurrentMonthBrif(SQLiteDatabase database, String date) {
        int sum = 0;
        String[] days = new String[2];
        days[0] = date + "01";
        days[1] = date + Month.of(Integer.parseInt(date.substring(5,7))).maxLength();
        ArrayList<Map<String,String>> ordersBrifList = new ArrayList<>();
        Set<String> daysCount = new HashSet<>();
        Cursor cursor = database.query(DBHelper.TABLE_DAILY_NOTE, new String[]{DBHelper.KEY_DATE, DBHelper.KEY_ORDER_TYPE,
                DBHelper.KEY_PAYMENT},DBHelper.KEY_DATE + " BETWEEN ? AND ? ", days,null,null,null);

        if (cursor.moveToFirst()) {
            ArrayList<MainPref.PrefNote> prefNotes = MainPref.getInstance(BrifActivity.this).getOrderTypeList();
            Map<String, Integer> orderTypeBrif = new HashMap<>();
            for (MainPref.PrefNote note : prefNotes) {
                orderTypeBrif.put(note.getKey(), 0);
            }
            int dateIndex = cursor.getColumnIndex(DBHelper.KEY_DATE);
            int orderTypeIndex = cursor.getColumnIndex(DBHelper.KEY_ORDER_TYPE);
            int paymentIndex = cursor.getColumnIndex(DBHelper.KEY_PAYMENT);
            do {
                Cursor finalCursor = cursor;
                orderTypeBrif.compute(cursor.getString(orderTypeIndex), (a, b) -> b + finalCursor.getInt(paymentIndex));
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
        monthBrif.setAdapter(adapter);
        brif_expect_deal.setText(String.valueOf(sum));

        if (daysCount.size() > 1) {
            ContentValues value = new ContentValues();
            value.put(DBHelper.KEY_WORK_DAY, String.valueOf(daysCount.size()));
            database.update(DBHelper.TABLE_MONTHLY_NOTE, value, DBHelper.KEY_DATE + " = ?", new String[]{days[0]});
        }

        cursor = database.query(DBHelper.TABLE_MONTHLY_NOTE, new String[]{DBHelper.KEY_EXPECTED_MS, DBHelper.KEY_FACT_MS,
                DBHelper.KEY_FACT_MD},DBHelper.KEY_DATE + " = ?", new String[]{days[0]},null,null,null);
        int expSalary = 0;
        if (cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(DBHelper.KEY_EXPECTED_MS);
            expSalary = Integer.parseInt(cursor.getString(index));
            expSalary =(int) (expSalary - expSalary*
                (Double.parseDouble(MainPref.getInstance(BrifActivity.this).getOtherPreferences().get("tax")) / 100));
            brif_expect_salary.setText(String.valueOf(expSalary));

            String factSalary, factDeal;
            int factSalaryIndex = cursor.getColumnIndex(DBHelper.KEY_FACT_MS);
            int factDealIndex = cursor.getColumnIndex(DBHelper.KEY_FACT_MD);
            factSalary = cursor.getString(factSalaryIndex);
            factDeal = cursor.getString(factDealIndex);
            brif_fact_salary.setText(factSalary);
            brif_fact_deal.setText(factDeal);
            setFactTotal();
        }
        cursor.close();

        // ?
        database.close();
        //

        brif_expect_total.setText(String.valueOf(expSalary + sum));
        setFactTotal();
    }

    public void setFactAmount (View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
          builder.setTitle("Фактический доход");
        builder.setMessage("Введите сколько реально получили на руки");
        LayoutInflater inflater = LayoutInflater.from(this);
        View addWindow = inflater.inflate(R.layout.add_pref_window, null);
        EditText factSalary = addWindow.findViewById(R.id.add_window_order_type);
        EditText factDeal = addWindow.findViewById(R.id.add_window_price);

        factSalary.setInputType(InputType.TYPE_CLASS_NUMBER);
        factSalary.setHint("Фактический оклад");
        factDeal.setHint("Фактическая сделка");
        factSalary.setText(brif_fact_salary.getText().toString());
        factDeal.setText(brif_fact_deal.getText().toString());

        builder.setView(addWindow);

        builder.setNegativeButton("Отменить", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.setPositiveButton("Установить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String salary = factSalary.getText().toString();
                String deal = factDeal.getText().toString();

                SQLiteDatabase database = dbHelper.getWritableDatabase();
                ContentValues value = new ContentValues();
                value.put(DBHelper.KEY_FACT_MS, salary);
                value.put(DBHelper.KEY_FACT_MD, deal);
                String date = monthList.getSelectedItem().toString()+"-01";
                database.update(DBHelper.TABLE_MONTHLY_NOTE, value, DBHelper.KEY_DATE + " = ?",
                         new String[]{date});
                database.close();
                brif_fact_salary.setText(salary);
                brif_fact_deal.setText(deal);

                Toast toast = Toast.makeText(BrifActivity.this,"Данные на "+ date.substring(0,7) +" установлены" , Toast.LENGTH_SHORT);
                toast.show();
                setFactTotal();
            }
        });

        builder.show();
    }

    private void setFactTotal() {
        String factSalary = TextUtils.isEmpty(brif_fact_salary.getText().toString()) ? "0" : brif_fact_salary.getText().toString();
        String factDeal = TextUtils.isEmpty(brif_fact_deal.getText().toString()) ? "0" : brif_fact_deal.getText().toString();
        String total = String.valueOf(Integer.parseInt(factSalary) + Integer.parseInt(factDeal));
        brif_fact_total.setText(total);
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
            Intent intent = new Intent(BrifActivity.this, c);
            startActivity(intent);
        });
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
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
//                Intent intent = new Intent(BrifActivity.this, PreferencesActivity.class);
//                startActivity(intent);
//            } else if ((e2.getX() - e1.getX()) > sensitvity) {
//                Intent intent = new Intent(BrifActivity.this, CurrentMonthActivity.class);
//                startActivity(intent);
//            }
//            return true;
//        }
//    };
//    GestureDetector gestureDetector = new GestureDetector(getBaseContext(), simpleOnGestureListener);
}