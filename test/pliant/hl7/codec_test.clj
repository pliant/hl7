(ns pliant.hl7.codec-test
  (:use clojure.test
        pliant.hl7.codec
        pliant.hl7.io)
  (:require [clojure.java.io :refer (reader resource)])
  (:import[java.io StringReader]))


(deftest read-segment-test
  (testing "Testing read-segment"
    (with-open [reader (StringReader. "TESTME\rNOSHOW")]
      (is (= "TESTME" (read-segment reader))))))


(deftest reader->segment-seq-test
  (testing "Testing reader->segment-seq with trailing carriage return."
    (with-open [rdr (reader (resource "cratend.hl7"))]
      (let [seq (reader->segment-seq rdr)]
        (is (= 29 (count seq))))))
  (testing "Testing reader->segment-seq without trailing carriage return."
    (with-open [rdr (reader (resource "nocratend.hl7"))]
      (let [seq (reader->segment-seq rdr)]
        (is (= 29 (count seq)))))))


(deftest string->segment-seq-test
  (testing "Testing string->segment-seq."
    (is (nil? (string->segment-seq nil)))
    (is (= 3 (count (string->segment-seq "test\rbest\rguest"))))
    (is (= 3 (count (string->segment-seq "test\rbest\rguest\r"))))))


(deftest reader->message-seq-test
  (testing "Testing reader->message-seq with trailing carriage return."
    (with-open [rdr (reader (resource "cratend.hl7"))]
      (let [seq (reader->message-seq rdr)]
        (is (= 3 (count seq))))))
  (testing "Testing reader->message-seq without trailing carriage return."
    (with-open [rdr (reader (resource "nocratend.hl7"))]
      (let [seq (reader->message-seq rdr)]
        (is (= 3 (count seq)))))))


(deftest string->message-seq-test
  (testing "Testing string->message-seq with trailing carriage return."
    (let [seq (string->message-seq "MSH|BLAH\rPID|BLAH\rMSH|BLAH\rPID|BLAH\rMSH|BLAH\rPID|BLAH\r")]
      (is (= 3 (count seq)))))
  (testing "Testing string->message-seq without trailing carriage return."
    (let [seq (string->message-seq "MSH|BLAH\rPID|BLAH\rMSH|BLAH\rPID|BLAH\rMSH|BLAH\rPID|BLAH")]
      (is (= 3 (count seq))))))


(deftest separators-test
  (testing "Testing separators."
    (let [seps (separators "MSH|^~\\&|BLAH")]
      (is (= \| (:field seps)))
      (is (= \^ (:component seps)))
      (is (= \& (:subcomponent seps)))
      (is (= \~ (:repeat seps)))
      (is (= \\ (:escape seps))))))


(deftest segment-parser-test
  (testing "Testing segment-parser."
    (let [header "MSH|^~\\&|PATCOM|0703|CDMS|USLG|20131011153154||ADT^A08|31011193154623348927|P|2.1|29430"
          segment "PID|||111111111^^PHS||JONES^JON^B^\"\"^\"\"^\"\"|\"\"|19770701|F|623348927|B|414 CLARA LN^\"\"^WASH^DC^294837428|08|(555)555-5555|\"\"|ENG|S|\"\"|62334892^7^PHS|214900506|\"\""
          seps (separators header)
          parser (segment-parser seps)]
      (is (= 13 (count (parser header))))
      (is (vector? (nth (parser header) 8)))
      (is (= 2 (count (nth (parser header) 8))))
      (is (= 21 (count (parser segment))))
      (is (vector? (nth (parser segment) 5)))
      (is (= 6 (count (nth (parser segment) 5)))))))
           
