(ns lein-survey.results
  (:require [clojure.java.jdbc :as sql]
            [clojure.pprint :as pprint]
            [lein-survey.questions :as q]))

(defn setize [x]
  (if (coll? x)
    (set x)
    (hash-set x)))

(defn merge-results [{:keys [id body timestamp]}]
  (assoc (read-string body) :id id :timestamp (.getTime timestamp)))

(defn results-str []
  (sql/with-connection (or (System/getenv "DATABASE_URL")
                           "postgres://localhost:5432/lein-survey")
    (sql/with-query-results results ["select * from answers"]
      (doall (map (comp pr-str merge-results) results)))))

(def results-url (java.net.URL. "http://lein-survey.herokuapp.com/results.clj"))

(defonce get-results
  (memoize (fn [] (read-string (slurp results-url)))))

(defn total []
  (clojure.java.jdbc/with-connection (System/getenv "DATABASE_URL")
    (clojure.java.jdbc/with-query-results res ["select count(*) from answers"]
      (println res))))