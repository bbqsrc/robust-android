package so.brendan.robust.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import so.brendan.robust.models.commands.BacklogCommand;
import so.brendan.robust.models.commands.MessageCommand;
import so.brendan.robust.utils.Constants;

/**
 * The database helper. Provides necessary convenience methods and static classes for handling
 * database content.
 *
 * Singleton; use <code>getInstance(Context context)</code>.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = Constants.createTag(DatabaseHelper.class);

    private static final String DB_NAME = Constants.APP_NAME + ".db";
    private static final int DB_VERSION = 1;

    private static DatabaseHelper sInstance;

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Handles operations related to the <code>messages</code> table.
     */
    public static final class Messages {
        public static final String TABLE_NAME = "messages";

        public static final String KEY_ID = "id";
        public static final String KEY_SUBTYPE = "subtype";
        public static final String KEY_TARGET = "target";
        public static final String KEY_TIMESTAMP = "ts";
        public static final String KEY_JSON = "json";
        public static final String KEY_USER_ID = "user_id";

        private static final String TABLE_CREATE = String.format(
                "CREATE TABLE %s (" +
                        "%s string primary key, %s string unique not null, %s text, %s text not null," +
                        "%s integer not null, %s text not null, %s string not null)",
                TABLE_NAME,
                BaseColumns._ID, KEY_ID, KEY_SUBTYPE, KEY_TARGET,
                KEY_TIMESTAMP,  KEY_JSON, KEY_USER_ID);

        /**
         * Returns a list of messages for the given target (channel or user).
         *
         * @param ctx
         * @param target
         * @return
         */
        public static List<MessageCommand> getTargetAsList(Context ctx, String target) {
            SQLiteDatabase db = getInstance(ctx).getReadableDatabase();
            ArrayList<MessageCommand> list = new ArrayList<MessageCommand>();

            Cursor cur = db.rawQuery(String.format("select %s from %s where %s=? order by %s",
                    KEY_JSON, TABLE_NAME, KEY_TARGET, KEY_TIMESTAMP), new String[] { target });

            while (cur.moveToNext()) {
                list.add(MessageCommand.fromJSON(cur.getString(0)));
            }

            return list;
        }

        /**
         * Gets the oldest timestamp found in the table for given target.
         *
         * @param ctx
         * @param target
         * @return
         */
        public static long getOldestTimestamp(Context ctx, String target) {
            SQLiteDatabase db = getInstance(ctx).getReadableDatabase();

            Cursor cursor = db.rawQuery(String.format("select min(%s) from %s where %s = ?",
                    KEY_TIMESTAMP, TABLE_NAME, KEY_TARGET), new String[] { target });

            // No records? Make it rain make it rain
            if (cursor.getCount() == 0) {
                return 0;
            }

            cursor.moveToNext();

            Log.d(TAG, String.format("Oldest timestamp: %s", cursor.getLong(0)));

            return cursor.getLong(0);
        }

        /**
         * Gets the newest timestamp found in the table for given target.
         *
         * @param ctx
         * @param target
         * @return
         */
        public static long getNewestTimestamp(Context ctx, String target) {
            SQLiteDatabase db = getInstance(ctx).getReadableDatabase();

            Cursor cursor = db.rawQuery(String.format("select max(%s) from %s where %s = ?",
                    KEY_TIMESTAMP, TABLE_NAME, KEY_TARGET), new String[] { target });

            // No records? Make it rain make it rain
            if (cursor.getCount() == 0) {
                return 0;
            }

            cursor.moveToNext();

            Log.d(TAG, String.format("Newest timestamp: %s", cursor.getLong(0)));

            return cursor.getLong(0);
        }

        private static ContentValues getContentValues(MessageCommand message) {
            ContentValues v = new ContentValues();

            v.put(KEY_ID, message.getId());
            v.put(KEY_SUBTYPE, message.getSubtype());
            v.put(KEY_TARGET, message.getTarget());
            v.put(KEY_TIMESTAMP, message.getTimestamp());
            v.put(KEY_JSON, message.toString());
            v.put(KEY_USER_ID, message.getSenderId());

            return v;
        }

        /**
         * Upserts a backlog.
         *
         * @param ctx
         * @param backlog
         * @return
         */
        public static boolean upsert(Context ctx, BacklogCommand backlog) {
            boolean flag = false;

            for (MessageCommand message : backlog.getMessages()) {
                if (upsert(ctx, message)) {
                    flag = true;
                }
            }

            return flag;
        }

        /**
         * Upserts a message.
         *
         * @param ctx
         * @param message
         * @return
         */
        public static boolean upsert(Context ctx, MessageCommand message) {
            return DatabaseHelper.upsert(ctx, TABLE_NAME, getContentValues(message),
                    KEY_ID + "=?", new String[]{ message.getId() });
        }
    }

    /**
     * Attempts to update the relevant record in a database, and if not found, inserts instead.
     *
     * @param ctx
     * @param table
     * @param values
     * @param whereClause
     * @param whereArgs
     * @return
     */
    public static boolean upsert(Context ctx, String table, ContentValues values, String whereClause, String[] whereArgs) {
        Log.v(TAG, String.format("Upserting into table '%s' values '%s' where: %s %s",
                table, values, whereClause, whereArgs == null ? null : "[" + TextUtils.join(", ", whereArgs) + "]"));
        SQLiteDatabase db = getInstance(ctx).getWritableDatabase();

        long i = db.update(table, values, whereClause, whereArgs);
        Log.v(TAG, "Update: " + i);

        if (i > 1) {
            Log.e(TAG, String.format("possible database corruption; update returned %s rows!", i));
        }

        if (i == 0) {
            try {
                i = db.insertOrThrow(table, null, values);

                if (i == -1) {
                    Log.e(TAG, "bad data provided to insert; no record created.");
                    Log.d(TAG, values.toString());

                    return false;
                }

                return true;
            } catch (SQLiteException e) {
                Log.e(TAG, "!", e);
            }
        }

        return false;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Messages.TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
