package r01f.services.bootstrap.core.eventbus.spring;

import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;
import r01f.eventbus.Event;
import r01f.eventbus.EventBusSpringIsolatedImpl.TransactionalSynchronizationSupport;


/**
 * This class acts as a dedicated helper for managing transactional safety in the (ASYNC) event bus. 
 *   Its main job is to intercept event dispatches when a database transaction is active and defer them until the transaction successfully commits.
 *   This completely prevents the asynchronous worker threads from trying to read records before they physically exist in the database
 *    (solving the ENTITY_NOT_FOUND race condition).

Step-by-Step Breakdown :::

(1) isActualTransactionActive()
-------------------------------------
	[] -> Delegates directly to Spring's TransactionSynchronizationManager.isActualTransactionActive(). It checks whether the current thread is executing inside an active database transaction (such as a @Transactional method).

(2) multicastEventAfterCommit(PayloadApplicationEvent<Event> payloadEvent)
------------------------------------------------------------------------------
	[] -> The Core Logic: Instead of broadcasting the event immediately, it registers a transaction synchronization hook using TransactionSynchronizationManager.registerSynchronization(...).

       Inside the callback (afterCommit()), 
       it waits until the database transaction finishes with a successful COMMIT.
       Only then does it tell the _eventMulticaster to multicast the event to the asynchronous workers.
 */
@RequiredArgsConstructor
public class COREEventBusTransactionalAsyncSynchronizationSupport 
	implements TransactionalSynchronizationSupport  {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////	
	final SimpleApplicationEventMulticaster _eventMulticaster;	
/////////////////////////////////////////////////////////////////////////////////////////
// METHODS
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public boolean isActualTransactionActive() {			
		return TransactionSynchronizationManager.isActualTransactionActive();
	}

	@Override
	public void multicastEventAfterCommit(final PayloadApplicationEvent<Event> payloadEvent) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
														                @Override
														                public void afterCommit() {
														                    _eventMulticaster.multicastEvent(payloadEvent);
														                }});	
	}	
/////////////////////////////////////////////////////////////////////////////////////////
// CREATOR
/////////////////////////////////////////////////////////////////////////////////////////
	public static  TransactionalSynchronizationSupport createFor( final SimpleApplicationEventMulticaster eventMulticaster) {
		return new COREEventBusTransactionalAsyncSynchronizationSupport(eventMulticaster);
	}
}
