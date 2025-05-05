package com.chargepoint.common.core.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions

@Configuration
class KafkaConfiguration {

    @Value("\${kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${kafka.consumer.group-id}")
    private lateinit var groupId: String

    @Value("\${kafka.consumer.topic}")
    private lateinit var consumerTopic: String

    @Value("\${kafka.producer.topic}")
     lateinit var producerTopic: String

    fun producerConfig(): Map<String, Any> {
        return mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all"
        )
    }

    fun consumerConfig(): Map<String, Any> {
        return mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.GROUP_ID_CONFIG to groupId
        )
    }

    @Bean
    fun kafkaSender(): KafkaSender<String, String> {
        val producerConfig = producerConfig()
        return KafkaSender.create(SenderOptions.create(producerConfig))
    }

    @Bean
    fun kafkaReceiver(): KafkaReceiver<String, String> {
        val consumerConfig = consumerConfig()
        val receiverOptions = ReceiverOptions.create<String, String>(consumerConfig)
            .subscription(listOf(consumerTopic))
        return KafkaReceiver.create(receiverOptions)
    }

}