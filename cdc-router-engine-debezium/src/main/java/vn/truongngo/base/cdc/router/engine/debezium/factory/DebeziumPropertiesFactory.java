package vn.truongngo.base.cdc.router.engine.debezium.factory;

import vn.truongngo.base.cdc.router.core.exception.ConfigurationException;
import vn.truongngo.base.cdc.router.core.model.config.CdcConnectorConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DebeziumPropertiesFactory {

    // Map tên ngắn -> Tên class đầy đủ (Dạng String để tránh lỗi compile khi thiếu jar)
    private static final Map<String, String> CONNECTOR_CLASS_MAP = new HashMap<>();

    static {
        // Hiện tại chỉ có MySQL
        CONNECTOR_CLASS_MAP.put("mysql", "io.debezium.connector.mysql.MySqlConnector");

        // Sau này chỉ cần uncomment dòng dưới và thêm dependency là chạy được Postgres
        // CONNECTOR_CLASS_MAP.put("postgres", "io.debezium.connector.postgresql.PostgresConnector");
    }

    public static Properties createProperties(CdcConnectorConfig config) {
        Properties props = new Properties();

        // 1. Nạp config từ JSON (Lưu ý: Trong này NÊN có 'bootstrap.servers' nếu config chung)
        if (config.getSourceProperties() != null) {
            props.putAll(config.getSourceProperties());
        }

        // 2. Resolve Class Connector (Giữ nguyên)
        String type = (String) props.getOrDefault("connector.type", "mysql");
        if (!props.containsKey("connector.class")) {
            String className = CONNECTOR_CLASS_MAP.get(type);
            if (className == null) {
                throw new ConfigurationException("Unsupported connector type: " + type);
            }
            props.setProperty("connector.class", className);
        }

        props.setProperty("name", config.getConnectorName());

        // -----------------------------------------------------------------------
        // 3. THAY ĐỔI: Cấu hình Kafka Offset Storage
        // -----------------------------------------------------------------------

        // Class quản lý việc lưu offset vào Kafka
        props.put("offset.storage", "org.apache.kafka.connect.storage.KafkaOffsetBackingStore");

        // Cấu hình Topic để lưu offset
        // Nếu bạn muốn mỗi connector 1 topic riêng thì thêm tên connector vào,
        // còn chuẩn Kafka Connect thì dùng chung 1 topic "connect-offsets"
        String offsetTopic = "connect-offsets-" + config.getConnectorName(); // Hoặc lấy từ config
        props.put("offset.storage.topic", offsetTopic);

        // Số lượng partitions cho topic offset (Thường là 25 cho cụm lớn, hoặc 3 cho cụm nhỏ)
        props.putIfAbsent("offset.storage.partitions", "1");

        // Replication Factor (Quan trọng: Production nên để >= 3, Dev để 1)
        props.putIfAbsent("offset.storage.replication.factor", "1");

        // QUAN TRỌNG: Phải có địa chỉ Kafka Broker để connect
        // Kiểm tra xem trong props đã có 'bootstrap.servers' chưa, nếu chưa thì phải set
//        if (!props.containsKey("bootstrap.servers")) {
//            // Giả sử class config của bạn có field này, hoặc bạn hardcode, hoặc lấy từ env
//            // props.put("bootstrap.servers", "localhost:9092");
//            throw new ConfigurationException("Missing 'bootstrap.servers' for KafkaOffsetBackingStore");
//        }

        // -----------------------------------------------------------------------

        // Internal Converters (Giữ nguyên)
        props.setProperty("key.converter", "org.apache.kafka.connect.json.JsonConverter");
        props.setProperty("value.converter", "org.apache.kafka.connect.json.JsonConverter");
        props.setProperty("key.converter.schemas.enable", "false");
        props.setProperty("value.converter.schemas.enable", "false");

        return props;
    }
}
