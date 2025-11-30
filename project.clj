(defproject bvm "0.1.0-SNAPSHOT"
  :description "A collection of generative art sketches, written in Quil and inspired by the work of Vera Molnár."
  :url "http://noelr.dev"
  :license {:name "CC BY-NC-SA 4.0: Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International"
            :url "https://creativecommons.org/licenses/by-nc-sa/4.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [quil "4.3.1323"]
                 [nrepl "1.3.1"]
                 [com.bhauman/rebel-readline "0.1.5"]
                 [com.bhauman/rebel-readline-nrepl "0.1.5"]
                 [org.clojure/test.check "1.1.1"]]

  :plugins [[cider/cider-nrepl "0.52.1"]]
  :repl-options {:repl-middleware ["cider.nrepl/cider-middleware"]})
