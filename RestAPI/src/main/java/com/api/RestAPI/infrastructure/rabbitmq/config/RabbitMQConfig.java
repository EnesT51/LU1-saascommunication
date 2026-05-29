package com.api.RestAPI.infrastructure.rabbitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String MESSAGE_EXCHANGE = "message.exchange";

    public static final String SWIFTSEND_QUEUE = "swiftsend.queue";
    public static final String SECUREPOST_QUEUE = "securepost.queue";
    public static final String LEGACYLINK_QUEUE = "legacylink.queue";
    public static final String ASYNCFLOW_QUEUE = "asyncflow.queue";

    public static final String PROVIDER_DLX = "provider.dlx";
    public static final String NOTIFICATIONS_DLQ = "notifications.dlq";
    public static final String SWIFTSEND_DLQ = "swiftsend.dlq";
    public static final String SECUREPOST_DLQ = "securepost.dlq";
    public static final String LEGACYLINK_DLQ = "legacylink.dlq";
    public static final String ASYNCFLOW_DLQ = "asyncflow.dlq";

    public static final String SWIFTSEND_ROUTING_KEY = "provider.swiftsend";
    public static final String SECUREPOST_ROUTING_KEY = "provider.securepost";
    public static final String LEGACYLINK_ROUTING_KEY = "provider.legacylink";
    public static final String ASYNCFLOW_ROUTING_KEY = "provider.asyncflow";

    @Bean
    public TopicExchange messageExchange() {
        return new TopicExchange(MESSAGE_EXCHANGE);
    }
    @Bean
    public TopicExchange providerDeadLetterExchange() {
        return new TopicExchange(PROVIDER_DLX);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public Queue swiftsendQueue() {
        return QueueBuilder.durable(SWIFTSEND_QUEUE)
                .withArgument("x-dead-letter-exchange", PROVIDER_DLX)
                .withArgument("x-dead-letter-routing-key", SWIFTSEND_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue securepostQueue() {
        return QueueBuilder.durable(SECUREPOST_QUEUE)
                .withArgument("x-dead-letter-exchange", PROVIDER_DLX)
                .withArgument("x-dead-letter-routing-key", SECUREPOST_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue legacylinkQueue() {
        return QueueBuilder.durable(LEGACYLINK_QUEUE)
                .withArgument("x-dead-letter-exchange", PROVIDER_DLX)
                .withArgument("x-dead-letter-routing-key", LEGACYLINK_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue asyncflowQueue() {
        return QueueBuilder.durable(ASYNCFLOW_QUEUE)
                .withArgument("x-dead-letter-exchange", PROVIDER_DLX)
                .withArgument("x-dead-letter-routing-key", ASYNCFLOW_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding swiftsendBinding() {
        return BindingBuilder
                .bind(swiftsendQueue())
                .to(messageExchange())
                .with(SWIFTSEND_ROUTING_KEY);
    }

    @Bean
    public Binding securepostBinding() {
        return BindingBuilder
                .bind(securepostQueue())
                .to(messageExchange())
                .with(SECUREPOST_ROUTING_KEY);
    }

    @Bean
    public Binding legacylinkBinding() {
        return BindingBuilder
                .bind(legacylinkQueue())
                .to(messageExchange())
                .with(LEGACYLINK_ROUTING_KEY);
    }

    @Bean
    public Binding asyncflowBinding() {
        return BindingBuilder
                .bind(asyncflowQueue())
                .to(messageExchange())
                .with(ASYNCFLOW_ROUTING_KEY);
    }
    @Bean
    public Queue notificationsDlq() {
        return QueueBuilder.durable(NOTIFICATIONS_DLQ).build();
    }

    @Bean
    public Queue swiftsendDlq() {
        return QueueBuilder.durable(SWIFTSEND_DLQ).build();
    }

    @Bean
    public Queue securepostDlq() {
        return QueueBuilder.durable(SECUREPOST_DLQ).build();
    }

    @Bean
    public Queue legacylinkDlq() {
        return QueueBuilder.durable(LEGACYLINK_DLQ).build();
    }

    @Bean
    public Queue asyncflowDlq() {
        return QueueBuilder.durable(ASYNCFLOW_DLQ).build();
    }
    @Bean
    public Binding swiftsendDlqBinding() {
        return BindingBuilder
                .bind(swiftsendDlq())
                .to(providerDeadLetterExchange())
                .with(SWIFTSEND_ROUTING_KEY);
    }

    @Bean
    public Binding securepostDlqBinding() {
        return BindingBuilder
                .bind(securepostDlq())
                .to(providerDeadLetterExchange())
                .with(SECUREPOST_ROUTING_KEY);
    }

    @Bean
    public Binding legacylinkDlqBinding() {
        return BindingBuilder
                .bind(legacylinkDlq())
                .to(providerDeadLetterExchange())
                .with(LEGACYLINK_ROUTING_KEY);
    }

    @Bean
    public Binding asyncflowDlqBinding() {
        return BindingBuilder
                .bind(asyncflowDlq())
                .to(providerDeadLetterExchange())
                .with(ASYNCFLOW_ROUTING_KEY);
    }
}