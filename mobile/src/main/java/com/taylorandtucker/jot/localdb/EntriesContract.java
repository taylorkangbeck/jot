package com.taylorandtucker.jot.localdb;

import android.provider.BaseColumns;

/**
 * Created by Taylor on 9/16/2015.
 */
public final class EntriesContract {

    public EntriesContract() {}

    public static abstract class Contract implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_BODY = "body";
    }
}
