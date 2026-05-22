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

    public static final String SWIFTSEND_ROUTING_KEY = "provider.swiftsend";
    public static final String SECUREPOST_ROUTING_KEY = "provider.securepost";
    public static final String LEGACYLINK_ROUTING_KEY = "provider.legacylink";
    public static final String ASYNCFLOW_ROUTING_KEY = "provider.asyncflow";

    @Bean
    public TopicExchange messageExchange() {
        return new TopicExchange(MESSAGE_EXCHANGE);
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
        return QueueBuilder.durable(SWIFTSEND_QUEUE).build();
    }

    @Bean
    public Queue securepostQueue() {
        return QueueBuilder.durable(SECUREPOST_QUEUE).build();
    }

    @Bean
    public Queue legacylinkQueue() {
        return QueueBuilder.durable(LEGACYLINK_QUEUE).build();
    }

    @Bean
    public Queue asyncflowQueue() {
        return QueueBuilder.durable(ASYNCFLOW_QUEUE).build();
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
}