# CDC Router

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green)
![Debezium](https://img.shields.io/badge/Debezium-Embedded-blue)

**Repository:** [https://github.com/truong-ngo/personal-base-cdc_router](https://github.com/truong-ngo/personal-base-cdc_router)

**CDC Router** là một hệ thống Change Data Capture (CDC) nhẹ, hiệu năng cao, được xây dựng dựa trên Debezium Embedded. Hệ thống này bắt các thay đổi dữ liệu (row-level changes) từ cơ sở dữ liệu nguồn (MySQL) và định tuyến chúng đến nhiều đích khác nhau (Sinks) dựa trên các quy tắc cấu hình linh hoạt.

## 🚀 Tính Năng Chính

*   **Embedded Engine**: Chạy Debezium trực tiếp bên trong ứng dụng (không cần cụm Kafka Connect riêng biệt).
*   **Định Tuyến Thông Minh (Smart Routing)**: Định tuyến sự kiện dựa trên tên bảng và điều kiện động (biểu thức SpEL).
*   **Khả Năng Chịu Lỗi (Fault Tolerance)**:
    *   Cấu hình **Cơ chế Retry** cho từng sink (Exponential backoff).
    *   Kiểm tra cấu hình **Fail-Fast** ngay khi khởi động.
    *   Đảm bảo giao nhận **At-Least-Once** (Xử lý Sink đồng bộ).
*   **Tính Sẵn Sàng Cao (High Availability)**: Hỗ trợ giao diện Distributed Lock, sẵn sàng cho triển khai đa node.
*   **Khả Năng Mở Rộng (Extensible)**: Kiến trúc dạng plugin, dễ dàng thêm Sink mới hoặc Config Provider mới.

## 🏗 Cấu Trúc Dự Án

Dự án được tổ chức theo mô hình multi-module Maven:

*   **`cdc-router-core`**: Chứa các domain model dùng chung, các interface SPI (`CdcSink`, `CdcRouter`), và các exception.
*   **`cdc-router-engine`**: Logic cốt lõi để quản lý vòng đời (`ConnectorOrchestrator`), tải cấu hình, và định tuyến sự kiện.
*   **`cdc-router-engine-debezium`**: Triển khai Source Engine sử dụng Debezium.
*   **`cdc-router-sink`**: Các triển khai của Sink (Kafka, Console) và Factory.
*   **`source-application`**: Ứng dụng Spring Boot mẫu để sinh dữ liệu thay đổi trong MySQL.

## ⚙️ Cấu Hình (`connectors.json`)

Mặc định, cấu hình được tải từ file `config/connectors.json`.

```json
[
  {
    "connectorName": "mysql-source-1",
    "sourceProperties": {
      "connector.type": "mysql",
      "database.hostname": "localhost",
      "database.port": "3306",
      "database.user": "root",
      "database.password": "root",
      "database.server.id": "1001",
      "database.server.name": "dbserver1",
      "database.include.list": "source_db",
      "schema.history.internal": "io.debezium.storage.file.history.FileSchemaHistory",
      "schema.history.internal.file.filename": "./history.dat"
    },
    "sinks": {
      "source_db.notes": [
        {
          "name": "kafka-sink-notes",
          "type": "kafka",
          "condition": "#event.operation == 'c' || #event.operation == 'u'",
          "maxRetries": 3,
          "retryWaitMillis": 2000,
          "properties": {
            "topic": "cdc-notes-topic"
          }
        },
        {
          "name": "console-debug",
          "type": "console",
          "properties": {
            "level": "DEBUG"
          }
        }
      ]
    }
  }
]
```

### Giải Thích Các Trường
*   **`condition`**: Biểu thức SpEL để lọc sự kiện. Ví dụ: `#event.operation == 'd'` (chỉ lấy sự kiện xóa).
*   **`maxRetries`**: Số lần tối đa thử gửi lại dữ liệu đến sink trước khi dừng engine.
*   **`retryWaitMillis`**: Thời gian chờ giữa các lần thử lại (mili giây).

## 🛠 Hướng Dẫn Chạy (Getting Started)

### 1. Yêu cầu hệ thống
*   Java 17
*   Docker & Docker Compose

### 2. Khởi động hạ tầng
Chạy Kafka, Zookeeper, và MySQL:
```bash
cd kafka-server
docker-compose up -d
```

### 3. Build dự án
```bash
./mvnw clean install
```

### 4. Chạy CDC Router
```bash
java -jar cdc-router-engine-debezium/target/cdc-router-engine-debezium-1.0.0-exec.jar
```

## 🔌 Cách Thêm Sink Mới

1.  Tạo một class mới trong module `cdc-router-sink`.
2.  Implement interface `CdcSink`.
3.  Đánh dấu class bằng annotation `@Component`.

```java
@Component
public class MyCustomSink implements CdcSink {
    @Override
    public String getType() {
        return "custom";
    }

    @Override
    public void execute(Map<String, Object> props, CdcEventContext event) {
        // Logic xử lý của bạn ở đây
    }
}
```
4.  Sử dụng `"type": "custom"` trong file cấu hình JSON của bạn.

## 🛡 Khả Năng Chịu Lỗi & Tính Bất Biến (Idempotency)

*   **Chính sách Retry**: Nếu một sink gặp lỗi (ví dụ: Kafka bị down), router sẽ thử lại dựa trên `maxRetries`. Nếu tất cả các lần thử đều thất bại, **Engine sẽ Dừng lại** để ngăn chặn mất dữ liệu (Offset chưa được commit).
*   **Tính Bất Biến (Idempotency)**: Vì hệ thống đảm bảo giao nhận *At-Least-Once* (Ít nhất một lần), các consumer/sink phía sau phải có tính idempotent (xử lý được các sự kiện trùng lặp mà không gây sai lệch dữ liệu).
