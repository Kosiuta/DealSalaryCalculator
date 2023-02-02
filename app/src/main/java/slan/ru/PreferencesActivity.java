package slan.ru;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import slan.ru.models.MainPref;

public class PreferencesActivity extends AppCompatActivity {

    DBHelper dbHelper;
    MainPref pref;
    ArrayAdapter<String> adapter;
    ArrayList<String> orderListData;

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
        orderListData = new ArrayList<>();
        for (MainPref.PrefNote note : list) {
            orderListData.add(note.key + "  ( " + note.value + " руб ) ");
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, orderListData);
        prefList.setAdapter(adapter);
        registerForContextMenu(prefList);
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
                orderListData.add(order + "  ( " + price + " руб ) ");
                adapter.notifyDataSetChanged();

            }
        });

        builder.show();
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0,2,0, R.string.delete_note);
    }

    public boolean onContextItemSelected (MenuItem item) {
        if (item.getItemId() == 2) {
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            TextView view = (TextView) acmi.targetView;
            List<MainPref.PrefNote> list = pref.getOrderTypeList();
            String[] targets = view.getText().toString().split("[(]",2);
            targets[1] = targets[1].replaceAll("\\D", "");
            while (targets[1].length() < 3) targets[1] = "0" + targets[1];
            String target = targets[0].trim() + targets[1];
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            database.delete(DBHelper.TABLE_PREFERENCES, DBHelper.KEY_PREF_VALUE + " = '" + target + "'", null);
            Toast toast = Toast.makeText(this, "Данные удалены. Запись " + view.getText().toString(), Toast.LENGTH_SHORT);
            toast.show();
            list.remove(acmi.position);
            orderListData.remove(acmi.position);
            adapter.notifyDataSetChanged();
            database.close();

            return true;
        }
        return super.onContextItemSelected(item);
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
//                Intent intent = new Intent(PreferencesActivity.this, MainActivity.class);
//                startActivity(intent);
//            } else if ((e2.getX() - e1.getX()) > sensitvity) {
//                Intent intent = new Intent(PreferencesActivity.this, BrifActivity.class);
//                startActivity(intent);
//            }
//            return true;
//        }
//    };
//    GestureDetector gestureDetector = new GestureDetector(getBaseContext(), simpleOnGestureListener);

}