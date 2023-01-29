package slan.ru;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import slan.ru.models.MainPref;

public class PreferencesActivity extends AppCompatActivity {

    DBHelper dbHelper;
    MainPref pref;
    ArrayAdapter<String> adapter;

    ImageButton nav_button_main, nav_button_month, nav_button_year, nav_button_preferences;
    ListView prefList;
    EditText pref_salary, pref_tax, pref_foreman;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferenses);
        prefList = findViewById(R.id.pref_list);
        pref_salary = findViewById(R.id.pref_salary);
        pref_tax = findViewById(R.id.pref_tax);
        pref_foreman = findViewById(R.id.pref_foreman);


        dbHelper = new DBHelper(this);
        setNavButtons();
        setPref();

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
            Intent intent = new Intent(PreferencesActivity.this, c);
            startActivity(intent);
        });
    }

    public void save(View view) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues value = new ContentValues();
        pref = MainPref.getInstance(PreferencesActivity.this);
        Map<String,String> map = pref.getOtherPreferences();
        String v = pref_salary.getText().toString();
        map.put("salary", v);
        value.put(DBHelper.KEY_PREF_VALUE, v);
        database.update(DBHelper.TABLE_PREFERENCES, value, DBHelper.KEY_PREF_NAME + " = 'salary'", null);
        value = new ContentValues();
        v = pref_tax.getText().toString();
        map.put("tax", v);
        value.put(DBHelper.KEY_PREF_VALUE, v);
        database.update(DBHelper.TABLE_PREFERENCES, value, DBHelper.KEY_PREF_NAME + " = 'tax'", null);
        value = new ContentValues();
        v = pref_foreman.getText().toString();
        map.put("foreman", v);
        value.put(DBHelper.KEY_PREF_VALUE, v);
        database.update(DBHelper.TABLE_PREFERENCES, value, DBHelper.KEY_PREF_NAME + " = 'foreman'", null);
        Toast toast = Toast.makeText(this,"Данные добавлены", Toast.LENGTH_SHORT);
        toast.show();
        database.close();
        setOtherPref();

    }

    private void setOtherPref() {
        Map<String,String> map = pref.getOtherPreferences();
        pref_salary.setText(map.get("salary"));
        pref_tax.setText(map.get("tax"));
        pref_foreman.setText(map.get("foreman"));
    }

    private void setPref () {
        pref = MainPref.getInstance(PreferencesActivity.this);
        List<MainPref.PrefNote> list = pref.getOrderTypeList();
        String[] m = new String[list.size()];
        for (int i = 0; i < m.length; i++) {
            m[i] = list.get(i).key + "  ( " + list.get(i).value + " руб ) ";
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, m);
        prefList.setAdapter(adapter);

        setOtherPref();
    }

    public void addPref (View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавление");
        builder.setMessage("Введите название работы и стандартную стоимость");
        LayoutInflater inflater = LayoutInflater.from(this);
        View addWindow = inflater.inflate(R.layout.add_pref_window, null);
        builder.setView(addWindow);

        EditText orderType = addWindow.findViewById(R.id.add_window_order_type);
        EditText priceDefault = addWindow.findViewById(R.id.add_window_price);

        builder.setNegativeButton("Отменить", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String order = orderType.getText().toString();
                if (TextUtils.isEmpty(order)) {
                    Toast toast = Toast.makeText(PreferencesActivity.this,"Введите название задания", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                String price = priceDefault.getText().toString();
                while (price.length() < 3) price = "0" + price;
                SQLiteDatabase database = dbHelper.getWritableDatabase();
                ContentValues value = new ContentValues();
                value.put(DBHelper.KEY_PREF_NAME, "order_type");
                value.put(DBHelper.KEY_PREF_VALUE, order+price);
                database.insert(DBHelper.TABLE_PREFERENCES, null, value);
                database.close();
                ArrayList<MainPref.PrefNote> list = pref.getOrderTypeList();
                list.add(new MainPref.PrefNote(order,price));
                Toast toast = Toast.makeText(PreferencesActivity.this,"Данные добавлены", Toast.LENGTH_SHORT);
                toast.show();
                adapter.add(order + "  ( " + price + " руб ) ");
                adapter.notifyDataSetChanged();

            }
        });

        builder.show();
    }
}