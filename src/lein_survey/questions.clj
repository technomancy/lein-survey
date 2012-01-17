(ns lein-survey.questions)

(def questions [["How long have you been using Clojure?" :radio
                 ["Just started" "Weeks" "Months" "1 year" "2 years" "3+ years"]]
                ["For what do you use Leiningen? (pick as many as apply)" :check
                 ["Open source" "Proprietary projects"
                  "Libraries" "Web sites" "Deployment"
                  "Backend code" "GUI clients" "Command-line applications"]]
                ["Do you deploy jars?" :check
                 ["to Clojars" "to other public repositories"
                  "to private repositories"]]
                ["When did you start using Leiningen?" :radio
                 ["I don't remember"
                  "0.5 (Nov 2009)"
                  "1.0 (Dec 2009)"
                  "1.1 (Feb 2010)"
                  "1.2 (Jul 2010)"
                  "1.3 (Aug 2010)"
                  "1.4 (Dec 2010)"
                  "1.5 (Mar 2011)"
                  "1.6 (Jun 2011)"
                  "2.0.0-SNAPSHOT"]]
                ["Your OS and package manager(s)" :check
                 ["Debian/Ubuntu"
                  "Fedora/other RPM-based"
                  "Arch" "Gentoo" "Nix"
                  "Other GNU/Linux"
                  "Mac OS X with Homebrew"
                  "Mac OS X with Macports"
                  "Mac OS X with Fink"
                  "Mac OS X with no package manager"
                  "Windows with Powershell"
                  "Windows without Powershell"
                  "Windows with Cygwin"
                  "Solaris"
                  "BSD"
                  "other"]]
                ["How do you install Leiningen" :check
                 ["Downloading bin/lein"
                  "Package manager"
                  "Git"]]
                [(str "Paste your results: history | grep \"lein \" | "
                      "awk '{print $3}' | sort | uniq -c | sort -nr | "
                      "egrep -v \"^ +1\"") :textarea]
                ["Which Leiningen features do you use?" :check
                 ["Auto-cleaning of transitively-compiled .class files"
                  "Checkout dependencies"
                  "clean task"
                  "Editor integration"
                  "interactive task"
                  "javac task"
                  "lein-clojars plugin"
                  "Native dependencies"
                  "pom task (other than for pushing to Clojars)"
                  "repl task"
                  "Shell wrappers"
                  "Test selectors"
                  "trampoline task"]]
                ["Favourite plugins? (comma-separated)" :textarea 2]
                ["Rank your biggest annoyances: (higher is worse)" :rank
                 ["Startup time"
                  "Difficulty finding dependencies"
                  "Not enough plugins"
                  "Leiningen's own end-user docs"
                  "Docs on extending Leiningen or writing plugins"
                  "ClojureScript integration"]]
                ["Do you have custom tasks you haven't published as a plugin?"
                 :radio
                 ["Yes" "No"]]
                [(str "Did you know if you have a single patch accepted you can"
                      " ask for commit rights and a sticker?") :radio
                      ["Yes" "No"]]
                ["Other comments?" :textarea]])