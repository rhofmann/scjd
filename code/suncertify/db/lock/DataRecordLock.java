package suncertify.db.lock;

import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import suncertify.db.SecurityException;

/**
 * Holds information about the lock on a data record, i.e. whether it is locked
 * at all and the current lock cookie value.
 * <p>
 * Provides methods to acquire and release this lock.
 * <p>
 * When a thread tries to acquire a lock that is already locked, it is blocked
 * on an internal condition variable.
 * <p>
 * When a lock is released, threads waiting for its release are notified via the
 * condition variable.
 * 
 * @author Rasmus Kuschel
 */
public final class DataRecordLock {

	/**
	 * Lock cookie generator
	 */
	private static final Random RND = new Random();

	/**
	 * Lock used to synchronize access to the instance variables and to provide
	 * a condition variable on which threads are synchronized.
	 */
	private final Lock lock = new ReentrantLock();

	/**
	 * Current lock cookie value.
	 */
	private long lockCookie;

	/**
	 * Flag set if this DataRecordLock instance is locked
	 */
	private boolean locked;

	/**
	 * Condition variable used to synchronize threads that want to acquire this
	 * lock.
	 */
	private final Condition lockReleased = lock.newCondition();

	/**
	 * Creates a new DataRecordLock.
	 */
	public DataRecordLock() {
		this.lockCookie = 0;
		this.locked = false;
	}

	/**
	 * Acquires this lock.
	 * <p>
	 * If the lock is not held by any thread, it is locked and a new cookie
	 * value is generated and returned.
	 * <p>
	 * If the lock is already held, the current thread waits on the condition
	 * variable until it is signalled that the lock is available. After being
	 * woken up, the thread tries again to acquire the lock, until it is
	 * successful.
	 */
	public long acquire() {

		lock.lock();
		try {
			// While the lock is not available, wait to be signalled about its
			// release.
			// We check the condition in a loop because the thread might
			// have been reactivated by a "spurious wakeup".
			// Therefore, we immediately recheck the condition after being
			// notified.
			while (locked) {
				this.lockReleased.awaitUninterruptibly();
			}

			this.locked = true;
			this.lockCookie = RND.nextLong();

			return this.lockCookie;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Releases this lock.
	 * <p>
	 * If other threads are waiting for this lock, one of them is notified to
	 * retry to aquire this lock.
	 * 
	 * @param lockCookie
	 *            cookie value to authorize the release
	 */
	public void release(long lockCookie) throws SecurityException {
		lock.lock();

		try {
			if (this.lockCookie != lockCookie) {
				throw new SecurityException("Invalid lock cookie");
			} else {
				this.locked = false;
				this.lockCookie = 0;

				// notify waiting threads about this lock's release
				this.lockReleased.signal();
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Returns the current cookie value.
	 * 
	 * @return cookie value
	 */
	public long getLockCookie() {
		return this.lockCookie;
	}
}
