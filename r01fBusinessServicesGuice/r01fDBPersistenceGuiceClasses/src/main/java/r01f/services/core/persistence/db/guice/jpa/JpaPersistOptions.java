package r01f.services.core.persistence.db.guice.jpa;

public final class JpaPersistOptions {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
  private final boolean _autoBeginWorkOnEntityManagerCreation;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
  	private JpaPersistOptions(final JpaPersistOptions.Builder builder) {
    	_autoBeginWorkOnEntityManagerCreation = builder._autoBeginWorkOnEntityManagerCreation;
 	}
	/** 
	 * Returns a builder to set options. 
	 */
	public static Builder builder() {
	  	return new Builder();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns true if the work unit should automatically begin when the EntityManager is created, if
	 * it hasn't already begun.
	 * <p>This defaults to <b>false</b> because it's not safe, as careless usage can lead to leaking
	 * sessions.
	 */
	public boolean getAutoBeginWorkOnEntityManagerCreation() {
	  	return _autoBeginWorkOnEntityManagerCreation;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * A builder to create the options.
	 */
	public static final class Builder {
	  	private boolean _autoBeginWorkOnEntityManagerCreation;
	
	  	private Builder() {}
	
	  	public JpaPersistOptions build() {
	   		return new JpaPersistOptions(this);
	  	}
	  	/** 
	  	 * Sets the {@link JpaPersistOptions#getAutoBeginWorkOnEntityManagerCreation} property. 
	  	 */
	  	public Builder setAutoBeginWorkOnEntityManagerCreation(final boolean autoBeginWorkOnEntityManagerCreation) {
	    	_autoBeginWorkOnEntityManagerCreation = autoBeginWorkOnEntityManagerCreation;
	    	return this;
	  	}
	}
}
