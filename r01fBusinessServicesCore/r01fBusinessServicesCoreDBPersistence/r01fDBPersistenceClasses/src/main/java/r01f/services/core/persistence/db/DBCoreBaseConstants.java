package r01f.services.core.persistence.db;

/**
 * This interface follows a Standardized Naming Convention pattern for a multi-module persistence architecture. 
 * Its purpose is to define consistent suffixes for the core components of the Spring/JPA stack, ensuring that every database module (e.g., Person, OrgConfig) 
 * follows the same naming blueprint.
 */
public interface DBCoreBaseConstants {
///////////////////////////////////////////////
///DB COMMON SUFFIX
//////////////////////////////////////////////	
	String DS_SUFFIX =  "DataSource";
    String EMF_SUFFIX = "EntityManagerFactory";
    String TM_SUFFIX =  "TransactionManager";
    String EM_SUFFIX =  "EntityManager";   
    String TMI_SUFFIX = "TransactionManagerInterceptor";
    String TMI_ADV_SUFFIX = "TransactionManagerAdvisor";
///////////////////////////////////////////////
/// DB PROPS SUFFIX
//////////////////////////////////////////////
    String  DB_PROPS_PREFIX = "db";
}
