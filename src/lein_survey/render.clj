(ns lein-survey.render
  (:use [hiccup.page :only [html5 doctype include-css]]
        [hiccup.form :only [form-to label text-field submit-button]]))

(def input nil) ; dang it defonce semantics!
(defmulti input second)

(defmethod input :radio [[question _ answers]]
  [:div
   [:p.question question]
   [:ul.inputs-list
    (for [a answers]
      [:li [:label [:input {:type "radio" :name question :value a}]
            [:span a]]])]])

(defmethod input :check [[question _ answers]]
  [:div
   [:p.question question]
   [:ul.inputs-list
    (for [a answers]
      [:li [:label [:input {:type "checkbox" :name question :value a}]
            [:span a]]])]])

(defmethod input :rank [[question _ answers]]
  [:div
   [:p.question question]
   [:ul
    (for [a answers]
      [:li.ranking [:span a]
       [:ul.rank
        (for [n (reverse (range (count answers)))]
          [:li [:label [:input {:type "radio"
                                :name (str question " " a) :value n}]
                [:span n]]])]])]])

(defmethod input :textarea [[question _ rows]]
  [:div [:p.question question]
   [:textarea.xxlarge {:rows (or rows 5) :name question}]])

(defn questions-form [questions]
  [:div
   [:div.row
    [:div.span10
     [:p (str "Do you use Leiningen? We'd love it if you could take a few"
              " minutes to answer some questions.")]]]
   #_[:div.row
       [:div.span10
        [:p "The survey results are "
         [:a {:href "/results"} "available"]
         ", but you can still fill it out if you like."]]]
   #_[:hr]
   [:form {:method "POST" :action "/"}
    (concat (map input questions)
            [[:div.content [:input.btn.primary {:type "submit"
                                                :value "Answer"}]]])]])

(defn layout [content]
  (html5
   [:head
    [:meta {:charset "utf-8"}]
    [:title "Leiningen Survey: 2013"]
    (include-css "bootstrap.min.css"
                 "lein-survey.css"
                 "http://fonts.googleapis.com/css?family=Electrolize")]
   [:body
    [:div.container
     [:div.content
      [:div.page-header
       [:h1 "Leiningen Survey: 2013"]]

      [:div.row
       [:div.offset1
        content]]]]]))