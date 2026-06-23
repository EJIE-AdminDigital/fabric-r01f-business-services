package r01f.services.core.persistence.db.config;

import jakarta.persistence.EntityManager;

public enum DBPersistenceProvider {
	HIBERNATE,
	ECLIPSELINK,
	OPENJPA;
	
	public static DBPersistenceProvider fromEntityManager(final EntityManager em) {
		DBPersistenceProvider outDBPersistenceProvider = null;
	    
	    // [1] - Check the [jakarta.persistence.provider] entity manager property
		//   			Hibernate: org.hibernate.jpa.HibernatePersistenceProvider
		// 			  EclipseLink: org.eclipse.persistence.jpa.PersistenceProvider
		//     			  OpenJPA: org.apache.openjpa.persistence.PersistenceProviderImpl
	    Object provider = em.getEntityManagerFactory()
	                        .getProperties()
	                        .get("jakarta.persistence.provider");		
	    if (provider != null) {
	    	outDBPersistenceProvider = _fromClassName(provider.toString());
	    }
	    // [2] - Check the class name of the ENTITY MANAGER FACTORY
	    if (outDBPersistenceProvider == null) {
	        String emFactoryClassName = em.getEntityManagerFactory()
	        							  .getClass()
	   	     							  .getName();
	        outDBPersistenceProvider = _fromClassName(emFactoryClassName);
	    }
	    // [3] - Last chance: check the class name of the ENTITY MANAGER
        if (outDBPersistenceProvider == null) {
        	// Avoid!!!!! It's problematic because:
        	// 		If in a simple SE environment this returns
			//				Hibernate: org.hibernate.internal.SessionImpl
 			//            EclipseLink: org.eclipse.persistence.internal.jpa.EntityManagerImpl
        	// 		BUT if in spring or other CDI container this returns a proxy class name like:
        	//			jdk.proxy2.$Proxy123 (Standard Java Proxy)
			//			com.sun.proxy.$Proxy...
        	@SuppressWarnings("deprecation")
			String emImplClassName = em.getDelegate()
        							   .getClass()
        							   .getName(); 
			outDBPersistenceProvider = _fromClassName(emImplClassName);
        }
        // [99] - Return or throw
	    if (outDBPersistenceProvider == null) throw new IllegalStateException("Cannot determine the DB persistence provider from the EntityManager; " +
																			  "the class name MUST contain one of: " + HIBERNATE + ", " + ECLIPSELINK + " or " + OPENJPA);
	    return outDBPersistenceProvider;
	}
	private static DBPersistenceProvider _fromClassName(final String className) {
		if (className.contains("hibernate")) {
			return HIBERNATE;
		} else if (className.contains("eclipse")) {
			return ECLIPSELINK;
		} else if (className.contains("openjpa")) {
			return OPENJPA;
		} 
		return null;	// not found
	}
}
