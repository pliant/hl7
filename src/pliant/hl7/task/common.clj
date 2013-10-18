;; Provides functions commonly used throughout tasks.
;; 
;; Author:  Daniel Rugg

(ns pliant.hl7.task.common
  (:require [clojure.java.io :refer (as-file)]))

(defn url->map
  "Parses a string that has URL parameter formating into a map.
   The keys are made into keywords, and the values are left as strings."
  [val]
  (into {} (for [[_ k v] (re-seq #"([^&=]+)=([^&]+)" val)]
    [(keyword k) v])))


(defn validate-option
  "Used to validate values in commandline options."
  [options path & {:as fns}]
  (try
    (let [test-fn (or (:test fns) (constantly true))
          parse-fn (or (:parse fns) identity)
          val (parse-fn (get-in options path))]
      (if (test-fn val)
        val
        (throw (IllegalArgumentException. (str "Option at " path " is not valid.  Value=" val)))))
    (catch IllegalArgumentException e (throw e))
    (catch Exception e 
      (throw (IllegalArgumentException. (str "Option at " path " is not valid.  Exception=" (.getMessage e)))))))


(defn directory?
  [val]
  (try 
    (.isDirectory (as-file val))
    (catch Exception e false)))

(defn exists?
  [val]
  (try 
    (.exists (as-file val))
    (catch Exception e false)))

(defn nil->empty
  [val]
  (or val ""))