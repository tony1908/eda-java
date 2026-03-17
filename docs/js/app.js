
    const schema = {
  "asyncapi": "3.0.0",
  "info": {
    "title": "Eventflow arquitectura orientada a eventos",
    "version": "1.0.0",
    "description": "Arquitectura orientada a eventos para la gestion de pedidos",
    "contact": {
      "name": "Tony",
      "email": "tony@gmail.com"
    },
    "license": {
      "name": "MIT"
    }
  },
  "defaultContentType": "application/json",
  "servers": {
    "rabbitmq-dev": {
      "host": "rabbitmq:5672",
      "protocol": "amqp",
      "description": "RabbitMQ server en dev",
      "security": [
        {
          "type": "userPassword",
          "description": "Credenciales de RabbitMQ"
        }
      ],
      "tags": [
        {
          "name": "development"
        }
      ]
    },
    "kafka-dev": {
      "host": "kafka:9092",
      "protocol": "kafka",
      "description": "Kafka server en dev",
      "tags": [
        {
          "name": "development"
        }
      ]
    }
  },
  "channels": {
    "orders.created": {
      "address": "orders.created",
      "description": "Evento de creacion de pedido",
      "messages": {
        "orderCreatedEvent": {
          "name": "orderCreatedEvent",
          "title": "Evento de creacion de pedido",
          "description": "Evento de creacion de pedido",
          "contentType": "application/json",
          "payload": {
            "type": "object",
            "properties": {
              "product": {
                "type": "string",
                "x-parser-schema-id": "<anonymous-schema-2>"
              },
              "price": {
                "type": "number",
                "x-parser-schema-id": "<anonymous-schema-3>"
              }
            },
            "x-parser-schema-id": "<anonymous-schema-1>"
          },
          "examples": [
            {
              "name": "laptopOrder",
              "summary": "Evento de creacion de pedido de laptop",
              "payload": {
                "product": "laptop",
                "price": 999.99
              }
            }
          ],
          "x-parser-unique-object-id": "orderCreatedEvent"
        }
      },
      "bindings": {
        "amqp": {
          "is": "routingKey",
          "exchange": {
            "name": "eventflow",
            "type": "topic",
            "durable": true
          }
        }
      },
      "x-parser-unique-object-id": "orders.created"
    },
    "orders.canceled": {
      "address": "orders.canceled",
      "description": "Evento de cancelacion de pedido",
      "messages": {
        "orderCanceledEvent": {
          "name": "orderCanceledEvent",
          "title": "Evento de cancelacion de pedido",
          "description": "Evento de cancelacion de pedido",
          "contentType": "application/json",
          "payload": {
            "type": "object",
            "properties": {
              "id": {
                "type": "string",
                "x-parser-schema-id": "<anonymous-schema-5>"
              },
              "reason": {
                "type": "string",
                "x-parser-schema-id": "<anonymous-schema-6>"
              }
            },
            "x-parser-schema-id": "<anonymous-schema-4>"
          },
          "examples": [
            {
              "name": "laptopOrderCanceled",
              "summary": "Evento de cancelacion de pedido de laptop",
              "payload": {
                "id": "uudhf",
                "reason": "Pedido cancelado"
              }
            }
          ],
          "x-parser-unique-object-id": "orderCanceledEvent"
        }
      },
      "bindings": {
        "amqp": {
          "is": "routingKey",
          "exchange": {
            "name": "eventflow",
            "type": "topic",
            "durable": true
          }
        }
      },
      "x-parser-unique-object-id": "orders.canceled"
    }
  },
  "operations": {
    "publishOrderCreated": {
      "action": "send",
      "channel": "$ref:$.channels.orders.created",
      "description": "Publica un evento de creacion de pedido",
      "tags": [
        {
          "name": "orders"
        }
      ],
      "x-parser-unique-object-id": "publishOrderCreated"
    },
    "publishOrderCanceled": {
      "action": "send",
      "channel": "$ref:$.channels.orders.canceled",
      "description": "Publica un evento de cancelacion de pedido",
      "tags": [
        {
          "name": "orders"
        }
      ],
      "x-parser-unique-object-id": "publishOrderCanceled"
    },
    "onOrderCreatedInventory": {
      "action": "receive",
      "channel": "$ref:$.channels.orders.created",
      "description": "Recibe un evento de creacion de pedido",
      "tags": [
        {
          "name": "inventory"
        }
      ],
      "x-parser-unique-object-id": "onOrderCreatedInventory"
    },
    "onOrderCanceledInventory": {
      "action": "receive",
      "channel": "$ref:$.channels.orders.canceled",
      "description": "Recibe un evento de cancelacion de pedido",
      "tags": [
        {
          "name": "inventory"
        }
      ],
      "x-parser-unique-object-id": "onOrderCanceledInventory"
    }
  },
  "components": {
    "messages": {
      "orderCreatedEvent": "$ref:$.channels.orders.created.messages.orderCreatedEvent",
      "orderCanceledEvent": "$ref:$.channels.orders.canceled.messages.orderCanceledEvent"
    },
    "schemas": {
      "securitySchemes": {
        "rabbitmqCredentials": "$ref:$.servers.rabbitmq-dev.security[0]",
        "x-parser-schema-id": "securitySchemes"
      }
    }
  },
  "x-parser-spec-parsed": true,
  "x-parser-api-version": 3,
  "x-parser-spec-stringified": true
};
    const config = {"show":{"sidebar":true},"sidebar":{"showOperations":"byDefault"}};
    const appRoot = document.getElementById('root');
    AsyncApiStandalone.render(
        { schema, config, }, appRoot
    );
  