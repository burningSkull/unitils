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
package org.unitils.dataset.core;

import org.unitils.core.UnitilsException;
import org.unitils.dataset.factory.DataSetRowSource;
import org.unitils.dataset.loader.impl.Database;
import org.unitils.dataset.loader.impl.IdentifierNameProcessor;
import org.unitils.dataset.loader.impl.TableContentDeleter;
import org.unitils.dataset.util.DatabaseAccessor;

import java.util.List;
import java.util.Properties;

/**
 * First deletes all data from the tables in the data set and then
 * loads the data using insert statements.
 *
 * @author Tim Ducheyne
 * @author Filip Neven
 */
public class CleanInsertDataSetStrategy extends InsertDataSetStrategy {

    protected TableContentDeleter tableContentDeleter;


    @Override
    public void init(Properties configuration, Database database) {
        super.init(configuration, database);
        tableContentDeleter = createTableContentDeleter(identifierNameProcessor, databaseAccessor);
    }


    @Override
    public void perform(DataSetRowSource dataSetRowSource, List<String> variables) {
        deleteDataFromTablesInReverseOrder(dataSetRowSource);
        super.perform(dataSetRowSource, variables);
    }


    protected void deleteDataFromTablesInReverseOrder(DataSetRowSource dataSetRowSource) {
        try {
            dataSetRowSource.open();
            tableContentDeleter.deleteDataFromTablesInReverseOrder(dataSetRowSource);

        } catch (Exception e) {
            throw new UnitilsException("Unable to delete table data for data set file: " + dataSetRowSource.getDataSetName(), e);
        } finally {
            dataSetRowSource.close();
        }
    }


    protected TableContentDeleter createTableContentDeleter(IdentifierNameProcessor identifierNameProcessor, DatabaseAccessor databaseAccessor) {
        TableContentDeleter tableContentDeleter = new TableContentDeleter();
        tableContentDeleter.init(identifierNameProcessor, databaseAccessor);
        return tableContentDeleter;
    }

}