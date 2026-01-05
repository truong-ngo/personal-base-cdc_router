package vn.truongngo.base.cdc.router.core.exception;

/**
 * Exception thrown when there is a configuration error.
 * <p>
 * Ngoại lệ được ném ra khi có lỗi cấu hình.
 */
public class ConfigurationException extends RuntimeException {

    /**
     * Constructs a new ConfigurationException with the specified detail message.
     * <p>
     * Khởi tạo một ConfigurationException mới với thông báo chi tiết được chỉ định.
     *
     * @param message The detail message.
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructs a new ConfigurationException with the specified detail message and cause.
     * <p>
     * Khởi tạo một ConfigurationException mới với thông báo chi tiết và nguyên nhân được chỉ định.
     *
     * @param message The detail message.
     * @param cause   The cause.
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
