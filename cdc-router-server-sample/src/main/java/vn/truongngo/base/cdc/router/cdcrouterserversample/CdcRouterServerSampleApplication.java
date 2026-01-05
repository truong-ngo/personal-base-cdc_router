package vn.truongngo.base.cdc.router.cdcrouterserversample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import vn.truongngo.base.cdc.router.engine.debezium.config.DebeziumSourceConfiguration;
import vn.truongngo.base.cdc.router.sink.config.SinkModuleConfiguration;

@SpringBootApplication
@Import({
		SinkModuleConfiguration.class,
		DebeziumSourceConfiguration.class
})
public class CdcRouterServerSampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(CdcRouterServerSampleApplication.class, args);
	}

}
