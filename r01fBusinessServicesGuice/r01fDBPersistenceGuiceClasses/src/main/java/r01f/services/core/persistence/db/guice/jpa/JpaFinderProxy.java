package r01f.services.core.persistence.db.guice.jpa;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import r01f.services.core.persistence.db.guice.jpa.finder.Finder;
import r01f.services.core.persistence.db.guice.jpa.finder.FirstResult;
import r01f.services.core.persistence.db.guice.jpa.finder.MaxResults;

/**
 * TODO(user): Make this work!!
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Singleton
     class JpaFinderProxy 
implements MethodInterceptor {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	private final Map<Method,FinderDescriptor> _finderCache = new MapMaker()
																	.weakKeys()
																	.makeMap();
	private final Provider<EntityManager> _emProvider;
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Inject
	public JpaFinderProxy(final Provider<EntityManager> emProvider) {
		_emProvider = emProvider;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Object invoke(final MethodInvocation methodInvocation) throws Throwable {
		EntityManager em = _emProvider.get();

		// obtain a cached finder descriptor (or create a new one)
		JpaFinderProxy.FinderDescriptor finderDescriptor = _getFinderDescriptor(methodInvocation);

		Object result = null;

		// execute as query (named params or otherwise)
		Query jpaQuery = finderDescriptor.createQuery(em);
		if (finderDescriptor._isBindAsRawParameters) {
			_bindQueryRawParameters(jpaQuery,finderDescriptor,methodInvocation.getArguments());
		} else {
			_bindQueryNamedParameters(jpaQuery,finderDescriptor,methodInvocation.getArguments());
		}

		// depending upon return type, decorate or return the result as is
		if (JpaFinderProxy.ReturnType.PLAIN.equals(finderDescriptor._returnType)) {
			result = jpaQuery.getSingleResult();
		} else if (JpaFinderProxy.ReturnType.COLLECTION.equals(finderDescriptor._returnType)) {
			result = _getAsCollection(finderDescriptor,jpaQuery.getResultList());
		} else if (JpaFinderProxy.ReturnType.ARRAY.equals(finderDescriptor._returnType)) {
			result = jpaQuery.getResultList().toArray();
		}

		return result;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings({ "unchecked", "rawtypes" }) // JPA Query returns raw type.
	private Object _getAsCollection(final JpaFinderProxy.FinderDescriptor finderDescriptor,final List results) {
		Collection<?> collection;
		try {
			collection = (Collection) finderDescriptor._returnCollectionTypeConstructor.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException("Specified collection class of Finder's returnAs could not be instantated: "
					+ finderDescriptor._returnCollectionType,e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(
					"Specified collection class of Finder's returnAs could not be instantated (do not have"
							+ " access privileges): " + finderDescriptor._returnCollectionType,
					e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(
					"Specified collection class of Finder's returnAs could not be instantated (it threw an"
							+ " exception): " + finderDescriptor._returnCollectionType,
					e);
		}

		collection.addAll(results);
		return collection;
	}
	private void _bindQueryNamedParameters(final Query jpaQuery,final JpaFinderProxy.FinderDescriptor descriptor,
			final Object[] arguments) {
		for (int i = 0; i < arguments.length; i++) {
			Object argument = arguments[i];
			Object annotation = descriptor._parameterAnnotations[i];

			if (null == annotation)
			// noinspection UnnecessaryContinue
			{
				continue; // skip param as it's not bindable
			} else if (annotation instanceof Named) {
				Named named = (Named) annotation;
				jpaQuery.setParameter(named.value(),argument);
			} else if (annotation instanceof jakarta.inject.Named) {
				jakarta.inject.Named named = (jakarta.inject.Named) annotation;
				jpaQuery.setParameter(named.value(),argument);
			} else if (annotation instanceof jakarta.inject.Named) {
				jakarta.inject.Named named = (jakarta.inject.Named) annotation;
				jpaQuery.setParameter(named.value(),argument);
			} else if (annotation instanceof FirstResult) {
				jpaQuery.setFirstResult((Integer) argument);
			} else if (annotation instanceof MaxResults) {
				jpaQuery.setMaxResults((Integer) argument);
			}
		}
	}
	private void _bindQueryRawParameters(final Query jpaQuery,final JpaFinderProxy.FinderDescriptor descriptor,final Object[] arguments) {
		for (int i = 0, index = 1; i < arguments.length; i++) {
			Object argument = arguments[i];
			Object annotation = descriptor._parameterAnnotations[i];

			if (null == annotation) {
				// bind it as a raw param (1-based index, yes I know its different from
				// Hibernate, blargh)
				jpaQuery.setParameter(index,argument);
				index++;
			} else if (annotation instanceof FirstResult) {
				jpaQuery.setFirstResult((Integer) argument);
			} else if (annotation instanceof MaxResults) {
				jpaQuery.setMaxResults((Integer) argument);
			}
		}
	}
	private JpaFinderProxy.FinderDescriptor _getFinderDescriptor(final MethodInvocation invocation) {
		Method method = invocation.getMethod();
		JpaFinderProxy.FinderDescriptor finderDescriptor = _finderCache.get(method);
		if (null != finderDescriptor) {
			return finderDescriptor;
		}

		// otherwise reflect and cache finder info...
		finderDescriptor = new JpaFinderProxy.FinderDescriptor();

		// determine return type
		finderDescriptor._returnClass = invocation.getMethod().getReturnType();
		finderDescriptor._returnType = _determineReturnType(finderDescriptor._returnClass);

		// determine finder query characteristics
		Finder finder = invocation.getMethod().getAnnotation(Finder.class);
		String query = finder.query();
		if (!"".equals(query.trim())) {
			finderDescriptor.setQuery(query);
		} else {
			finderDescriptor.setNamedQuery(finder.namedQuery());
		}

		// determine parameter annotations
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		Object[] discoveredAnnotations = new Object[parameterAnnotations.length];
		for (int i = 0; i < parameterAnnotations.length; i++) {
			Annotation[] annotations = parameterAnnotations[i];
			// each annotation per param
			for (Annotation annotation : annotations) {
				// discover the named, first or max annotations then break out
				Class<? extends Annotation> annotationType = annotation.annotationType();
				if (Named.class.equals(annotationType) || jakarta.inject.Named.class.equals(annotationType)
						|| jakarta.inject.Named.class.equals(annotationType)) {
					discoveredAnnotations[i] = annotation;
					finderDescriptor._isBindAsRawParameters = false;
					break;
				} else if (FirstResult.class.equals(annotationType)) {
					discoveredAnnotations[i] = annotation;
					break;
				} else if (MaxResults.class.equals(annotationType)) {
					discoveredAnnotations[i] = annotation;
					break;
				} // leave as null for no binding
			}
		}

		// set the discovered set to our finder cache object
		finderDescriptor._parameterAnnotations = discoveredAnnotations;

		// discover the returned collection implementation if this finder returns a
		// collection
		if (JpaFinderProxy.ReturnType.COLLECTION.equals(finderDescriptor._returnType)
				&& finderDescriptor._returnClass != Collection.class) {
			finderDescriptor._returnCollectionType = finder.returnAs();
			try {
				finderDescriptor._returnCollectionTypeConstructor = finderDescriptor._returnCollectionType
						.getConstructor();
				finderDescriptor._returnCollectionTypeConstructor.setAccessible(true); // UGH!
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(
						"Finder's collection return type specified has no default constructor! returnAs: "
								+ finderDescriptor._returnCollectionType,
						e);
			}
		}

		// cache it
		_cacheFinderDescriptor(method,finderDescriptor);

		return finderDescriptor;
	}

	/**
	 * writes to a chm (used to provide copy-on-write but this is bettah!)
	 *
	 * @param method           The key
	 * @param finderDescriptor The descriptor to cache
	 */
	private void _cacheFinderDescriptor(final Method method,final FinderDescriptor finderDescriptor) {
		// write to concurrent map
		_finderCache.put(method,finderDescriptor);
	}

	private JpaFinderProxy.ReturnType _determineReturnType(final Class<?> returnClass) {
		if (Collection.class.isAssignableFrom(returnClass)) {
			return JpaFinderProxy.ReturnType.COLLECTION;
		} else if (returnClass.isArray()) {
			return JpaFinderProxy.ReturnType.ARRAY;
		}

		return JpaFinderProxy.ReturnType.PLAIN;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * A wrapper data class that caches information about a finder method. 
	 */
	private static class FinderDescriptor {
		private volatile boolean _isKeyedQuery = false;
		volatile boolean _isBindAsRawParameters = true;
		// should we treat the query as having ? instead of :named params
		volatile JpaFinderProxy.ReturnType _returnType;
		volatile Class<?> _returnClass;

		@SuppressWarnings("rawtypes") // Unavoidable because class literal uses raw type
		volatile Class<? extends Collection> _returnCollectionType;

		volatile Constructor<?> _returnCollectionTypeConstructor;
		volatile Object[] _parameterAnnotations;
		// contract is: null = no bind, @Named = param, @FirstResult/@MaxResults for
		// paging

		private String _query;
		private String _name;

		void setQuery(final String query) {
			_query = query;
		}
		void setNamedQuery(final String name) {
			_name = name;
			_isKeyedQuery = true;
		}
		public boolean isKeyedQuery() {
			return _isKeyedQuery;
		}
		Query createQuery(final EntityManager em) {
			return _isKeyedQuery ? em.createNamedQuery(_name) : em.createQuery(_query);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	private static enum ReturnType {
		PLAIN, 
		COLLECTION, 
		ARRAY
	}
}
