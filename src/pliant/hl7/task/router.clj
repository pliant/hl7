;; A task for consuming messages from one source and sending them to a target destination.
;; 
;; Author:  Daniel Rugg
(ns pliant.hl7.task.router
  (:gen-class)
  (:require [clojure.java.io :refer (reader resource as-url as-file file writer)]
            [clojure.tools.cli :refer (cli)]
            [pliant.hl7.codec :refer (reader->message-seq separators segment-parser message->writer)]
            [pliant.hl7.task.common :refer (url->map validate-option directory? exists? nil->empty)]))


(defmulti partitioners identity)

(defmethod partitioners :msg-hour
  [_]
  (fn [options msg]
    (if-let [header (first msg)]
      (.substring (nth ((segment-parser (separators header)) header) 6) 0 10))))


(defmulti sources :source)

(defmethod sources :file
  [options]
  (if-let [file-path (validate-option options  [:source-options :path] :test exists? :parse as-file)]
    (fn [target]
      (with-open [rdr (reader file-path)]
        (doseq [msg (reader->message-seq rdr)]
          (target msg)))
      (target nil))))

(defmethod sources :resource
  [options]
  (if-let [r (validate-option options [:source-options :path] :test identity :parse resource)]
    (fn [target]
      (with-open [rdr (reader r)]
        (doseq [msg (reader->message-seq rdr)]
          (target msg)))
      (target nil))))

(defmethod sources :uri
  [options]
  (if-let [url (validate-option options [:source-options :uri] :test identity :parse as-url)]
    (fn [target]
      (with-open [rdr (reader url)]
        (doseq [msg (reader->message-seq rdr)]
          (target msg)))
      (target nil))))


(defmulti targets :target)

(defmethod targets :counter
  [options]
  (let [count (atom 0)]
    (fn [msg]
      (if msg
        (swap! count inc)
        (println "Total Messages: " @count)))))

(defmethod targets :splitter
  [options]
  (let [count (atom 0)
        counts (atom {})
        endpoints (atom {})
        partitioner (partitioners (validate-option options [:target-options :partitioner] :test identity :parse keyword))
        prefix (validate-option options [:target-options :prefix] :parse nil->empty)
        suffix (validate-option options [:target-options :suffix] :parse nil->empty)
        directory (validate-option options [:target-options :directory] :test directory?)
        append (validate-option options [:target-options :append] :parse (fn [x] (= x "true")))]
    (fn [msg]
      (if msg
        (let [fileName (str prefix (partitioner options msg) suffix)]
          (swap! count inc)
          (if (@endpoints fileName)
            (do
              (swap! counts update-in [fileName] inc)
              (message->writer msg (@endpoints fileName)))
            (let [writer (writer (file directory fileName) :append append)] 
              (swap! counts assoc-in [fileName] 1)
              (swap! endpoints assoc fileName writer)
              (message->writer msg writer))))
        (do ; Messages have ended.  Close all connections and report on counts.
          (println "Total Messages: " @count)
          (doseq [[f c] @counts]
            (println "Total In " f ": " c))
          (doseq [[fileName writer] @endpoints]
            (try 
              (.close writer)
              (catch Exception e (println (str "Failed to close writer for " fileName))))))))))


(def cmdopts [["-s" "--source"
               "The source type to obtain the HL7 messages.  Known types are file, resource, port."
               :default :resource :parse-fn keyword]
              ["-so" "--source-options" 
               "The options that are passed to the source processor. Provided in key1=val&key2=val format, with no spaces"
               :parse-fn url->map]
              ["-t" "--target" 
               "(Required) The name of the function that determines where messages are routed." 
               :default :counter :parse-fn keyword]
              ["-to" "--target-options" 
               "The options that are passed to the target processor. Provided in key1=val&key2=val format, with no spaces"
               :parse-fn url->map]
              ["-h" "--help" 
               "Show help options" 
               :default false :flag true]])

(defn -main
  "Entry point for the split-file task.  To understand how to execute, call:  
     java -cp .:hl7-uber.jar pliant.hl7.task.split_file --help"
  [& args]
  (let [[options args banner] (try
                                (apply cli args cmdopts)
                                (catch Exception e
                                  [{:help true}]))]
    (when (:help options)
      (println banner)
      (System/exit 0))
    (try
      (println "Running With Options: " options)
      (let [source (sources options)
            target (targets options)]
        (source target))
      (catch Exception e (println "Task Failed: " e)))))
