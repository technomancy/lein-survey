(defproject lein-survey "1.0.0-SNAPSHOT"
  :description "Survey for Leiningen users"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [incanter/incanter-charts "1.4.1"]
                 [org.clojure/java.jdbc "0.2.1"]
                 [postgresql "9.1-901-1.jdbc4"]
                 [hiccup "1.0.2"]
                 [ring/ring-jetty-adapter "1.1.6"]]
  :min-lein-version "2.0.0"
  :uberjar-name "lein-survey-standalone.jar"
  :main lein-survey.web
  :aot [lein-survey.web])
