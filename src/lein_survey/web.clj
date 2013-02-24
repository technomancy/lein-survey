(ns lein-survey.web
  (:require [lein-survey.render :as render]
            [lein-survey.questions :as q]
            [lein-survey.results :as results]
            [ring.adapter.jetty :as jetty]
            [clojure.java.jdbc :as sql]
            [clojure.java.io :as io]
            [ring.middleware.params :as params]))

(defn create-table []
  (sql/with-connection (or (System/getenv "DATABASE_URL")
                           "postgres://localhost:5432/lein-survey")
    (sql/create-table :answers
                      [:id :serial "PRIMARY KEY"]
                      [:body :text "NOT NULL"]
                      [:timestamp :timestamp "NOT NULL"
                       "DEFAULT CURRENT_TIMESTAMP"])))

(defn add-column []
  (sql/with-connection (or (System/getenv "DATABASE_URL")
                           "postgres://localhost:5432/lein-survey")
    (sql/do-commands "ALTER TABLE answers ADD COLUMN edition INTEGER")
    (sql/do-commands "UPDATE answers SET edition = 2012")))

(defn record [params]
  (sql/with-connection (or (System/getenv "DATABASE_URL")
                           "postgres://localhost:5432/lein-survey")
    (sql/insert-values :answers [:body :edition] [(pr-str params) 2013]))
  (render/layout [:h1 "Thank you!"]))

(defn handler [req]
  (cond (= :post (:request-method req))
        {:status 201
         :headers {"Content-type" "text/html"}
         :body (record (:params req))}
        (re-find #"^/.*\.png$" (:uri req))
        {:status 200
         :headers {"Content-type" "image/png"}
         :body (results/image (second (re-find #"^/(.*)\.png$" (:uri req))))}
        (= "/results.clj" (:uri req))
        {:status 200
         :headers {"Content-type" "application/x-clojure"}
         :body (results/results-str)}
        (= "/results/2012" (:uri req))
        {:status 200
         :headers {"Content-type" "text/html"}
         :body (render/layout (results/summary))}
        (= "/results" (:uri req))
        {:status 200
         :headers {"Content-type" "text/html"}
         :body (render/layout (results/summary))}
        ;; WTF wrap-resource; why are you trying to serve a directory?
        (re-find #"\.css$" (:uri req))
        {:status 200
         :headers {"Content-type" "text/css"}
         :body (slurp (io/resource (str "public/" (:uri req))))}
        (= "/" (:uri req))
        {:status 200
         :headers {"Content-type" "text/html"}
         :body (render/layout (render/questions-form q/questions))}))

(def app (-> handler
             params/wrap-params))

(defn -main []
  (let [port (Integer. (or (System/getenv "PORT") 5005))]
    (jetty/run-jetty #'app {:port port})))
