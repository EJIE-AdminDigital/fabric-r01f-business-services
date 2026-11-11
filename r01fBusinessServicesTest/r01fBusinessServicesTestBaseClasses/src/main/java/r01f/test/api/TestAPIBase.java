package r01f.test.api;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.concurrent.Threads;
import r01f.oids.SecurityIDs.IdentityProvider;
import r01f.oids.SecurityIDs.SecurityPrincipalID;
import r01f.oids.SecurityOIDs.SecurityPrincipalOID;
import r01f.types.TimeLapse;
import r01f.util.types.Strings;

/**
 * 		JVM argument:
 * 				-DR01_HOME={DEV_HOME}		(ej: d:/develop)
 * 				-javaagent:{DEV_HOME}/local_libs/aspectj/lib/aspectjweaver.jar -Daj.weaving.verbose=true
 * 				-DEJIE_PROPERTIES_PATTERN=/default/[entityCode]/[entityCode].properties.xml -DEJIE_PROPERTY_LOADER=classPathLoader
 * 		Add to the classpath:
 * 			- The MySQL / Posgre / Oracle driver
 */

@Slf4j
@Accessors(prefix="_")
@RequiredArgsConstructor
public abstract class TestAPIBase {  // Just required at this lever for this contants //
	
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	public static final SecurityPrincipalOID TEST_PRINCIPAL_OID = SecurityPrincipalOID.forId("testPrincipalOid");
	public static final SecurityPrincipalID TEST_PRINCIPAL_ID = SecurityPrincipalID.forId("testPrincipalId");
	
	public static final IdentityProvider TEST_IDENTITY_PROVIDER = IdentityProvider.GOOGLE; 		
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	protected void runTest(final int iterationNum) {
		try {
			Stopwatch stopWatch = Stopwatch.createStarted();
		
			for (int i=0; i < iterationNum; i++) {
				Stopwatch itStopWatch = Stopwatch.createStarted();
				System.out.println("\n\n\n\nSTART =========== Iteration " + i + " ===================\n\n\n\n");
				
				_doTest();		// Iteration test
			
				System.out.println("\n\n\n\nEND =========== Iteration " + i + " > " + itStopWatch.elapsed(TimeUnit.SECONDS) + "seconds ===================\n\n\n\n");
			}
		
			System.out.println("\n\n\n\n******* ELAPSED TIME: " + NumberFormat.getNumberInstance(Locale.getDefault()).format(stopWatch.elapsed(TimeUnit.SECONDS)) + " seconds");
			stopWatch.stop();
		} catch (Throwable th) {
			th.printStackTrace(System.out);
		
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("static-method")
	protected void _doTest() {
		log.warn("MUST implement this!");
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	protected static void _giveTimeForBackgroundJobsToFinish(final long milis) {
		_giveTimeForBackgroundJobsToFinish(milis,
										   null);	// default msg
	}
	
	@SuppressWarnings("deprecation")
	protected static void _giveTimeForBackgroundJobsToFinish(final TimeLapse lapse) {
		_giveTimeForBackgroundJobsToFinish(lapse.asMillis(),
											null);	// default msg
	}
	protected static void _giveTimeForBackgroundJobsToFinish(final long milis,
															 final String msg,final Object... msgParams) {
		// wait for background jobs to complete (if there's any background job that depends on DB data -like lucene indexing-
		// 										 if the DB data is deleted BEFORE the background job finish, it'll fail)
		if (Strings.isNullOrEmpty(msg)) {
			log.warn("... give {} milis for background jobs (ie lucene index) to complete before deleting created DB records (ie lucene indexing will fail if the DB record is deleted)",milis);
		} else {
			log.warn("... give {} milis for {}",milis,Strings.customized(msg,msgParams));
		}
		Threads.safeSleep(milis);
	}
	protected static void _giveTimeForBackgroundJobsToFinish(final TimeLapse lapse,
															  final String msg,final Object... msgParams) {
		_giveTimeForBackgroundJobsToFinish(lapse.asMillis(),
										   msg,msgParams);
	}

}
