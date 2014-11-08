# Project Name

Currently running on http://130.211.91.62:8080/api/logs/ feel free to experiment.

## Development

stdout-io uses [Leiningen
2](https://github.com/technomancy/leiningen/blob/master/doc/TUTORIAL.md).

```sh
service redis-server start # make sure redis is running and installed
./scripts/run # start the webserver
```

```sh
curl -XPOST -H'Content-type: application/json' -d'["foo","bar"]' localhost:8080/api/logs/test
curl -v localhost:8080/api/logs/test # fetch logs, connection will be closed after 10 seconds
```

```sh
# post this while fetching logs to see pub sub in action
curl -XPOST -H'Content-type: application/json' -d'["blaaah","blub"]' localhost:8080/api/logs/test
```

## Setup local docker images

```sh
lein uberimage # build docker image with uberimage https://github.com/palletops/lein-uberimage

docker run --name some-redis -d redis # start redis docker container https://registry.hub.docker.com/_/redis/

# link the redis container and run the uberimage
docker run -i -t --link some-redis:redis -p 8080:8080 <uberimage-id> /bin/bash -c '/usr/bin/java -jar /uberjar.jar --redis-host "$REDIS_PORT_6379_TCP_ADDR" --redis-port $REDIS_PORT_6379_TCP_PORT'
```

## License

Copyright (C) 2014 Joscha Meyer

Double licensed under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html) (the same as Clojure) or
the [Apache Public License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).
