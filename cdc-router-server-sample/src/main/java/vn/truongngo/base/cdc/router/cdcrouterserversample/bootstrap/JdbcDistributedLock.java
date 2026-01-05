package vn.truongngo.base.cdc.router.cdcrouterserversample.bootstrap;

import vn.truongngo.base.cdc.router.core.spi.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class JdbcDistributedLock implements DistributedLock {

    private final JdbcTemplate jdbcTemplate;

    private final String instanceId = UUID.randomUUID().toString();

    @Override
    public boolean tryLock(String lockKey, long expireMillis) {
        long now = System.currentTimeMillis();
        long expiresAt = now + expireMillis;

        try {
            // 1. Cố gắng INSERT một dòng khóa mới
            // Nếu chưa ai lock (khóa chính chưa tồn tại) -> Thành công
            String insertSql = "INSERT INTO cdc_distributed_locks (lock_key, locked_by, expires_at) VALUES (?, ?, ?)";
            jdbcTemplate.update(insertSql, lockKey, instanceId, expiresAt);
            return true;

        } catch (DuplicateKeyException e) {
            // 2. Nếu đã tồn tại (Duplicate Key), kiểm tra xem lock cũ có bị HẾT HẠN chưa?
            // Đây là cơ chế "Steal Lock" phòng trường hợp instance cũ bị crash mà chưa kịp nhả lock
            String updateIfExpiredSql = "UPDATE cdc_distributed_locks " +
                    "SET locked_by = ?, expires_at = ? " +
                    "WHERE lock_key = ? AND expires_at < ?";

            int rowsUpdated = jdbcTemplate.update(updateIfExpiredSql, instanceId, expiresAt, lockKey, now);

            // Nếu update được 1 dòng -> Tức là lock cũ đã hết hạn và ta đã chiếm được
            return rowsUpdated > 0;
        } catch (Exception e) {
            log.error("Error trying to acquire JDBC lock for key: {}", lockKey, e);
            return false;
        }
    }

    @Override
    public boolean refreshLock(String lockKey, long expireMillis) {
        long newExpiresAt = System.currentTimeMillis() + expireMillis;

        // Chỉ được phép gia hạn nếu chính mình (instanceId) đang giữ lock
        String sql = "UPDATE cdc_distributed_locks " +
                "SET expires_at = ? " +
                "WHERE lock_key = ? AND locked_by = ?";

        int rows = jdbcTemplate.update(sql, newExpiresAt, lockKey, instanceId);
        return rows > 0;
    }

    @Override
    public void unlock(String lockKey) {
        // Chỉ xóa lock nếu chính mình đang giữ
        String sql = "DELETE FROM cdc_distributed_locks WHERE lock_key = ? AND locked_by = ?";
        jdbcTemplate.update(sql, lockKey, instanceId);
    }
}