(ns lein-survey.web
  (:require [lein-survey.render :as render]
            [lein-survey.questions :as q]
            [lein-survey.results :as results]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as res]
            [clojure.java.jdbc :as sql]
            [clojure.java.io :as io]
            [ring.middleware.params :as params])
  (:gen-class))

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
  (render/layout [:div
                  [:h1 "Thank you!"]
                  [:p "Check back in a few weeks to see the results."]]))

(defn handler [req]
  (cond ;; (= :post (:request-method req))
        ;; {:status 201
        ;;  :headers {"Content-type" "text/html"}
        ;;  :body (record (:params req))}
        (re-find #"^/.*\.png$" (:uri req))
        {:status 200
         :headers {"Content-type" "image/png"}
         :body (results/image (second (re-find #"^/(.*)\.png$" (:uri req))))}
        (= "/results.clj" (:uri req))
        {:status 200
         :headers {"Content-type" "application/x-clojure"}
         :body (results/results-str)}
        (= "/comments.txt" (:uri req))
        {:status 200
         :headers {"Content-type" "text/plain"}
         :body (results/comments)}
        (= "/results" (:uri req))
        {:status 200
         :headers {"Content-type" "text/html"}
         :body (render/layout (results/summary))}
        (re-find #"\.css$" (:uri req))
        {:status 200
         :headers {"Content-type" "text/css"}
         :body (slurp (io/resource (str "public/" (subs (:uri req) 1))))}
        (= "/" (:uri req))
        (res/redirect "/results")
        #_{:status 200
         :headers {"Content-type" "text/html"}
         :body (render/layout (render/questions-form q/questions))}))

(def app (-> handler
             params/wrap-params))

(defn -main []
  (let [port (Integer. (or (System/getenv "PORT") 5005))]
    (jetty/run-jetty #'app {:port port :join? false})))

;; (def server (-main))
