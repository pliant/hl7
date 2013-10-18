;; Provides functions for converting between streams of HL7 data and representative structures.
;; 
;; Author:  Daniel Rugg
;; References:  Ghadi Shayban (hl7profiling)
(ns pliant.hl7.codec
  (:use [pliant.hl7.io])
  (:import [java.io Reader InputStream StringReader Writer]))


(defn read-segment
  "Reads a HL7 segment from an inputstream.  HL7 messages segments are partitioned by the 
   carriage return (ASCII value 13).  Returns a string containing the next segment or nil
   when the end of the stream is reached."
  [^Reader rdr]
  (let [c (.read rdr)]
    (when (not= c -1)
      (let [sb (StringBuilder. 8192)]
        (loop [ch c]
          (if (or (= ch -1)
                  (= ch 13))
            (str sb)
            (do (.append sb (char ch))
                (recur (.read rdr)))))))))


(defn reader->segment-seq
  "Creates a lazy sequence of HL7 segments from a Reader."
  [^Reader rdr]
  (when-let [segment (read-segment rdr)]
    (cons segment (lazy-seq (reader->segment-seq rdr)))))


(defn string->segment-seq
  "Creates a sequence of HL7 segments from a String. This is not lazy."
  [^String s]
  (and s (seq (clojure.string/split s #"\r"))))


(defn reader->message-seq
  "Creates a lazy sequence of HL7 messages from a Reader."
  ([^Reader rdr] 
    (if-let [ss (reader->segment-seq rdr)]
      (reader->message-seq (next ss) (first ss))))
  ([segments header]
    (when segments
      (loop [msg [header]
             segs (next segments)
             seg (first segments)]
        (cond 
          (nil? seg) (cons msg nil)
          (.startsWith seg "MSH") (cons msg (lazy-seq (reader->message-seq segs seg)))
          :else (recur (conj msg seg) (next segs) (first segs)))))))


(defn string->message-seq
  "Creates a lazy sequence of HL7 messages from a Reader."
  [^String s] 
  (if-let [segments (string->segment-seq s)]
    (let [msgs (reduce (fn [m seg] 
                         (if (.startsWith seg "MSH")
                           {:ms (conj (:ms m) (:m m)) :m [seg]}
                           (assoc m :m (conj (:m m) seg))))
                       {:ms [] :m [(first segments)]} (next segments))]
      (conj (:ms msgs) (:m msgs)))))


(defn separators
  "Obtains a map of characters used as specific separators throughout a HL7 message.  Either the header segment string 
   or full message vector of segments can be passed."
  [arg]
  (cond 
    (string? arg) {:field (.charAt arg 3)
                   :component (.charAt arg 4)
                   :repeat (.charAt arg 5)
                   :escape (.charAt arg 6)
                   :subcomponent (.charAt arg 7)}
    (vector? arg) (separators (first arg))
    :else nil))

(def char-patterns {\| #"\|"
               \\ #"\\"
               \^ #"\^"
               \& #"\&"
               \~ #"\~"
               \[ #"\["
               \] #"\]"
               \{ #"\{"
               \} #"\}"
               \? #"\?"
               \/ #"\/"
               \< #"\<"
               \> #"\>"
               \! #"\!"
               \. #"\."
               \( #"\("
               \) #"\)"
               \+ #"\+"
               \= #"\="
               \* #"\*"
               \: #"\:"
               \; #"\;"
               \, #"\,"
               \@ #"\@"
               \# #"\#"
               \% #"\%"
               \` #"\`"
               \$ #"\$"
               \- #"\-"
               \_ #"\_"
               \' #"\'"
               \" #"\""})

(defn char->pattern
  [c]
  (or (char-patterns c) (re-pattern (str c))))


(defn segment-parser
  "Creates a function that will parse segments into vectors using the provided separators."
  [seps]
  (let [s (char->pattern (:subcomponent seps))
        c (char->pattern (:component seps))
        f (char->pattern (:field seps))
        split-s #(if (re-find s %) (clojure.string/split % s) %)
        split-c #(if (re-find c %) 
                   (into [] (map split-s (clojure.string/split % c))) %)
        split-f #(clojure.string/split % f)]
    (fn [seg]
      (if (.startsWith seg "MSH")
        (into ["MSH" (.substring seg 3 8)] (map split-c (split-f (.substring seg 9))))
        (into [] (map split-c (split-f seg)))))))


(defn message->writer
  [msg ^Writer writer]
  (doseq [seg msg]
    (.write writer seg)
    (.write writer 13)))