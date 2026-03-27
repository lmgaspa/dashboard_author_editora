package rabbitmq

import (
	"fmt"

	amqp "github.com/rabbitmq/amqp091-go"
)

const (
	ExchangeName = "painel.events"
	ExchangeType = "topic"
)

// Connect establishes a connection and opens a channel to RabbitMQ.
func Connect(url string) (*amqp.Connection, *amqp.Channel, error) {
	conn, err := amqp.Dial(url)
	if err != nil {
		return nil, nil, fmt.Errorf("rabbitmq dial: %w", err)
	}

	ch, err := conn.Channel()
	if err != nil {
		conn.Close()
		return nil, nil, fmt.Errorf("rabbitmq open channel: %w", err)
	}

	if err := DeclareExchange(ch); err != nil {
		ch.Close()
		conn.Close()
		return nil, nil, err
	}

	return conn, ch, nil
}

// DeclareExchange declares the main topic exchange (idempotent).
func DeclareExchange(ch *amqp.Channel) error {
	return ch.ExchangeDeclare(
		ExchangeName,
		ExchangeType,
		true,  // durable
		false, // auto-deleted
		false, // internal
		false, // no-wait
		nil,
	)
}

// DeclareQueue declares a durable queue with an optional dead-letter exchange.
func DeclareQueue(ch *amqp.Channel, name string, dlxName string) (amqp.Queue, error) {
	args := amqp.Table{}
	if dlxName != "" {
		args["x-dead-letter-exchange"] = dlxName
	}
	return ch.QueueDeclare(
		name,
		true,  // durable
		false, // auto-delete
		false, // exclusive
		false, // no-wait
		args,
	)
}

// BindQueue binds a queue to the exchange with a routing key.
func BindQueue(ch *amqp.Channel, queueName, routingKey string) error {
	return ch.QueueBind(
		queueName,
		routingKey,
		ExchangeName,
		false,
		nil,
	)
}

// Publish publishes a message to the exchange with the given routing key.
func Publish(ch *amqp.Channel, routingKey string, body []byte) error {
	return ch.Publish(
		ExchangeName,
		routingKey,
		false, // mandatory
		false, // immediate
		amqp.Publishing{
			ContentType:  "application/json",
			DeliveryMode: amqp.Persistent,
			Body:         body,
		},
	)
}
