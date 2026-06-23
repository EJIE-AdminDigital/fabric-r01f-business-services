package r01f.services.core.persistence.db.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(prefix="_")
@RequiredArgsConstructor
public enum DBSchemaTablesDDLActionForHibernate 
 implements DBSchemaTablesDDLAction {
	NONE("none",org.hibernate.tool.schema.Action.ACTION_NONE), 													// Hibernate equivalent: No action
    CREATE_TABLES("create",org.hibernate.tool.schema.Action.ACTION_CREATE), 									// Hibernate: Create tables
    CREATE_OR_EXTEND_TABLES("update",org.hibernate.tool.schema.Action.ACTION_UPDATE), 							// TODO Not exist hibernate
    DROP_TABLES("none",org.hibernate.tool.schema.Action.ACTION_DROP), 										 	// Hibernate: Validate (no schema change)
    DROP_AND_CREATE_TABLES("drop-and-create",org.hibernate.tool.schema.Action.SPEC_ACTION_DROP_AND_CREATE); 	// Hibernate: Drop and create tables	
	
	@Getter private final String _jpaAction;
	@Getter private final String _vendorAction;

	public static DBSchemaTablesDDLActionForHibernate fromName(final String name) {
		return DBSchemaTablesDDLActionForHibernate.valueOf(name.toUpperCase());
	}
}