package r01f.services.core.persistence.db.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import r01f.patterns.IsBuilder;
import r01f.services.core.ServicesCoreModuleConfig;
import r01f.services.ids.ServiceIDs.CoreSubModule;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class DBModuleConfigBuilderForHibernate
		   implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	public static final <CFG extends DBModuleConfig> CFG dbConfigFor(final XMLPropertiesForAppComponent xmlProps) {
		PersistenceUnitType persistenceUnitType = xmlProps.propertyAt("persistence/@unitType")
														  .asEnumElement(PersistenceUnitType.class,
																  		 PersistenceUnitType.DRIVER_MANAGER);	// use driverManager by default
		return persistenceUnitType.is(PersistenceUnitType.DRIVER_MANAGER)
							? (CFG)new DBModuleConfigForPoolConnectionForHibernate(xmlProps)			// driver managed
							: (CFG)new DBModuleConfigForDataSourceConnectionForHibernate(xmlProps);		// data-source
	}
	@SuppressWarnings("unchecked")
	public static <DBCFG extends DBModuleConfig> DBCFG dbModuleConfigFrom(final ServicesCoreModuleConfig coreCfg) {
		return (DBCFG)coreCfg.getSubModuleConfigFor(CoreSubModule.DBPERSISTENCE);
	}
}
