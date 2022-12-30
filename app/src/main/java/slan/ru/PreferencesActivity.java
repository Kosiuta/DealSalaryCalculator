package slan.ru;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.List;

import slan.ru.models.MainPref;

public class PreferencesActivity extends AppCompatActivity {

    DBHelper dbHelper;

    ImageButton nav_button_main, nav_button_month, nav_button_year, nav_button_preferences;
    ListView prefList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferenses);
        prefList = findViewById(R.id.pref_list);

        dbHelper = new DBHelper(this);
        setNavButtons();
        setPref(dbHelper.getReadableDatabase());

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

    }

    private void setPref (SQLiteDatabase database) {
        MainPref pref = new MainPref(database);
        List<MainPref.PrefNote> list = pref.getOrderTypeList();
        String[] m =(String[]) list.stream().map(MainPref.PrefNote::getKey).toArray(i -> new String[list.size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, m);

        prefList.setAdapter(adapter);
    }
}