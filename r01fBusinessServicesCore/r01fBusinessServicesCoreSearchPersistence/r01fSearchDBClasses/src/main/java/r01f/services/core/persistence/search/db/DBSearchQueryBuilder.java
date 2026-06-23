package r01f.services.core.persistence.search.db;

import jakarta.persistence.EntityManager;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import r01f.locale.Language;
import r01f.model.search.SearchFilter;
import r01f.patterns.IsBuilder;
import r01f.patterns.ProviderFrom;
import r01f.services.core.persistence.db.DBEntity;
import r01f.services.core.persistence.db.config.DBModuleConfig;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class DBSearchQueryBuilder
  		   implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  FLUENT API
/////////////////////////////////////////////////////////////////////////////////////////
	public static <F extends SearchFilter,DB extends DBEntity> DBSearchQueryBuilderDBModuleConfigStep<F,DB> forDBEntityType(final Class<DB> dbEntityType) {
		return DBSearchQueryBuilder.forDBEntityTypeFromFilterProvider(filter -> dbEntityType);
	}
	public static <F extends SearchFilter,DB extends DBEntity> DBSearchQueryBuilderDBModuleConfigStep<F,DB> forDBEntityTypeFromFilterProvider(final ProviderFrom<F,Class<? extends DB>> dbEntityTypeFromFilterProvider) {
		return new DBSearchQueryBuilder() { /* nothing */ }
					.new DBSearchQueryBuilderDBModuleConfigStep<>(dbEntityTypeFromFilterProvider);
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class DBSearchQueryBuilderDBModuleConfigStep<F extends SearchFilter,DB extends DBEntity> {
		private final ProviderFrom<F,Class<? extends DB>> _dbEntityTypeFromFilterProvider;
		public DBSearchQueryBuilderEntityManagerStep<F,DB> usingDBModuleConfig(final DBModuleConfig dbModuleConfig) {
			return new DBSearchQueryBuilderEntityManagerStep<>(_dbEntityTypeFromFilterProvider,
															   dbModuleConfig);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class DBSearchQueryBuilderEntityManagerStep<F extends SearchFilter,DB extends DBEntity> {
		private final ProviderFrom<F,Class<? extends DB>> _dbEntityTypeFromFilterProvider;
		private final DBModuleConfig _dbModuleConfig;
		public DBSearchBuilderQueryUILanguageStep<F,DB> with(final EntityManager entityManager) {
			return new DBSearchBuilderQueryUILanguageStep<>(_dbEntityTypeFromFilterProvider,
															_dbModuleConfig,
															entityManager);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class DBSearchBuilderQueryUILanguageStep<F extends SearchFilter,DB extends DBEntity> {
		private final ProviderFrom<F,Class<? extends DB>> _dbEntityTypeFromFilterProvider;
		private final DBModuleConfig _dbModuleConfig;
		private final EntityManager _entityManager;

		public DBSearchQueryBuilderPredicatesStep<F,DB> withUILanguage(final Language uiLang) {
			return new DBSearchQueryBuilderPredicatesStep<>(_dbEntityTypeFromFilterProvider,
															_dbModuleConfig,
															_entityManager,
															uiLang);
		}
		public DBSearchQueryBuilderPredicatesStep<F,DB> noUILanguage() {
			return new DBSearchQueryBuilderPredicatesStep<>(_dbEntityTypeFromFilterProvider,
															_dbModuleConfig,
															_entityManager,
															null);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class DBSearchQueryBuilderPredicatesStep<F extends SearchFilter,DB extends DBEntity> {
		private final ProviderFrom<F,Class<? extends DB>> _dbEntityTypeFromFilterProvider;
		private final DBModuleConfig _dbModuleConfig;
		private final EntityManager _entityManager;
		private final Language _uiLanguage;

		@SuppressWarnings("unused")
		public DBSearchQuery<F,DB> withFilterType(final Class<F> filterType) {
			DBSearchQuery<F,DB> outQuery = new DBSearchQuery<>(_dbEntityTypeFromFilterProvider,
															   _dbModuleConfig,
															   _entityManager,
													   		   _uiLanguage);
			return outQuery;
		}
	}
}
