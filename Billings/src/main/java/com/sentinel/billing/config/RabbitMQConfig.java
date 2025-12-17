package com.sentinel.billing.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para Billing-Service.
 * 
 * Declara:
 * - Exchanges: billing-exchange, tenant-exchange, project-exchange
 * - Queues: billing-tenant-events-queue, billing-project-events-queue
 * - Bindings entre queues y exchanges
 */
@Configuration
public class RabbitMQConfig {

    // ==================== BILLING EXCHANGE ====================
    @Value("${billing.events.exchange:billing-exchange}")
    private String billingExchange;

    @Value("${billing.events.payment-succeeded-routing-key:billing.payment_succeeded}")
    private String paymentSucceededRoutingKey;

    @Value("${billing.events.payment-failed-routing-key:billing.payment_failed}")
    private String paymentFailedRoutingKey;

    // ==================== TENANT EXCHANGE ====================
    @Value("${billing.listeners.tenant.exchange:tenant-exchange}")
    private String tenantExchange;

    @Value("${billing.listeners.tenant.queue:billing-tenant-events-queue}")
    private String tenantQueue;

    @Value("${billing.listeners.tenant.plan-upgraded-routing-key:tenant.plan.upgraded}")
    private String tenantPlanUpgradedRoutingKey;

    // ==================== PROJECT EXCHANGE ====================
    @Value("${billing.listeners.project.exchange:project-exchange}")
    private String projectExchange;

    @Value("${billing.listeners.project.queue:billing-project-events-queue}")
    private String projectQueue;

    @Value("${billing.listeners.project.created-routing-key:project.created}")
    private String projectCreatedRoutingKey;

    // ==================== BILLING EXCHANGE DECLARATIONS ====================

    // ==================== BILLING EXCHANGE DECLARATIONS ====================

    @Bean
    public TopicExchange billingExchange() {
        return new TopicExchange(billingExchange, true, false);
    }

    // ==================== TENANT EXCHANGE & QUEUE ====================

    @Bean
    public TopicExchange tenantExchange() {
        return new TopicExchange(tenantExchange, true, false);
    }

    // ==================== PROJECT EXCHANGE & QUEUE ====================

    @Bean
    public TopicExchange projectExchange() {
        return new TopicExchange(projectExchange, true, false);
    }

    // ==================== JSON CONVERTER + RABBIT TEMPLATE ====================

    @Bean
    public MessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        org.springframework.amqp.support.converter.DefaultClassMapper classMapper = new org.springframework.amqp.support.converter.DefaultClassMapper();
        classMapper.setTrustedPackages("*");
        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
