(ns lein-survey.results
  (:require [clojure.set] ; work around incanter bug
            [clojure.java.jdbc :as sql]
            [clojure.pprint :as pprint]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [incanter.core :as incanter]
            [incanter.charts :as charts]
            [lein-survey.questions :as q])
  (:import (org.apache.commons.codec.digest DigestUtils)))

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

(def results-url (java.net.URL. "http://lein-survey-2014.herokuapp.com/results.clj"))

(defonce get-results
  (memoize (fn [] (read-string (slurp results-url)))))

(defn hash-question [q]
  (subs (DigestUtils/shaHex q) 10))

(defn commentary [q]
  (if-let [c (io/resource (str "commentary/2014/" (hash-question q)))]
    [:p (slurp c)]))

(defn img-link [q]
  (format "/%s.png" (hash-question q)))

(defn percent-freqs [f choice results]
  (let [n (f choice)
        total (count results)]
    (format "%s (%s%%)" n (int (* 100 (/ n total))))))

(defmulti summarize-question (fn [results question] (second question)))

(defmethod summarize-question :radio [results [q _ choices]]
  (let [freqs (frequencies (for [r results] (get r q)))]
    [:div.answer
     [:img {:src (img-link q) :align "right"}]
     [:h4.question q]
     [:dl (apply concat (for [choice choices]
                          [[:dt choice] [:dd (percent-freqs
                                              freqs choice results)]]))]
     (commentary q)]))

(defmethod summarize-question :check [results [q _ choices]]
  (let [results-sets (apply concat (for [r results] (setize (get r q))))
        freqs (frequencies results-sets)]
    [:div.answer
     [:img {:src (img-link q) :align "right"}]
     [:h4.question q]
     [:dl (apply concat (for [choice choices]
                          [[:dt choice] [:dd (percent-freqs
                                              freqs choice results)]]))]
     (commentary q)]))

;;; summarizing plugins
;; (->> (mapcat #(.split % "[, ]+") p)
;;      (map #(string/replace % "lein-" ""))
;;      (map (memfn toLowerCase))
;;      (frequencies)
;;      (filter (comp pos? dec second))
;;      (sort-by second)
;;      (reverse)
;;      (rest)
;;      (pprint))

(defmethod summarize-question :textarea [results [q _ choices]]
  [:div.answer (slurp (io/resource (case q
                                     "Other comments?"
                                     "commentary/2014/other.html"
                                     "Favourite plugins? (comma-separated)"
                                     "commentary/2014/plugins.html"
                                     "Favourite templates? (comma-separated)"
                                     "commentary/2014/templates.html")))])

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
               (str " | " count))])]
     [:p "The chart's missing for this one on account of getting in a fight "
      "with Incanter's stacked bar chart and losing."]
     (commentary q)]))

(defn summary []
  (let [results (get-results)]
    (into [:div.summary
           [:h3 "Data and commentary on the results"]
           [:p "The survey ran from the 22nd of February to the 28th of March."

            " Most questions allowed more than one answer, so percentages"
            " will not add up to 100. At the time of this writing,"
            " (28 March) there were just over 500 responses."]
           [:p [:a {:href "http://lein-survey-2012.herokuapp.com"}
                "Last year's survey is still up."]]
           [:p "It may be interesting to compare some of these results "
            "with Chas Emerick's "
            [:a {:href "http://cemerick.com/2012/08/06/results-of-the-2012-state-of-clojure-survey/"}
             "State of Clojure"] " survey from last summer."]
           [:p "You can see "
            [:a {:href "https://github.com/technomancy/lein-survey"}
             "the source"] " for this survey on Github or get the "
            [:a {:href "/results.clj"} "raw results"]
            " for your own analysis."]
           [:p "Total responses: " (count results)]]
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

(defn pie
  ([q results] (pie q results (fn [q result] (get result q))))
  ([q results lookup]
     (let [freqs (dissoc (frequencies (map (partial lookup q) results))
                         nil "I don't remember" #{})
           freqs (sort-by (comp str key) freqs)
           labels (for [l (keys freqs)]
                    (first (.split (str l) "\\(")))]
       (charts/pie-chart labels (vals freqs)
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
           freqs (filter (fn [[x n]] (> n 3)) freqs)]
       (charts/bar-chart (map str (keys freqs)) (vals freqs)
                         :x-label "" :y-label ""
                         :title q :vertical false))))

(defn get-ranks [result]
  (into {} (for [[q rank] result
                 :when (.startsWith (name q) "Rank your biggest")
                 :let [annoyance (second (.split (name q) ": "))]]
             [annoyance rank])))

(defn stacked-bar [q results]
  (let [ranks (map get-ranks results)
        categories (keys (first ranks))
        values (for [category categories
                     ranking ranks
                     [annoyance number] ranking
                     :when (= category annoyance)]
                 [category number])
        ;; rankings (reduce (fn [acc [q n]]
        ;;                    (update-in acc [q] conj (Integer. n)))
        ;;                  (zipmap categories (repeat (count categories) []))
        ;;                  values)
        categories (map first values)
        values (map (comp #(Integer. %) second) values)]
    ;; grraaaah; stupid stacked bar charts can suck it
    (charts/stacked-bar-chart categories values
                              :x-label "" :y-label ""
                              :title "Biggest annoyances" :vertical false)))

(defn chart [q type results]
  (cond (= "How long have you been using Clojure?" q)
        (bar q results)
        (= :radio type)
        (pie q results)
        (= :check type)
        (bar q results)
        (= :os type)
        (pie q results os-lookup)
        ;; (= :rank type)
        ;; (stacked-bar q results)
        ))

(def hashed-questions (into {} (for [q q/questions]
                                 [(hash-question (first q)) q])))

(def image-bytes
  (memoize (fn [id]
             (let [[q type] (hashed-questions
                             id ["Your OS and package manager(s)" :os])
                   out (java.io.ByteArrayOutputStream.)
                   results (get-results)
                   chart (chart q type results)]
               (incanter/save chart out)
               (.toByteArray out)))))

(defn image [id]
  (java.io.ByteArrayInputStream. (image-bytes id)))

(defn comments []
  (string/join "\n----------------\n"
               (for [body (get-results)]
                 (get body "Other comments?"))))
