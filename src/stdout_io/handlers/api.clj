(ns stdout-io.handlers.api
  (:use [org.httpkit.server]
        [org.httpkit.timer]
        [stdout-io.config :only [cfg]])
  (:require [taoensso.carmine :as car :refer (wcar)]
            [clojure.data.json :as json]))

(defn redis-server-conn [] {:pool {} :spec {:host (cfg :redis-host) :port (cfg :redis-port)}})

(defmacro wcar* [& body] `(car/wcar (redis-server-conn) ~@body))

(defn get-logs [id]
  (wcar* (car/get id)))

(defn write-logs [id lines]
  (wcar* (car/set id (into (get-logs id) lines))))

(defn wait-for-new-lines [id processor]
  (car/with-new-pubsub-listener (:spec (redis-server-conn))
    {id (fn [msg] (prn id msg) (if (= (first msg) "message") (processor (nth msg 2))))
     "*" (fn [msg] (prn id msg))}
    (car/subscribe id)
    (car/psubscribe "*")))

(defn publish-new-lines [id newlines]
  (wcar* (car/publish id newlines)))

(defn json-ok [data]
  {:status 200
   :headers {"Content-Type" "application/json; charset=utf-8"}
   :body (json/write-str data)})

(defn get-logs-handler [req]
  (let [id (-> req :params :id)
        new-lines (get-logs id)]
    (with-channel req channel
      (if (seq new-lines)
        (send! channel (json-ok new-lines) false))
      (let [listener (wait-for-new-lines
                       id
                       (fn [new-lines]
                         (send! channel (json-ok new-lines) false)))]
        (on-close channel (fn [status]
                            (car/close-listener listener)
                            (println "channel closed, " status))))
      (schedule-task 10000 (close channel))
      )))

(defn write-logs-handler [req]
  (let [id (-> req :params :id)
        newlines (json/read-str (slurp (-> req :body)))]
    (write-logs id newlines)
    (publish-new-lines id newlines))
    "OK")
