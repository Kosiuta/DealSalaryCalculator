package slan.ru;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final int DATAbASE_VERSION = 1;
    public static final String DATABASE_NAME = "coreDb";
    public static final String TABLE_MONTHLY_NOTE = "monthly_note";
    public static final String TABLE_DAILY_NOTE = "daily_note";
    public static final String TABLE_PREFERENCES = "pref";

    public static final String KEY_ID = "_id";
    public static final String KEY_DATE = "date";
    public static final String KEY_ORDER_TYPE = "order_type";
    public static final String KEY_COEF = "coefficient";
    public static final String KEY_PAYMENT = "payment";
    public static final String KEY_COMMENT = "comment";

    public static final String KEY_WORK_DAY = "days_in_work";
    public static final String KEY_OFF_DAY = "days_off";
    public static final String KEY_EXPECTED_MS = "expected_month_salary";
    public static final String KEY_EXPECTED_MD = "expected_month_deal";
    public static final String KEY_FACT_MS = "fact_month_salary";
    public static final String KEY_FACT_MD = "fact_month_deal";

    public static final String KEY_PREF_NAME = "pref_name";
    public static final String KEY_PREF_VALUE = "pref_value";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME,null, DATAbASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE " + TABLE_DAILY_NOTE + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_DATE + " DATE, " +
                KEY_ORDER_TYPE + " TEXT, " +
                KEY_COEF + " TEXT, " +
                KEY_PAYMENT + " INTEGER, " +
                KEY_COMMENT + " TEXT" + ")");
        database.execSQL("CREATE TABLE " + TABLE_MONTHLY_NOTE + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_DATE + " DATE, " +
                KEY_WORK_DAY + " TEXT, " +
                KEY_OFF_DAY + " TEXT, " +
                KEY_EXPECTED_MS + " TEXT, " +
                KEY_EXPECTED_MD + " TEXT, " +
                KEY_FACT_MS + " TEXT, " +
                KEY_FACT_MD + " TEXT" + ")");
        database.execSQL("CREATE TABLE " + TABLE_PREFERENCES + "(" +
                KEY_PREF_NAME + " TEXT, " +
                KEY_PREF_VALUE + " TEXT" + ")");

        database.execSQL("INSERT INTO " + TABLE_PREFERENCES + " VALUES " +
                "(tax, 0.13), "+
                "(salary, 15300)," +
                "(foreman, 0.00)," +
                "(order_type, 'Подключение (оптика)500')," +
                "(order_type, 'Подключение (радио)350')," +
                "(order_type, 'Подключение (медь)350')," +
                "(order_type, 'Дорога300')," +
                "(order_type, 'Стройка линии007')," +
                "(order_type, 'Монтаж муфты/бокса250')," +
                "(order_type, 'Сварка волокна050')," +
                "(order_type, 'Монтаж ТКД750')," +
                "(order_type, 'Выезд (бесплатный)100')," +
                "(order_type, 'Выезд (платный)125')," +
                "(order_type, 'Конфиг дэвайса в аренде050')," +
                "(order_type, 'Конфиг дэвайса абонента125')," +
                "(order_type, 'Продажа/прокл. кабеля025')," +
                "(order_type, 'Ремонт у абонента125')," +
                "(order_type, 'Простой ремонт150')," +
                "(order_type, 'Сложный ремонт250')," +
                "(order_type, 'Сложный ремонт (оптика)400')"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_DAILY_NOTE);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_MONTHLY_NOTE);
        onCreate(database);
    }
}
