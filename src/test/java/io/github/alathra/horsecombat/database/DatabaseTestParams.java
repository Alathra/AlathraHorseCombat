package io.github.alathra.horsecombat.database;

import io.github.alathra.horsecombat.database.handler.DatabaseType;

record DatabaseTestParams(String jdbcPrefix, DatabaseType requiredDatabaseType, String tablePrefix) {
}
