/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt
 */

package org.collectionspace.services.common.init;

import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.storage.JDBCTools;
import org.collectionspace.services.common.service.ServiceBindingType;
import org.collectionspace.services.common.service.InitHandler.Params.Field;
import org.collectionspace.services.common.service.InitHandler.Params.Property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;

/** Concrete class which does nothing, but subclasses may override to do
 *  some action on the event onRepositoryInitialized(), such as sending JDBC
 *  calls to the repository to add indices, etc.
 * @author Laramie
 */
public class InitHandler implements IInitHandler {

    final Logger logger = LoggerFactory.getLogger(InitHandler.class);

    public void onRepositoryInitialized(ServiceBindingType sbt, List<Field> fields, List<Property> properties) throws Exception {
        // see org.collectionspace.services.common.init.AddIndices for a real implementation example.
        System.out.println("\r\n\r\n~~~~~~~~~~~~~ in InitHandler.onRepositoryInitialized with ServiceBindingType: "+sbt);
        for (Field field : fields){
            System.out.println( "InitHandler.fields:"
                               +"\r\n    col: "+field.getCol()
                               +"   table: "+field.getTable()
                               +"   type: "+field.getType()
                               +"   param: "+field.getParam());
        }
        for (Property prop : properties){
            System.out.println( "InitHandler.properties:"
                               +"\r\n    key: "+prop.getKey()
                               +"   value: "+prop.getValue());

        }
    }

    public ResultSet openResultSet(String sql) throws Exception {
        return JDBCTools.openResultSet(sql);
    }

    public void closeResultSet(ResultSet rs) throws SQLException {
        rs.close();
    }


}
