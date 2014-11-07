# Project Name


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


## License

Copyright (C) 2014 Joscha Meyer

Double licensed under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html) (the same as Clojure) or
the [Apache Public License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).
