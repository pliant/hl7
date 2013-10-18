(ns pliant.hl7.task.common-test
  (:use clojure.test
        pliant.hl7.task.common))

(deftest url->map-test
  (testing "Testing url->map."
    (is (= {:key1 "val1" :key2 "va=l2"} (url->map "key1=val1&key2=va=l2")))))


(deftest validate-option-test
  (testing "Testing validate-option."
    (is (validate-option {:key "val"} [:key]))
    (nil? (validate-option {:key "val"} [:notkey]))
    
    (is (validate-option {:key "val"} [:key] :test identity))
    (is (thrown? IllegalArgumentException (validate-option {:key "val"} [:notkey] :test identity)))
    
    (keyword? (validate-option {:key "val"} [:key] :parse keyword))
    (nil? (validate-option {:key "val"} [:notkey] :parse keyword))))

(deftest directory?-test
  (testing "Testing directory?."
    (is (directory? "/"))
    (is (not (directory? "/NoWayIsThisADirectory")))))
