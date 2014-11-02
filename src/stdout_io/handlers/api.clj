(ns stdout-io.handlers.api
  (:require [taoensso.carmine :as car :refer (wcar)]
            [clojure.data.json :as json]))

(defmacro wcar* [& body] `(car/wcar {:pool {} :spec {}} ~@body))

(defn get-logs [id]
  (wcar* (car/get id)))

(defn write-logs [id lines]
  (wcar* (car/set id (into (get-logs id) lines))))

(defn get-logs-handler [req]
  (get-logs (-> req :params :id)))

(defn write-logs-handler [req]
  (write-logs (-> req :params :id) (json/read-str (slurp (-> req :body)))))
