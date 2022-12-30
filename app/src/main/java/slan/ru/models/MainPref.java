package slan.ru.models;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import slan.ru.DBHelper;

public class MainPref {
    private ArrayList<PrefNote> orderTypeList;
    private ArrayList<PrefNote> otherPreferences;

    public MainPref ( SQLiteDatabase database) {
        orderTypeList = new ArrayList<>();
        otherPreferences = new ArrayList<>();
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
                PrefNote note = new PrefNote(cursor.getString(prefNameIndex), cursor.getString(prefValueIndex));
                otherPreferences.add(note);
            }while (cursor.moveToNext());
        }

        cursor.close();
        database.close();
    }

    public ArrayList<PrefNote> getOrderTypeList() {
        return orderTypeList;
    }
    public ArrayList<PrefNote> getOtherPreferences() {
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
