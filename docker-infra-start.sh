docker compose -f ./docker-infra/docker-compose-database.yaml -f ./docker-infra/docker-compose-broker.yaml up -d
docker compose -f ./cloud/docker-compose-cloud.yaml up -d
docker exec -it rabbitmq-2 rabbitmqctl stop_app
docker exec -it rabbitmq-2 rabbitmqctl reset
docker exec -it rabbitmq-2 rabbitmqctl join_cluster rabbit@rabbitmq-1
docker exec -it rabbitmq-2 rabbitmqctl start_app