package com.sentinel.scaner_orchestrator_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange.scan}")
    private String scanExchange;

    @Value("${app.rabbitmq.queue.scan-events}")
    private String scanEventsQueue;

    // Exchange
    @Bean
    public TopicExchange scanExchange() {
        return new TopicExchange(scanExchange);
    }

    // Consumer Queue
    @Bean
    public Queue scanEventsQueue() {
        return new Queue(scanEventsQueue, true);
    }

    @Bean
    public Binding bindingProgress(Queue scanEventsQueue, TopicExchange scanExchange) {
        return BindingBuilder.bind(scanEventsQueue).to(scanExchange).with("scan.progress.#");
    }

    @Bean
    public Binding bindingCompleted(Queue scanEventsQueue, TopicExchange scanExchange) {
        return BindingBuilder.bind(scanEventsQueue).to(scanExchange).with("scan.completed.#");
    }

    @Bean
    public Binding bindingFailed(Queue scanEventsQueue, TopicExchange scanExchange) {
        return BindingBuilder.bind(scanEventsQueue).to(scanExchange).with("scan.failed.#");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate adminRabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
