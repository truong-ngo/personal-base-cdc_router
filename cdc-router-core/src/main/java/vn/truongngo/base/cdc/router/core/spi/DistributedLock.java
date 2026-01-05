package vn.truongngo.base.cdc.router.core.spi;

/**
 * Interface for distributed locking mechanism.
 * <p>
 * Giao diện cho cơ chế khóa phân tán.
 */
public interface DistributedLock {
    /**
     * Attempts to acquire a lock.
     * <p>
     * Cố gắng lấy khóa.
     *
     * @param lockKey      The unique key for the lock.
     *                     <p>Khóa duy nhất cho lock.</p>
     * @param expireMillis The expiration time in milliseconds.
     *                     <p>Thời gian hết hạn tính bằng mili giây.</p>
     * @return true if the lock was acquired, false otherwise.
     *         <p>true nếu lấy được khóa, ngược lại là false.</p>
     */
    boolean tryLock(String lockKey, long expireMillis);

    /**
     * Refreshes the expiration time of an existing lock.
     * <p>
     * Làm mới thời gian hết hạn của một khóa hiện có.
     *
     * @param lockKey      The unique key for the lock.
     *                     <p>Khóa duy nhất cho lock.</p>
     * @param expireMillis The new expiration time in milliseconds.
     *                     <p>Thời gian hết hạn mới tính bằng mili giây.</p>
     * @return true if the lock was refreshed, false otherwise.
     *         <p>true nếu khóa được làm mới, ngược lại là false.</p>
     */
    boolean refreshLock(String lockKey, long expireMillis);

    /**
     * Releases the lock.
     * <p>
     * Giải phóng khóa.
     *
     * @param lockKey The unique key for the lock.
     *                <p>Khóa duy nhất cho lock.</p>
     */
    void unlock(String lockKey);
}
