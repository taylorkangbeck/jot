package com.taylorandtucker.jot.localdb;

import android.provider.BaseColumns;

/**
 * Created by Taylor on 9/16/2015.
 */
public final class EntitiesContract {

    public EntitiesContract() {}

    public static abstract class ContractEntities implements BaseColumns {
        public static final String TABLE_NAME = "entities";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_IMPORTANCE = "importance";
        public static final String COLUMN_SENTIMENT = "sentiment";

    }
}
