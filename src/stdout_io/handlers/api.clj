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
    {id (fn [msg] (processor (nth msg 2)))}
    (car/subscribe id)))

(defn publish-new-lines [id newlines]
  (wcar* (car/publish id newlines)))

(defn json-ok [data]
  {:status 200
   :headers {"Content-Type" "application/json; charset=utf-8"}
   :body (json/write-str data)})

(defn get-logs-handler [req]
  (let [id (-> req :params :id)]
    (with-channel req channel
      (let [listener (wait-for-new-lines
                       id
                       (fn [newlines]
                         (send! channel (json-ok newlines))))]
        (on-close channel (fn [status] (car/close-listener listener)))))))

(defn write-logs-handler [req]
  (let [id (-> req :params :id)
        newlines (json/read-str (slurp (-> req :body)))]
    (write-logs id newlines)
    (publish-new-lines id newlines)))
