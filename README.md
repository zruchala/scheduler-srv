# Scheduler service

Scheduler triggers tasks like sending emails, generating reports at the scheduled time.

## Short description

The new tasks are being created and scheduled on receiving stream events (RabbitMQ). If they match to the `current hour` slot, 
they are scheduled within `TaskScheduler` immediately. Otherwise, their definition is sent to DB for later processing.
The task can be executed periodically (eg. once a week) or designed to be a single-use process.  

The service is part of the infrastructure.

Please note that the project is a skeleton to be developed the production solution.  

## Environment properties:

```
POSTGRES_URL=<database url> # jdbc:postgresql://db:5432/fv?gssEncMode=disable 
POSTGRES_USERNAME=<username>
POSTGRES_PASSWORD=<password>
POSTGRES_SCHEMA=<db schema> (optional)   

RABBITMQ_HOST=  
RABBITMQ_PORT=  
RABBITMQ_USERNAME=  
RABBITMQ_PASSWORD=  
RABBITMQ_VHOST=
```  

## Dependencies
