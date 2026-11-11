package r01f.services.core.persistence.db.sql;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.extern.slf4j.Slf4j;
import r01f.io.IOStreams;
import r01f.model.security.context.SecurityContext;
import r01f.objectstreamer.Marshaller;
import r01f.oids.PersistableObjectOID;
import r01f.services.core.persistence.db.DBBase;
import r01f.services.core.persistence.db.config.DBModuleConfig;

@Slf4j
public abstract class DBBLOBCRUDCommonBase<O extends PersistableObjectOID>
			  extends DBBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBBLOBCRUDCommonBase(final DBModuleConfig dbCfg,
					  	  		final EntityManager entityManager,
					  	  		final Marshaller marshaller) {
		super(dbCfg,
			  entityManager,
			  marshaller);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	ABSTRACT
/////////////////////////////////////////////////////////////////////////////////////////
	protected abstract String _createBLOBSelectSQL(final O oid);

/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public long uploadApplicationBLOB(final SecurityContext securityContext,
							  		  final O oid,
							  		  final InputStream is) throws IOException {
		long outBlobSize = -1;

		EntityTransaction dbTrans = _entityManager.getTransaction();
		if (!dbTrans.isActive()) dbTrans.begin();	// beware! before unwrap the Connection
		Connection dbConx = _entityManager.unwrap(Connection.class);
		try {	// DO NOT use try-with-resources!!
			// [2] - Upload blob
			outBlobSize = this.uploadBlob(dbConx,
						    			  oid,
						    			  is);
			// [3] - Commit
			dbTrans.commit();
		} catch (SQLException sqlEx) {
			log.error("Error uploading blob data for object oid={}: {}",
					  oid,
					  sqlEx.getMessage(),sqlEx);
			dbTrans.rollback();

			throw new IOException("Could NOT update blob data of object oid=" + oid + ": " + sqlEx.getMessage());
		} finally {
			try {
				if (!dbConx.isClosed()) dbConx.close();
			} catch (Throwable th) {
				// ignore
			}
		}
		return outBlobSize;
	}
	public long uploadBlob(final Connection dbConx,
				 		   final O oid,
				 		   final InputStream is) throws SQLException {
		if (is == null) return 0;

		log.debug("...upload BLOB data of object with oid={}",oid);
		long outBlobSize = -1;
		try (Statement stmt = dbConx.createStatement(ResultSet.TYPE_FORWARD_ONLY,
													 ResultSet.CONCUR_UPDATABLE);
			ResultSet rset = stmt.executeQuery(_createBLOBSelectSQL(oid))) {
			// get first row
			if (rset.next()) {
				// update
				rset.updateBinaryStream(2,	// column index
									    is);
				rset.updateRow();

				// get the length
				outBlobSize = rset.getBlob(2).length();
			} else {
				throw new SQLException("blob with oid=" + oid + " does NOT exists!");
			}
		}
		log.debug("...uploaded BLOB data of object with oid={}",oid);
		return outBlobSize;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public long getBlobLength(final SecurityContext securityContext,
							  final O oid) throws IOException {
		long outBlobSize = -1;

		EntityTransaction dbTrans = _entityManager.getTransaction();
		if (!dbTrans.isActive()) dbTrans.begin();	// beware! before unwrap the Connection
		Connection dbConx = _entityManager.unwrap(Connection.class);
		try {	// DO NOT use try-with-resources!!
			outBlobSize = this.getBlobLength(dbConx,
						    			  	 oid);
			dbTrans.commit();
		} catch (SQLException sqlEx) {
			log.error("Error getting blob length of object with oid={}: {}",
					  oid,
					  sqlEx.getMessage(),sqlEx);
			dbTrans.rollback();

			throw new IOException("Could NOT get blob length of object with oid=" + oid + ": " + sqlEx.getMessage());
		} finally {
			try {
				if (!dbConx.isClosed()) dbConx.close();
			} catch (Throwable th) {
				// ignore
			}
		}
		return outBlobSize;
	}
	public long getBlobLength(final Connection dbConx,
						   	  final O oid) throws SQLException {
		log.debug("...get BLOB of object with oid={} length",oid);

		Long outLength = null;
		try (Statement stmt = dbConx.createStatement(ResultSet.TYPE_FORWARD_ONLY,
													 ResultSet.CONCUR_READ_ONLY);
			ResultSet rset = stmt.executeQuery(_createBLOBSelectSQL(oid))) {
			if (rset.next()) {
				// get the blob size
				outLength = rset.getBlob(2).length();
			} else {
				throw new SQLException("blob with oid=" + oid + " does NOT exists!");
			}
			return outLength;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DOWNLOAD
/////////////////////////////////////////////////////////////////////////////////////////
	public InputStream downloadBlob(final SecurityContext securityContext,
									final O oid) throws IOException {
		InputStream outData = null;

		EntityTransaction dbTrans = _entityManager.getTransaction();
		if (!dbTrans.isActive()) dbTrans.begin();	// beware! before unwrap the Connection
		Connection dbConx = _entityManager.unwrap(Connection.class);
		try {	// DO NOT use try-with-resources!!
			outData = this.retrieveBlobForReading(dbConx,
								 			  	  oid);
			dbTrans.commit();
		} catch (SQLException sqlEx) {
			log.error("Error DOWNLOADING blob oid={}: {}",
					  oid,
					  sqlEx.getMessage(),sqlEx);
			dbTrans.rollback();

			throw new IOException("Could NOT get the data of blob oid=" + oid + ": " + sqlEx.getMessage());
		} finally {
			try {
				if (!dbConx.isClosed()) dbConx.close();
			} catch (Throwable th) {
				// ignore
			}
		}
		return outData;
	}
	public InputStream retrieveBlobForReading(final Connection dbConx,
									  		  final O oid) throws SQLException {
		log.debug("...retrieve BLOB of object with oid={} for READING",oid);
		try (Statement stmt = dbConx.createStatement(ResultSet.TYPE_FORWARD_ONLY,
													 ResultSet.CONCUR_READ_ONLY);
			ResultSet rset = stmt.executeQuery(_createBLOBSelectSQL(oid))) {

			// get first row
			InputStream outData = null;
			if (rset.next()) {
				// get the blob size
				long blobLength = rset.getBlob(2).length();

				// get an input stream to the blob
				InputStream blobContent = rset.getBinaryStream(2);		// column index

				// BEWARE!! The [blob] content MUST be CLONED since the [blobContent] input stream
				//		    is CLOSED when the connection is released, so the client can no longer
				//			read from it
				// 			BE CAREFUL IF THE BLOB CONTENT IS HUGE > the memory will exploit
				outData = _cloneInputStream(oid,
											blobContent,(int)blobLength);
			} else {
				throw new SQLException("blob with oid=" + oid + " does NOT exists!");
			}
			return outData;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DOWNLOAD CHUNK
/////////////////////////////////////////////////////////////////////////////////////////
	public byte[] downloadBlobChunk(final SecurityContext securityContext,
									final O oid,
									final long chunkStartPos,final int chunkLength) throws IOException {
		byte[] outData = null;

		EntityTransaction dbTrans = _entityManager.getTransaction();
		if (!dbTrans.isActive()) dbTrans.begin();	// beware! before unwrap the Connection
		Connection dbConx = _entityManager.unwrap(Connection.class);
		try {	// DO NOT use try-with-resources!!
			outData = this.retrieveBlobChunk(dbConx,
										 	 oid,
										 	 chunkStartPos,chunkLength);
			dbTrans.commit();
		} catch (SQLException sqlEx) {
			log.error("Error DOWNLOADING blob oid={}: {}",
					  oid,
					  sqlEx.getMessage(),sqlEx);
			dbTrans.rollback();

			throw new IOException("Could NOT get the data of blob oid=" + oid + ": " + sqlEx.getMessage());
		} finally {
			try {
				if (!dbConx.isClosed()) dbConx.close();
			} catch (Throwable th) {
				// ignore
			}
		}
		return outData;
	}
	public byte[] retrieveBlobChunk(final Connection dbConx,
							  	  	final O oid,
								  	final long chunkStartPos,final int chunkLength) throws SQLException {
		log.debug("...retrieve BLOB oid={} CHUNK of length={} from pos={}",
				  oid,chunkLength,chunkStartPos);
		try (Statement stmt = dbConx.createStatement(ResultSet.TYPE_FORWARD_ONLY,
													 ResultSet.CONCUR_READ_ONLY);
			ResultSet rset = stmt.executeQuery(_createBLOBSelectSQL(oid))) {

			// get first row
			byte[] outData = null;
			if (rset.next()) {
				// get the blob size
				long blobLength = rset.getBlob(2).length();
				int theChunkLength = chunkStartPos + chunkLength <= blobLength ? chunkLength
																				: (int)(blobLength);
				if (chunkStartPos < blobLength) {
					// get an input stream to the blob
					outData = rset.getBlob(2)	// column index
								  .getBytes(chunkStartPos,theChunkLength);

					// BEWARE!! If returning an InputStream, the [blob] chunk MUST be CLONED since the [blobContent]
					//			input stream is CLOSED when the connection is released, so the client can no longer
					//			read from it
					// 			BE CAREFUL IF THE BLOB CHUNK IS HUGE > the memory will BANG
					// InputStream outdata = rset.getBlob(2)
					//						     .getBinaryStream(chunkStartPost,theChunkLength);
					// outData = _cloneInputStream(oid,
					//							   blobChunk,theChunkLength);
				}
			} else {
				throw new SQLException("blob with oid=" + oid + " does NOT exists!");
			}
			return outData;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	private InputStream _cloneInputStream(final O blobOid,
										  final InputStream is,final int contentLength) {
		InputStream outData = null;
		try {
			// fully read the blob content > BEWARE!! this is an in-memory operation
			ByteArrayOutputStream os = new ByteArrayOutputStream(contentLength);	// will contain ALL the blob in memory!!
			IOStreams.copy(is,os,
						   true);
			// and return the data
			outData = new ByteArrayInputStream(os.toByteArray());
		} catch (IOException ioEx) {
			log.error("could NOT read data for [blob] with oid={}",
					  blobOid,
					  ioEx.getMessage(),ioEx);
		}
		return outData;
	}
}
