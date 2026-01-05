package vn.truongngo.base.cdc.router.core.exception;

import lombok.Getter;

/**
 * Exception thrown when a Sink encounters an error during execution.
 * <p>
 * Ngoại lệ được ném ra khi một Sink gặp lỗi trong quá trình thực thi.
 */
@Getter
public class SinkExecutionException extends RuntimeException {

    /**
     * Indicates whether the operation should be retried.
     * <p>
     * Chỉ định xem thao tác có nên được thử lại hay không.
     * <p>
     * If true: The Router will attempt a retry mechanism (e.g., exponential backoff).
     * <br>
     * Nếu true: Router sẽ thực hiện cơ chế thử lại (ví dụ: backoff theo cấp số nhân).
     * <p>
     * If false: The Router will skip (or write to a Dead Letter Queue) and proceed.
     * <br>
     * Nếu false: Router sẽ bỏ qua (hoặc ghi vào Dead Letter Queue) và tiếp tục.
     */
    private final boolean retryable;

    /**
     * Constructs a new SinkExecutionException.
     * <p>
     * Khởi tạo một SinkExecutionException mới.
     *
     * @param message   The detail message.
     * @param retryable Whether the error is retryable.
     */
    public SinkExecutionException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }

    /**
     * Constructs a new SinkExecutionException with a cause.
     * <p>
     * Khởi tạo một SinkExecutionException mới với nguyên nhân.
     *
     * @param message   The detail message.
     * @param cause     The cause.
     * @param retryable Whether the error is retryable.
     */
    public SinkExecutionException(String message, Throwable cause, boolean retryable) {
        super(message, cause);
        this.retryable = retryable;
    }

    /**
     * Constructs a new SinkExecutionException that is retryable by default.
     * <p>
     * Khởi tạo một SinkExecutionException mới mặc định có thể thử lại.
     *
     * @param message The detail message.
     * @param cause   The cause.
     */
    public SinkExecutionException(String message, Throwable cause) {
        super(message, cause);
        this.retryable = true;
    }
}
