package com.sentinel.results_aggregator_service.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.queue.scan-results}")
    private String scanResultsQueue;

    @Value("${app.rabbitmq.exchange.scan}")
    private String scanExchange;

    @Value("${app.rabbitmq.routing-key.scan-completed}")
    private String scanCompletedRoutingKey;

    @Value("${app.rabbitmq.routing-key.scan-failed}")
    private String scanFailedRoutingKey;

    @Bean
    public Queue resultsQueue() {
        return new Queue(scanResultsQueue, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(scanExchange);
    }

    @Bean
    public Binding bindingCompleted(Queue resultsQueue, TopicExchange exchange) {
        return BindingBuilder.bind(resultsQueue).to(exchange).with(scanCompletedRoutingKey);
    }

    @Bean
    public Binding bindingFailed(Queue resultsQueue, TopicExchange exchange) {
        return BindingBuilder.bind(resultsQueue).to(exchange).with(scanFailedRoutingKey);
    }
}
