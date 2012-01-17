(ns lein-survey.web
  (:require [lein-survey.render :as render]
            [lein-survey.questions :as q]
            [ring.adapter.jetty :as jetty]
            [clojure.java.jdbc :as sql]
            [ring.middleware.params :as params]
            [ring.middleware.resource :as resource]))

(defn create-table []
  (sql/with-connection (or (System/getenv "DATABASE_URL")
                           "postgres://localhost:5432/lein-survey")
    (sql/create-table :answers
                      [:id :serial "PRIMARY KEY"]
                      [:body :text "NOT NULL"]
                      [:timestamp :timestamp "NOT NULL"
                       "DEFAULT CURRENT_TIMESTAMP"])))

(defn record [params]
  (sql/with-connection (or (System/getenv "DATABASE_URL")
                           "postgres://localhost:5432/lein-survey")
    (sql/insert-values :answers [:body] [(pr-str params)]))
  (render/layout [:h1 "Thank you!"]))

(defn handler [req]
  (if (= :post (:request-method req))
    {:status 201
     :headers {"Content-type" "text/html"}
     :body (record (:params req))}
    {:status 200
     :headers {"Content-type" "text/html"}
     :body (render/layout (render/questions-form q/questions))}))

(def app (-> handler
             params/wrap-params
             (resource/wrap-resource "/public")))

(defn -main []
  (let [port (Integer. (or (System/getenv "PORT") 5000))]
    (jetty/run-jetty #'app {:port port})))

(defn total []
  (clojure.java.jdbc/with-connection (or (System/getenv "DATABASE_URL")
                                         "postgres://localhost:5432/lein-survey")
    (clojure.java.jdbc/with-query-results res ["select count(*) from answers"]
      (println res))))