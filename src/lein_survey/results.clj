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
      (pr-str (map merge-results results)))))

(def results-url (java.net.URL. "http://lein-survey.herokuapp.com/results.clj"))

(defonce get-results
  (memoize (fn [] (read-string (slurp results-url)))))

(defn total []
  (clojure.java.jdbc/with-connection (System/getenv "DATABASE_URL")
    (clojure.java.jdbc/with-query-results res ["select count(*) from answers"]
      (println res))))

(defmulti summarize-question (fn [answers question] (second question)))

(defmethod summarize-question :radio [answers [q _ choices]]
  (let [freqs (frequencies (for [r results] (get r q)))]
    [:div.answer
     [:h4.question q]
     [:dl (apply concat (for [choice choices]
                          [[:dt choice] [:dd (freqs choice)]]))]]))

(defmethod summarize-question :check [answers [q _ choices]]
  (let [answers (apply concat (for [r results] (setize (get r q))))
        freqs (frequencies answers)]
    [:div.answer
     [:h4.question q]
     [:dl (apply concat (for [choice choices]
                          [[:dt choice] [:dd (freqs choice)]]))]]))

(defmethod summarize-question :textarea [answers [q _ choices]])

(defmethod summarize-question :rank [answers [q _ choices]]
  (let [freqs #(sort-by (comp first key)
                        (frequencies (for [r results]
                                       (setize (get r (str q " " %))))))]
    [:div.answer
     [:h4.question q]
     [:ul (for [choice choices]
            [:li choice
             (for [[rank count] (freqs choice)
                   :when (not= rank #{nil})]
               (str " | " (first rank) " - " count))])]]))



(defn summary []
  (let [results (get-results)]
    (into [:div.summary]
          (map (partial summarize-question results) q/questions))))