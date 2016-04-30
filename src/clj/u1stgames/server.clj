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
            [clojure.data.json :as json]
            [clojure.walk :as walk])
  (:use ring.middleware.edn)
  (:gen-class))

(defn new-uuid []
  (str (java.util.UUID/randomUUID)))

(defn numeric? [s]
  (if-let [s (seq s)]
    (let [s (if (= (first s) \-) (next s) s)
          s (drop-while #(Character/isDigit %) s)
          s (if (= (first s) \.) (next s) s)
          s (drop-while #(Character/isDigit %) s)]
      (empty? s))))

(def graph-base-string "https://graph.facebook.com/1557991804501532?fields=picture&amp;access_token=240902319579455f6c7c8504a5566f0c491c79b46c8bba8")

(defn graph-app-string [appid]
  (str
    "https://graph.facebook.com/"
    appid
    "?fields=picture,subcategory,monthly_active_users&amp;access_token=240902319579455f6c7c8504a5566f0c491c79b46c8bba8"))

(def mysql-db {:subprotocol "mysql"
               :subname "//173.230.153.62:3306/fortunecookies"
               :user "root"
               :password "naLdC28LCyAMVqq2"})

(defn generate-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(defn fb-games []
  (->>
    (filter
      #(do
        #_(not (= "" (:appid %)))
        (numeric? (:appid %))
        )
      (j/query mysql-db ["SELECT title,appid,ytvideo,pics from games"]))
    (map
      #(update-in % [:pics] (fn [x]
                              (map
                                (fn [url] {:url url :uid (new-uuid)})
                                (clojure.string/split x #",") ))))))

(def q-result (j/query mysql-db ["SELECT title,appid,ytvideo,pics from games"]))

(comment
 (first q-result)
 (:appid (first q-result))
 (:appid (first q-result))
 (numeric? (:appid (first q-result)))
 (type (:appid (first q-result)))
 (fb-games)
 )

(defn app-data [appid]
  (let []
    (->
      (:body (client/get (graph-app-string appid)))
      (json/read-str)
      (walk/keywordize-keys))))

(defn flattened-app-data [appid]
  (let [app-data-res (app-data appid)
        pic-url (get-in app-data-res [:picture :data :url])]
    (merge {:picture pic-url} (dissoc app-data-res :picture))))

(def all-data-atom (atom []))

(defn update-all-data []
  (reset!
    all-data-atom
    (->
        (map
          (fn [{:keys [title appid ytvideo] :as data}]
            (println title)
            (merge
              (try
                (flattened-app-data appid)
                (catch Exception e (do
                                     (str "caught exception: " (.getMessage e))
                                     {})))
              data
              {:type :game}))
          (fb-games)))))
(update-all-data)

(def test-atom (atom []))
(def test-sorted (atom []))

(comment

  (sort-by
      (fn [x]
        (int (:monthly_active_users x)))
      >
      @test-atom
      )

  (Integer. (:monthly_active_users (first @test-atom)))
  (Integer. "")

  (reset! test-atom (->
                      (map
                        (fn [{:keys [title appid ytvideo] :as data}]
                          (println title)
                          (merge
                            (try
                              (flattened-app-data appid)
                              (catch Exception e (do
                                                   (str "caught exception: " (.getMessage e))
                                                   {})))
                            data
                            {:type :game}))
                        (fb-games))))


  )

(defroutes routes
           (GET "/" _
             {:status 200
              :headers {"Content-Type" "text/html; charset=utf-8"}
              :body (io/input-stream (io/resource "public/index.html"))})

           (GET "/fbgames" []
             (generate-response @all-data-atom))

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


