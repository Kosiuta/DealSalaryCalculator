package slan.ru.models;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import slan.ru.DBHelper;

public class MainPref {
    private ArrayList<PrefNote> orderTypeList;

    public MainPref ( SQLiteDatabase database) {
        Cursor cursor = database.query(DBHelper.TABLE_PREFERENCES, new String[]{DBHelper.KEY_PREF_VALUE},
                DBHelper.KEY_PREF_NAME + " = ?", new String[]{"order_type"}, null, null, null);
        if(cursor.moveToFirst()) {
            int prefValueIndex = cursor.getColumnIndex(DBHelper.KEY_PREF_VALUE);
            do {
                String value = cursor.getString(prefValueIndex);
                PrefNote note = new PrefNote(value.substring(0, value.length()-3), value.substring(value.length()-3));
                orderTypeList.add(note);
            }while (cursor.moveToNext());
        }
    }
}
