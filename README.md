# scheduler

Tasks Scheduler

Scheduler triggers tasks like sending emails, generating reports at the scheduled time. 

The service is part of the infrastructure.

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
