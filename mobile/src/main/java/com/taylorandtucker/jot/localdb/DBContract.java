package com.taylorandtucker.jot.localdb;

import android.provider.BaseColumns;

/**
 * Created by Taylor on 9/16/2015.
 */
public final class DBContract {

    public DBContract() {}

    public static abstract class EntryContract implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_BODY = "body";
        public static final String COLUMN_SENTIMENT = "sentimentEntry";
        public static final String COLUMN_ENTRY_NUM = "entryNum";

    }

    public static abstract class EntityContract implements BaseColumns {
        public static final String TABLE_NAME = "entity";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_IMPORTANCE = "importance";
        public static final String COLUMN_SENTIMENT = "sentimentEntity";

    }

    public static abstract class EtoEContract implements BaseColumns {
        public static final String TABLE_NAME = "entitiesToEntries";
        public static final String COLUMN_ENTITY_ID = "entityID";
        public static final String COLUMN_ENTRY_ID = "entryID";
        public static final String COLUMN_SENTIMENT = "sentimentEntity";

    }

}
