(ns pliant.hl7.io-test
  (:use clojure.test
        pliant.hl7.io)
  (:import[java.io ByteArrayInputStream]))


(deftest stream->reader-test
  (testing "Testing common/stream->reader function."
    (with-open [rdr (stream->reader (ByteArrayInputStream. (.getBytes "TESTME")))]
      (is (= "TESTME" (.readLine rdr))))))
