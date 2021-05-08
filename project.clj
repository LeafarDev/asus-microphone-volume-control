(defproject asus-microphone-volume-control "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"] [cljfx "1.7.13"]]
  :main ^:skip-aot asus-microphone-volume-control.core
  :target-path "target/%s"
  :jvm-opts ["-Dcljfx.skip-javafx-initialization=true"]
  :profiles {:uberjar {:aot :all}})
