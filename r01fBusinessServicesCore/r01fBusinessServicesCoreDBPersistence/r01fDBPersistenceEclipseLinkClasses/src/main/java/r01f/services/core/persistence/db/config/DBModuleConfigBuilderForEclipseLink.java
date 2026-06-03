package r01f.services.core.persistence.db.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import r01f.patterns.IsBuilder;
import r01f.services.core.ServicesCoreModuleConfig;
import r01f.services.ids.ServiceIDs.CoreSubModule;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class DBModuleConfigBuilderForEclipseLink
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
							? (CFG)new DBModuleConfigForPoolConnectionForEclipselink(xmlProps)			// driver managed
							: (CFG)new DBModuleConfigForDataSourceConnectionForEclipseLink(xmlProps);	// data-source
	}
	@SuppressWarnings("unchecked")
	public static <DBCFG extends DBModuleConfig> DBCFG dbModuleConfigFrom(final ServicesCoreModuleConfig coreCfg) {
		// the coreCfg has the DBModuleConfig for the DBPersistence core module
		if (coreCfg.getSubModuleConfigFor(CoreSubModule.DBPERSISTENCE) == null) throw new IllegalStateException("The [core module] config MUST have the DBModuleConfig for the DBPersistence core module");
		return (DBCFG)coreCfg.getSubModuleConfigFor(CoreSubModule.DBPERSISTENCE);
	}
}
