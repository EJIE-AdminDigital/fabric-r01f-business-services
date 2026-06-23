package r01f.services.core.persistence.db.dialect;

import java.sql.Types;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.descriptor.sql.internal.DdlTypeImpl;
import org.hibernate.type.descriptor.sql.spi.DdlTypeRegistry;
import org.hibernate.type.spi.TypeConfiguration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DBMySQLFabricDialect 
	extends MySQLDialect {
	   
 
    /**
     * Overrides Hibernate's default DDL type registration.
     *
     * Hibernate 6 uses a DdlTypeRegistry internally to determine
     * which SQL type should be generated in DDL statements.
     *
     * Here we explicitly register a new DdlType for Types.CLOB,
     * forcing Hibernate to use "longtext" when generating schema
     * for MySQL.
     *
     * This affects only schema generation (DDL), not runtime JDBC binding.
     */
    @Override
    public void contributeTypes(final TypeContributions typeContributions,
    							final ServiceRegistry serviceRegistry) {
  
        // Preserve default behavior first
        super.contributeTypes(typeContributions, serviceRegistry);
 
        TypeConfiguration typeConfig = typeContributions.getTypeConfiguration();
        DdlTypeRegistry ddlRegistry = typeConfig.getDdlTypeRegistry();
 
        // Replace the DDL mapping for CLOB → longtext
        ddlRegistry.addDescriptor(
                new DdlTypeImpl(Types.CLOB,
                                "longtext",
                                 this
                )
        );
 
        log.warn("[Fabric] MySQL Dialect: Types.CLOB mapped to LONGTEXT");
    }
}