/*
 * Copyright Unitils.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitils.dataset.loader.impl;

import org.unitils.core.UnitilsException;
import org.unitils.dataset.core.DatabaseRow;
import org.unitils.dataset.core.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * @author Tim Ducheyne
 * @author Filip Neven
 */
public class UpdateDataSetLoader extends BaseDataSetLoader {


    @Override
    protected int loadDatabaseRow(DatabaseRow databaseRow) throws Exception {
        List<Value> statementValues = new ArrayList<Value>();
        String sql = buildUpdateStatement(databaseRow, statementValues);

        int nrUpdates = databaseAccessor.executeUpdate(sql, statementValues);
        if (nrUpdates == 0) {
            handleNoUpdatesPerformed();
        }
        return nrUpdates;
    }


    protected void handleNoUpdatesPerformed() {
        throw new UnitilsException("Unable to update record for data set. No record found in database with matching primary key values.");
    }

    @Override
    protected void handleUnusedPrimaryKeyColumns(Set<String> unusedPrimaryKeyColumnNames) {
        throw new UnitilsException("Unable to create update statement for data set row. Unable to determine record to update because no values for primary keys were specified. Missing primary key columns: " + unusedPrimaryKeyColumnNames);
    }

    protected void handleNoPrimaryKeyColumnFound() {
        throw new UnitilsException("Unable to create update statement for data set row. Unable to determine record to update because no values for primary keys were specified.");
    }


    protected String buildUpdateStatement(DatabaseRow databaseRow, List<Value> statementValues) {
        List<Value> columnsValues = new ArrayList<Value>();
        List<Value> whereValues = new ArrayList<Value>();
        StringBuilder columnsPart = new StringBuilder();
        StringBuilder wherePart = new StringBuilder();

        boolean primaryKeyColumnFound = false;
        for (Value databaseColumn : databaseRow.getDatabaseColumnsWithValue()) {
            if (databaseColumn.getDatabaseColumn().isPrimaryKey()) {
                primaryKeyColumnFound = true;
                addColumnToStatementPart(databaseColumn, wherePart, whereValues);
            }
            addColumnToStatementPart(databaseColumn, columnsPart, columnsValues);
        }

        if (!primaryKeyColumnFound) {
            handleNoPrimaryKeyColumnFound();
        }

        statementValues.addAll(columnsValues);
        statementValues.addAll(whereValues);
        return createStatement(databaseRow.getQualifiedTableName(), columnsPart, wherePart);
    }

    protected void addColumnToStatementPart(Value databaseColumn, StringBuilder statementPart, List<Value> parameters) {
        statementPart.append(databaseColumn.getDatabaseColumn().getColumnName());
        statementPart.append('=');

        if (databaseColumn.isLiteralValue()) {
            statementPart.append(databaseColumn.getValue());
        } else {
            statementPart.append('?');
            parameters.add(databaseColumn);
        }
        statementPart.append(", ");
    }

    protected String createStatement(String tableName, StringBuilder columnsPart, StringBuilder wherePart) {
        columnsPart.setLength(columnsPart.length() - 2);
        wherePart.setLength(wherePart.length() - 2);

        StringBuilder sql = new StringBuilder();
        sql.append("update ");
        sql.append(tableName);
        sql.append(" set ");
        sql.append(columnsPart);
        sql.append(" where ");
        sql.append(wherePart);
        return sql.toString();
    }

}