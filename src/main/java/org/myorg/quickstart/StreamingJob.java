/** Something profound. */
package org.myorg.quickstart;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.dropwizard.metrics.DropwizardMeterWrapper;
import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.Meter;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/** Starting point for a Flink streaming job using the DataStream API. */
public final class StreamingJob {

  public static final String DESTINATION_TOPIC = "destination";
  public static final String DESTINATION_ID = "destination-sink";
  public static final String SOURCE_TOPIC = "source";
  public static final String SOURCE_ID = "source-source";
  public static final String SOURCE_STREAM = "source-stream";
  public static final String MAP_FUNCTION = "map";

  private StreamingJob() {
    // prevents calls from subclass
    throw new UnsupportedOperationException();
  }

  public static void main(final String[] args) throws Exception {

    final JobConfig config = JobConfig.create();
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    var source = SourceFactory.getSource("Kafka", config);

    final KafkaSink<String> sink =
        KafkaSink.<String>builder()
            .setBootstrapServers(config.brokers())
            .setRecordSerializer(
                KafkaRecordSerializationSchema.builder()
                    .setTopic(DESTINATION_TOPIC)
                    .setValueSerializationSchema(new SimpleStringSchema())
                    .build())
            .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
            .setKafkaProducerConfig(config.producer())
            .build();

    var stream =
        env.fromSource(source, WatermarkStrategy.noWatermarks(), SOURCE_ID)
            .name(SOURCE_STREAM)
            .uid(SOURCE_STREAM);

    var mappedStream =
        stream
            .map(
                new RichMapFunction<String, String>() {
                  private transient Counter counter;
                  private transient Meter meter;

                  @Override
                  public void open(Configuration config) {
                    this.counter = getRuntimeContext().getMetricGroup().counter("wordCount");

                    com.codahale.metrics.Meter dropwizardMeter = new com.codahale.metrics.Meter();

                    this.meter =
                        getRuntimeContext()
                            .getMetricGroup()
                            .meter("wordMeter", new DropwizardMeterWrapper(dropwizardMeter));
                  }

                  @Override
                  public String map(String value) {
                    this.meter.markEvent();
                    this.counter.inc();
                    return value.toUpperCase();
                  }
                })
            .name(MAP_FUNCTION)
            .uid(MAP_FUNCTION);

    mappedStream.sinkTo(sink).name(DESTINATION_ID).uid(DESTINATION_ID);

    env.execute("Flink Streaming Java API Skeleton");
  }
}
