(defproject pliant/hl7 "0.1.0-SNAPSHOT"
  :description "Provides tools for working with HL7 messages and streams."
  
  :url "https://github.com/HSSC/data-tools"
  
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  
  :main pliant.hl7.task.router
  
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.cli "0.2.4"]])
