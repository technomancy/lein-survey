(ns lein-survey.questions)

(def questions [["How long have you been using Clojure?" :radio
                 ["Weeks" "Months" "1 year" "2 years" "3+ years"]]
                ["When did you start using Leiningen?" :radio
                 ["I don't remember"
                  "0.x (Nov 2009)"
                  "1.x (Dec 2009 - Mar 2012)"
                  "2.0.0-preview (Mar 2012 - Jan 2013)"
                  "2.x (Jan 2013 - present)"]]
                ["What operating systems do you use Leiningen on?" :check
                 ["GNU/Linux" "Mac OS X" "Windows"
                  "Cygwin or other Windows GNU"
                  "Solaris" "BSD" "other"]]
                ["What package managers do you use?" :check
                 ["apt" "yum" "nix" "portage" "pacman"
                  "BSD ports" "homebrew" "macports" "choclatey"]]
                ["How do you install Leiningen?" :check
                 ["Downloading bin/lein"
                  "Package manager"
                  "Through IDE integration"
                  "Windows installer"
                  "From git"]]
                ["What JVM versions you use regularly?  (java -version)" :check
                 ["1.6.0" "1.7.0" "1.8.0" "prereleases from source"]]
                ["Which Leiningen features do you use?" :check
                 ["custom profiles"
                  "aliases"
                  "checkout dependencies"
                  "editor/IDE integration"
                  "native dependencies"
                  "auto-cleaning of transitively-compiled .class files"
                  "pom task"
                  "CLI repl"
                  "grenchman"
                  "test selectors"
                  "trampoline task"]]
                ["Do you use Leiningen for any mixed-language projects?" :check
                 ["Only Clojure"
                  "Clojure with Clojurescript"
                  "Only Clojurescript"
                  "Clojure with Java"
                  "Clojure with another JVM language"
                  "Plain Java"
                  "Another JVM language alone"]]
                ["Favourite plugins? (comma-separated)" :textarea 2]
                ["Favourite templates? (comma-separated)" :textarea 2]
                ["What annoys you about Leiningen?" :check
                 ["startup time"
                  "difficulty finding dependencies"
                  "lack of plugins"
                  "confusing profile logic"
                  "support for native code"
                  "AOT issues"
                  "unmanaged jars"
                  "leiningen's own end-user docs"
                  "docs on extending Leiningen or writing plugins"
                  "other (please provide details in comment box below)"]]
                ["Do you have a GPG key?"
                 :radio ["Yes, and I've gotten it signed by others"
                         "Yes, and I have used it"
                         "I have one, but I've never used it"
                         "I've been meaning to get one"
                         "No"]]
                [(str "Did you know if you have a single patch accepted you can"
                      " ask for commit rights and a sticker?") :radio
                      ["Yes" "No"]]
                ["Other comments?" :textarea]])
