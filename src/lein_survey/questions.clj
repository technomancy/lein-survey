(ns lein-survey.questions)

(def questions [["How long have you been using Clojure?" :radio
                 ["Weeks" "Months" "1 year" "2 years" "3+ years"]]
                ["For what do you use Leiningen? (pick as many as apply)" :check
                 ["Open source" "Proprietary projects"
                  "Libraries" "Web sites" "Deployment"
                  "Backend code" "GUI clients" "Command-line applications"]]
                ["Do you deploy jars with Leiningen?" :check
                 ["to Clojars" "to other public repositories"
                  "to private repositories"]]
                ["When did you start using Leiningen?" :radio
                 ["I don't remember"
                  "0.x (Nov 2009)"
                  "1.x (Dec 2009 - Mar 2012)"
                  "2.0.0-previewN (Mar 2012 - Jan 2013)"
                  "2.x (Jan 2013 - present)"]]
                ["What operating systems do you use Leiningen on?" :check
                 ["GNU/Linux" "Mac OS X" "Windows"
                  "Cygwin or other Windows GNU"
                  "Solaris" "BSD" "other"]]
                ["What package managers do you use?" :check
                 ["apt" "yum" "nix" "portage" "pacman"
                  "BSD ports" "homebrew" "macports" "fink"]]
                ["How do you install Leiningen?" :check
                 ["Downloading bin/lein"
                  "Package manager"
                  "Git"]]
                [(str "What's the oldest JVM version you use regularly?"
                      " (java -version)")
                 :radio
                 ["1.6.0_18 or older"
                  "1.6.0 newer"
                  "1.7.0"
                  "1.8.0"]]
                ["Which Leiningen features do you use?" :check
                 ["custom profiles"
                  "aliases"
                  "checkout dependencies"
                  "editor/IDE integration"
                  "javac task"
                  "native dependencies"
                  "auto-cleaning of transitively-compiled .class files"
                  "pom task (other than for Clojars)"
                  "CLI repl"
                  "test selectors"
                  "trampoline task"]]
                ["Favourite plugins? (comma-separated)" :textarea 2]
                ["Favourite templates? (comma-separated)" :textarea 2]
                ["What annoys you about Leiningen?" :check
                 ["startup time"
                  "difficulty finding dependencies"
                  "lack of plugins"
                  "support for native code"
                  "unmanaged jars"
                  "leiningen's own end-user docs"
                  "docs on extending Leiningen or writing plugins"
                  "other (see comment box below)"]]
                ["Do you have a GPG key?"
                 :radio ["Yes, and I've gotten it signed by others"
                         "Yes, and I have used it"
                         "I have one, but I've never used it"
                         "I've been meaning to get one"
                         "No"]]
                ["Do you have custom tasks you haven't published as a plugin?"
                 :radio ["Yes" "No"]]
                [(str "Did you know if you have a single patch accepted you can"
                      " ask for commit rights and a sticker?") :radio
                      ["Yes" "No"]]
                ["Other comments?" :textarea]])