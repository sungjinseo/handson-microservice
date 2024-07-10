docker compose -f ./docker-infra/docker-compose-database.yaml -f ./docker-infra/docker-compose-broker.yaml down
docker compose -f ./cloud/docker-compose-cloud.yaml down