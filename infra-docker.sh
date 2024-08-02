if [[ $@ == *"build"* ]]
then
    echo "docker infra building..."
    docker compose -f ./docker-infra/docker-compose-database.yaml -f ./docker-infra/docker-compose-broker.yaml build
    docker compose -f ./cloud/docker-compose-cloud.yaml build
fi

if [[ $@ == *"up"* ]]
then
    echo "docker infra starting..."
    docker compose -f ./docker-infra/docker-compose-database.yaml -f ./docker-infra/docker-compose-broker.yaml up -d
    docker compose -f ./cloud/docker-compose-cloud.yaml up -d
fi

if [[ $@ == *"down"* ]]
then
    echo "docker infra shutting down..."
    docker compose -f ./docker-infra/docker-compose-database.yaml -f ./docker-infra/docker-compose-broker.yaml down
    docker compose -f ./cloud/docker-compose-cloud.yaml down
fi