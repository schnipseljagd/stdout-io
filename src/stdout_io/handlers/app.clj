(ns stdout-io.handlers.app
  (:require [stdout-io.tmpls :as tmpl]))

(defn show-landing [req]
  (tmpl/landing {:user-agent (get-in req [:headers "user-agent"])
                 :title "TODO: your app title"
                 :list ["list item 1"
                        "list item 2"]}))
