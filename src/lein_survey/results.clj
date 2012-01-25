(ns lein-survey.results
  (:require [clojure.java.jdbc :as sql]
            [clojure.pprint :as pprint]
            [clojure.string :as string]
            [incanter.core :as incanter]
            [incanter.charts :as charts]
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

(defmulti summarize-question (fn [results question] (second question)))

(defmethod summarize-question :radio [results [q _ choices]]
  (let [freqs (frequencies (for [r results] (get r q)))]
    [:div.answer
     [:h4.question q]
     [:dl (apply concat (for [choice choices]
                          [[:dt choice] [:dd (freqs choice)]]))]]))

(defmethod summarize-question :check [results [q _ choices]]
  (let [results-sets (apply concat (for [r results] (setize (get r q))))
        freqs (frequencies results-sets)]
    [:div.answer
     [:h4.question q]
     [:dl (apply concat (for [choice choices]
                          [[:dt choice] [:dd (freqs choice)]]))]]))

(defmethod summarize-question :textarea [results [q _ choices]])

(defmethod summarize-question :rank [results [q _ choices]]
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

(def os-map {"Debian/Ubuntu" :linux
             "Fedora/other RPM-based" :linux
             "Arch" :linux
             "Gentoo" :linux
             ;; "Nix" :other
             "Other GNU/Linux" :linux
             "Mac OS X with Homebrew" :mac
             "Mac OS X with Macports" :mac
             "Mac OS X with Fink" :mac
             "Mac OS X with no package manager" :mac
             "Windows with Powershell" :windows
             "Windows without Powershell" :windows
             "Windows with Cygwin" :windows
             ;; "Solaris" :other
             ;; "BSD" :other
             ;; "other" :other
             })

(defn os-lookup [q result]
  (let [q-result (get result q)
        result-set (if (coll? q-result)
                     (set q-result)
                     (hash-set q-result))]
    (set (for [[name type] os-map
               :when (result-set name)]
           type))))

(def pie-overrides {"Your OS and package manager(s)" os-lookup})

(defn pie
  ([q results] (pie q results (pie-overrides q (fn [q result] (get result q)))))
  ([q results lookup]
     (let [freqs (dissoc (frequencies (map (partial lookup q) results))
                         nil "I don't remember" #{})
           freqs (sort-by (comp str key) freqs)]
       (charts/pie-chart (map str (keys freqs)) (vals freqs)
                         :title q :legend true))))

(defn bar
  ([q results] (bar q results (fn [q result] (get result q))))
  ([q results lookup]
     (let [q-results (map (partial lookup q) results)
           q-results-spread (apply concat (for [r q-results]
                                            (if (coll? r) r [r])))
           freqs (dissoc (frequencies q-results-spread)
                         nil "I don't remember" #{} [] "")
           freqs (sort-by (comp str key) freqs)
           ;; TODO: make threshhold customizable?
           freqs (filter (fn [[x n]] (> n 3)) freqs)
           ]
       (charts/bar-chart (map str (keys freqs)) (vals freqs)
                         :title "Favourite Plugins" :vertical false))))

;; (incanter/view (bar "Favourite plugins? (comma-separated)" (get-results)
;;                     (fn [q result] (vec (.split (get result q) ", *")))))