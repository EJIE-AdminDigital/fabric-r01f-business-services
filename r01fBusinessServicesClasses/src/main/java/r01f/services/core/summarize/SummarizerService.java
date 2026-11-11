package r01f.services.core.summarize;

import r01f.locale.Language;
import r01f.model.PersistableModelObject;
import r01f.model.SummarizedPersistableObject;
import r01f.model.security.context.SecurityContext;
import r01f.oids.PersistableObjectOID;

public interface SummarizerService<O extends PersistableObjectOID,M extends PersistableModelObject<O>,
								   S extends SummarizedPersistableObject<O,M>> {
	/**
	 * Summarizes the given obj 
	 * @param securityContext
	 * @param targetObjOid
	 * @param lang
	 * @return
	 */
	public S summarize(final SecurityContext securityContext,
					   final O targetObjOid,final Language lang);
	/**
	 * Summarizes the given obj
	 * @param securityContext
	 * @param obj
	 * @param lang
	 * @return
	 */
	public S summarize(final SecurityContext securityContext,
					   final M obj,final Language lang);
	/**
	 * Invalidates any cached copy of the summarized object
	 * @param objOid
	 */
	public void invalidate(final O objOid);
	/**
	 * Check if the given object is cached
	 * @param objOid
	 * @return
	 */
	public boolean isCached(final O objOid);
}
