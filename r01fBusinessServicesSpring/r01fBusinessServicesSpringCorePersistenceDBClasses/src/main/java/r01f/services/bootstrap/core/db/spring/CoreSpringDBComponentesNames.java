package r01f.services.bootstrap.core.db.spring;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.patterns.Memoized;
import r01f.services.core.persistence.db.DBCoreBaseConstants;
import r01f.services.ids.ServiceIDs.CoreModule;


@Accessors(prefix="_")
@RequiredArgsConstructor
public class CoreSpringDBComponentesNames {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////	
	private final CoreModule  _coreModule;
/////////////////////////////////////////////////////////////////////////////////////////
// DS
/////////////////////////////////////////////////////////////////////////////////////////
	private final transient Memoized<String> _composedDatataSourceName = new Memoized<>() {
																			@Override
																			public String supply() {
																				return  _coreModule.asString() + DBCoreBaseConstants.DS_SUFFIX; 
																			}
																		};
	
	public String getDataSourceBeanName() {
		return _composedDatataSourceName.get();
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//	EMF
/////////////////////////////////////////////////////////////////////////////////////////
	private final transient Memoized<String> _composedEntityManagerFactoryeName = new Memoized<>() {
																					@Override
																					public String supply() {
																						return  _coreModule.asString() + DBCoreBaseConstants.EMF_SUFFIX; 
																					}
	};

	
	public String geEntityManagerFactoryBeanName() {
		return _composedEntityManagerFactoryeName.get();
	}		
/////////////////////////////////////////////////////////////////////////////////////////
//EMF
/////////////////////////////////////////////////////////////////////////////////////////
	private final transient Memoized<String> _composedEntityManagerName = new Memoized<>() {
																				@Override
																				public String supply() {
																					return  _coreModule.asString() + DBCoreBaseConstants.EM_SUFFIX; 
																				}
																				};
	
	
	public String geEntityManagerBeanName() {
		return _composedEntityManagerName.get();
	}

/////////////////////////////////////////////////////////////////////////////////////////
// TM
/////////////////////////////////////////////////////////////////////////////////////////
	private final transient Memoized<String> _composedTransactionManagerName = new Memoized<>() {
																				@Override
																				public String supply() {
																					return  _coreModule.asString() + DBCoreBaseConstants.TM_SUFFIX; 
																				}
																			};
	
	
	public String geEntityTransactionManagerBeanName() {
		return _composedTransactionManagerName.get();
	}
/////////////////////////////////////////////////////////////////////////////////////////
// TMI
/////////////////////////////////////////////////////////////////////////////////////////
	private final transient Memoized<String> _composedTransactionManagerInterceptorName = new Memoized<>() {
																							@Override
																							public String supply() {
																								return  _coreModule.asString() + DBCoreBaseConstants.TMI_SUFFIX; 
																							}
																							};

	
	public String geEntityTransactionManagerInterceptorBeanName() {
		return _composedTransactionManagerInterceptorName.get();
	}
	
/////////////////////////////////////////////////////////////////////////////////////////
//TMI
/////////////////////////////////////////////////////////////////////////////////////////
	private final transient Memoized<String> _composedTransactionManagerAdvisorName = new Memoized<>() {
																							@Override
																							public String supply() {
																								return  _coreModule.asString() + DBCoreBaseConstants.TMI_ADV_SUFFIX; 
																							}
																							};
																					
	
	public String geEntityTransactionManagerAdvisorBeanName() {
		return _composedTransactionManagerAdvisorName.get();
	}	
////////////////////////////////////////////////////////////////
///
///
//////////////////////////////////////////////////////////////////////

}
