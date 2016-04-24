(ns u1stgames.server
  (:require [clojure.java.io :as io]
            [compojure.core :refer [ANY GET PUT POST DELETE defroutes]]
            [compojure.route :refer [resources]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [environ.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]
            [clojure.java.jdbc :as j]
            [ajax.core :as jx]
            [clj-http.client :as client]
            [clojure.data.json :as json])
  (:use ring.middleware.edn)
  (:gen-class))

(def graph-base-string "https://graph.facebook.com/1557991804501532?fields=picture&amp;access_token=240902319579455f6c7c8504a5566f0c491c79b46c8bba8")

(def mysql-db {:subprotocol "mysql"
                 :subname "//173.230.153.62:3306/fortunecookies"
                 :user "root"
                 :password "naLdC28LCyAMVqq2"})

(defn generate-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(defn fb-games []
  (filter
    #(not (= "" (:appid %)))
    (j/query mysql-db ["SELECT title,appid from games"])))

(comment

  (read-string (:body (client/get graph-base-string)))
  (client/get graph-base-string)
  (:body (client/get graph-base-string))
  (type (:body (client/get graph-base-string)) )

  (read-string "{}")

  (slurp @response)

  (json/read-string (:body (client/get graph-base-string)))

  )

(defroutes routes
           (GET "/" _
             {:status 200
              :headers {"Content-Type" "text/html; charset=utf-8"}
              :body (io/input-stream (io/resource "public/index.html"))})

           (GET "/fbgames" []
             (generate-response (fb-games)))

           (resources "/"))

(def http-handler
  (-> routes
      (wrap-defaults api-defaults)
      wrap-with-logger
      wrap-gzip
      wrap-edn-params))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (run-jetty http-handler {:port port :join? false})))


