(ns stdout-io.handlers.api
  (:use org.httpkit.server)
  (:require [taoensso.carmine :as car :refer (wcar)]
            [clojure.data.json :as json]))

(defmacro wcar* [& body] `(car/wcar {:pool {} :spec {}} ~@body))

(defn get-logs [id]
  (wcar* (car/get id)))

(defn write-logs [id lines]
  (wcar* (car/set id (into (get-logs id) lines))))

(defn wait-for-new-lines [id processor]
  (car/with-new-pubsub-listener {}
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
    ;  (if (seq new-lines)
    ;    (send! channel (json-ok new-lines)))
      (let [listener (wait-for-new-lines
                       id
                       (fn [new-lines]
                         (println "send " (apply str new-lines) " to " channel)
                         (send! channel (json-ok new-lines))))]

        (on-close channel (fn [status]
                            (car/close-listener listener)
                            (println "channel closed, " status)))
        ))))

(defn write-logs-handler [req]
  (let [id (-> req :params :id)
        newlines (json/read-str (slurp (-> req :body)))]
    (write-logs id newlines)
    (publish-new-lines id newlines))
    "OK")
