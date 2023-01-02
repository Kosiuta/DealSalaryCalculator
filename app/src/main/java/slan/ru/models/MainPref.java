package slan.ru.models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import slan.ru.DBHelper;

public class MainPref {
    private static MainPref instance;

    private ArrayList<PrefNote> orderTypeList;
    private Map<String,String> otherPreferences;

    public static synchronized MainPref getInstance(Context context) {
        if (instance == null) {
            instance = new MainPref(context);
        }
        return instance;
    }

    private MainPref (Context context) {
        SQLiteDatabase database = new DBHelper(context).getWritableDatabase();
        orderTypeList = new ArrayList<>();
        otherPreferences = new HashMap<>();
        Cursor cursor = database.query(DBHelper.TABLE_PREFERENCES, new String[]{DBHelper.KEY_PREF_VALUE},
                DBHelper.KEY_PREF_NAME + " = ?", new String[]{"order_type"}, null, null, null);
        if(cursor.moveToFirst()) {
            int prefValueIndex = cursor.getColumnIndex(DBHelper.KEY_PREF_VALUE);
            do {
                String value = cursor.getString(prefValueIndex);
                PrefNote note = new PrefNote(value.substring(0, value.length() - 3), value.substring(value.length() - 3));
                orderTypeList.add(note);
            }while (cursor.moveToNext());
        }
        cursor.close();
        cursor = database.query(DBHelper.TABLE_PREFERENCES, new String[]{DBHelper.KEY_PREF_NAME, DBHelper.KEY_PREF_VALUE},
                DBHelper.KEY_PREF_NAME + " != ?", new String[]{"order_type"}, null, null, null);

        if(cursor.moveToFirst()) {
            int prefNameIndex = cursor.getColumnIndex(DBHelper.KEY_PREF_NAME);
            int prefValueIndex = cursor.getColumnIndex(DBHelper.KEY_PREF_VALUE);
            do {
                otherPreferences.put(cursor.getString(prefNameIndex), cursor.getString(prefValueIndex));
            }while (cursor.moveToNext());
        }

        cursor.close();
        database.close();
    }

    public ArrayList<PrefNote> getOrderTypeList() {
        return orderTypeList;
    }
    public Map<String, String> getOtherPreferences() {
        return otherPreferences;
    }

    public static class PrefNote {
        public String key;
        public String value;

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public PrefNote(String key, String value) {
            this.key = key;
            this.value = value.replaceAll("^0+", "");
        }

        @Override
        public String toString() {
            return key;
        }
    }
}
