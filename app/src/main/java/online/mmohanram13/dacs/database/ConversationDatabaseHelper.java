package online.mmohanram13.dacs.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import online.mmohanram13.dacs.database.model.ConversationDB;
/**
 * Created by Mohan Ram M on 5/10/2018.
 */

public class ConversationDatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "dacs_db";

    public ConversationDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ConversationDB.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ConversationDB.TABLE_NAME);
    }

    public long insertConverseData(String sender, String recipient, String data){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"));
        Date currentTimeStamp = cal.getTime();
        DateFormat time = new SimpleDateFormat("h:mm a", Locale.getDefault());
        time.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
        String localTime = time.format(currentTimeStamp);
        values.put(ConversationDB.COLUMN_SENDER,sender);
        values.put(ConversationDB.COLUMN_RECIPIENT,recipient);
        values.put(ConversationDB.COLUMN_DISPLAYED,0);
        values.put(ConversationDB.COLUMN_DATA,data);
        values.put(ConversationDB.COLUMN_TIMESTAMP,localTime);
        long id = db.insert(ConversationDB.TABLE_NAME,null,values);
        db.close();
        return id;
    }

    public List<ConversationDB> getAllUnreadData(String endpoint){
        List<ConversationDB> converse_data = new ArrayList<>();

        String select_query = "SELECT * FROM " + ConversationDB.TABLE_NAME + " WHERE " + ConversationDB.COLUMN_DISPLAYED + " = 0 and " + ConversationDB.COLUMN_SENDER + "= \"" + endpoint + "\" ORDER BY " + ConversationDB.COLUMN_ID + " ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(select_query, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do{
                ConversationDB conversationDB = new ConversationDB();
                conversationDB.setId(cursor.getInt(cursor.getColumnIndex(ConversationDB.COLUMN_ID)));
                conversationDB.setSender(cursor.getString(cursor.getColumnIndex(ConversationDB.COLUMN_SENDER)));
                conversationDB.setRecipient(cursor.getString(cursor.getColumnIndex(ConversationDB.COLUMN_RECIPIENT)));
                conversationDB.setData(cursor.getString(cursor.getColumnIndex(ConversationDB.COLUMN_DATA)));
                conversationDB.setViewed(cursor.getInt(cursor.getColumnIndex(ConversationDB.COLUMN_DISPLAYED)));
                conversationDB.setTimestamp(cursor.getString(cursor.getColumnIndex(ConversationDB.COLUMN_TIMESTAMP)));
                converse_data.add(conversationDB);
            }while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return converse_data;
    }

    public int updateData(ConversationDB conversationDB){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ConversationDB.COLUMN_DISPLAYED,1);

        return db.update(ConversationDB.TABLE_NAME, values, ConversationDB.COLUMN_ID + " =? ", new String[]{String.valueOf(conversationDB.getId())});
    }
}
