;; Provides functions commonly used throughout the library.
;; 
;; Author:  Daniel Rugg
(ns pliant.hl7.io
  (:import [java.io BufferedReader InputStream InputStreamReader]))


(defn stream->reader
  "Wraps a BufferedReader around an InputStream."
  [^InputStream is]
  (BufferedReader. (InputStreamReader. is)))

