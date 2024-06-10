package org.myorg.quickstart;

import java.util.Random;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.connector.source.Source;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;
import org.apache.flink.connector.kafka.source.KafkaSource;

public class SourceFactory {
  public static final String SOURCE_TOPIC = "source";
  public static final String SOURCE_ID = "source-source";
  public static final String SOURCE_STREAM = "source-stream";

  public static Source getSource(String sourceType, JobConfig config) {
    if (sourceType == null) {
      return null;
    }
    if (sourceType.equalsIgnoreCase("Kafka")) {
      return KafkaSource.<String>builder()
          .setBootstrapServers(config.brokers())
          .setTopics(SOURCE_TOPIC)
          .setValueOnlyDeserializer(new SimpleStringSchema())
          .setProperties(config.consumer())
          .build();
    } else {

      GeneratorFunction<Long, String> generatorFunction =
          index -> {
            Random random = new Random();
            int leftLimit = 97; // letter 'a'
            int rightLimit = 122; // letter 'z'
            int targetStringLength = 10;
            return random
                .ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
          };

      DataGeneratorSource<String> source =
          new DataGeneratorSource<>(
              generatorFunction,
              Long.MAX_VALUE,
              RateLimiterStrategy.perSecond(
                  Double.valueOf(config.producer().getProperty("producer.rate", "1"))),
              Types.STRING);

      return source;
    }
  }
}
