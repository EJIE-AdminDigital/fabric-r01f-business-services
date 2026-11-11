package r01f.services.core.persistence.db.config;



import java.util.HashMap;
import java.util.Map;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import jakarta.persistence.Table; //

public abstract class DBTableNamingStrategyBase 
		extends PhysicalNamingStrategyStandardImpl {

	 private static final long serialVersionUID = 492705601412022916L;
/////////////////////////////////////////////////////////////////////
/// CONSTRUCTOR
/////////////////////////////////////////////////////////////////////
	 private final Map<String, String> _tableOverrides = new HashMap<>();
	 protected final String _dbTablesPrefix;	
/////////////////////////////////////////////////////////////////////
/// CONSTRUCTOR
/////////////////////////////////////////////////////////////////////
	 public DBTableNamingStrategyBase(final String dbTablesPrefix) {
	     super();
	     _dbTablesPrefix = (dbTablesPrefix != null) ? dbTablesPrefix : "";	  
	     registerTablesToOverrideWithPrefix();
	 }
/////////////////////////////////////////////////////////////////////
/// METHODS TO IMPLEMENT
/////////////////////////////////////////////////////////////////////
	 protected abstract void registerTablesToOverrideWithPrefix();
	
	 protected void _registerOverride(final Class<?> entityClass, final String targetTableName) {
        Table tableAnnotation = entityClass.getAnnotation(Table.class);
        if (tableAnnotation != null && !tableAnnotation.name().trim().isEmpty()) {           
            String originalName = tableAnnotation.name().replace("\"", "").trim().toUpperCase();
            _tableOverrides.put(originalName, targetTableName);
          
        } else {           
            String simpleClassName = entityClass.getSimpleName().toUpperCase();
            _tableOverrides.put(simpleClassName, targetTableName);
          
        }
	  }
	
	 @Override
	 public Identifier toPhysicalTableName(final Identifier name, final JdbcEnvironment context) {
	     if (name != null) {
	         String originalTableName = name.getText().toUpperCase();	       
	         if (_tableOverrides.containsKey(originalTableName)) {
	           
	             String newTableName = _tableOverrides.get(originalTableName);
	             return Identifier.toIdentifier(newTableName);
	         }
	     }
	     return super.toPhysicalTableName(name, context);
	 }
}