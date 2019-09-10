(ns jason.core-test
  (:require
    [clojure.test :refer :all]
    [clojure.string :as string]

    [clj-time.core :as time]

    [camel-snake-kebab.core
     :refer [->camelCaseString
             ->snake_case_string
             ->snake_case_keyword
             ->kebab-case-keyword]]

    [jason.core :as jason])
  (:import [java.time ZonedDateTime ZoneOffset]))

(defn- multiline-str [& args]
  (string/join "\n" args))

(deftest ->encode-key-fn
  (testing "converts to camel case string for standard keys by default"
    (let [key-fn (jason/->encode-key-fn)]
      (is (= "field" (key-fn :field)))
      (is (= "someField" (key-fn :some-field)))
      (is (= "someOtherField" (key-fn :some-other-field)))))

  (testing (str "converts to camel case string preserving meta indicator for "
             "meta keys by default")
    (let [key-fn (jason/->encode-key-fn)]
      (is (= "_field" (key-fn :_field)))
      (is (= "_someField" (key-fn :_some-field)))
      (is (= "_someOtherField" (key-fn :_some-other-field)))))

  (testing (str "uses specified standard key converter for standard keys "
             "when provided")
    (let [key-fn (jason/->encode-key-fn
                   {:standard-key-fn ->snake_case_string})]
      (is (= "field" (key-fn :field)))
      (is (= "some_field" (key-fn :some-field)))
      (is (= "some_other_field" (key-fn :some-other-field)))))

  (testing (str "uses specified standard key converter for meta keys "
             "preserving meta indicator when provided")
    (let [key-fn (jason/->encode-key-fn
                   {:standard-key-fn ->snake_case_string})]
      (is (= "_field" (key-fn :_field)))
      (is (= "_some_field" (key-fn :_some-field)))
      (is (= "_some_other_field" (key-fn :_some-other-field)))))

  (testing (str "uses specified meta key converter for meta keys "
             "when provided")
    (let [key-fn (jason/->encode-key-fn {:meta-key-fn name})]
      (is (= "_field" (key-fn :_field)))
      (is (= "_some-field" (key-fn :_some-field)))
      (is (= "_some-other-field" (key-fn :_some-other-field)))))

  (testing "treats function argument as standard-key-fn"
    (let [key-fn (jason/->encode-key-fn ->snake_case_string)]
      (is (= "field" (key-fn :field)))
      (is (= "some_field" (key-fn :some-field)))
      (is (= "some_other_field" (key-fn :some-other-field)))
      (is (= "_field" (key-fn :_field)))
      (is (= "_some_field" (key-fn :_some-field)))
      (is (= "_some_other_field" (key-fn :_some-other-field))))))

(deftest ->decode-key-fn
  (testing "converts to kebab case keyword for standard keys by default"
    (let [key-fn (jason/->decode-key-fn)]
      (is (= :field (key-fn "field")))
      (is (= :some-field (key-fn "someField")))
      (is (= :some-other-field (key-fn "someOtherField")))))

  (testing (str "converts to kebab case keyword preserving meta indicator for "
             "meta keys by default")
    (let [key-fn (jason/->decode-key-fn)]
      (is (= :_field (key-fn "_field")))
      (is (= :_some-field (key-fn "_someField")))
      (is (= :_some-other-field (key-fn "_someOtherField")))))

  (testing (str "uses specified standard key converter for standard keys "
             "when provided")
    (let [key-fn (jason/->decode-key-fn
                   {:standard-key-fn ->snake_case_keyword})]
      (is (= :field (key-fn "field")))
      (is (= :some_field (key-fn "someField")))
      (is (= :some_other_field (key-fn "someOtherField")))))

  (testing (str "uses specified standard key converter for meta keys "
             "preserving meta indicator when provided")
    (let [key-fn (jason/->decode-key-fn
                   {:standard-key-fn ->snake_case_keyword})]
      (is (= :_field (key-fn "_field")))
      (is (= :_some_field (key-fn "_someField")))
      (is (= :_some_other_field (key-fn "_someOtherField")))))

  (testing (str "uses specified meta key converter for meta keys "
             "when provided")
    (let [key-fn (jason/->decode-key-fn {:meta-key-fn keyword})]
      (is (= :_field (key-fn "_field")))
      (is (= :_some-field (key-fn "_some-field")))
      (is (= :_some-other-field (key-fn "_some-other-field")))))

  (testing "treats function argument as standard-key-fn"
    (let [key-fn (jason/->decode-key-fn ->snake_case_keyword)]
      (is (= :field (key-fn "field")))
      (is (= :some_field (key-fn "someField")))
      (is (= :some_other_field (key-fn "someOtherField")))
      (is (= :_field (key-fn "_field")))
      (is (= :_some_field (key-fn "_someField")))
      (is (= :_some_other_field (key-fn "_someOtherField"))))))

(deftest new-json-coders
  (testing "returns a map with ->json and <-json functions and default key fns"
    (let [{:keys [->json <-json]} (jason/new-json-coders)]
      (testing "for <-json"
        (testing "parses json"
          (is (= {:key 123}
                (<-json "{\"key\": 123}"))))

        (testing "converts keys to kebab case"
          (is (= {:some-key 123}
                (<-json "{\"someKey\": 123}"))))

        (testing "preserves keys prefixed with an underscore"
          (is (= {:_some-links 123}
                (<-json "{\"_someLinks\": 123}")))))

      (testing "for ->json"
        (testing "returns a json string"
          (is (= (multiline-str
                   "{"
                   "  \"key\" : 123"
                   "}")
                (->json {:key 123}))))

        (testing "converts keys to kebab case"
          (is (= (multiline-str
                   "{"
                   "  \"someKey\" : 123"
                   "}")
                (->json {:some-key 123}))))

        (testing "preserves meta keys"
          (is (= (multiline-str
                   "{"
                   "  \"_someKey\" : 123"
                   "}")
                (->json {:_some-key 123}))))

        (testing "converts joda dates"
          (is (= (multiline-str
                   "{"
                   "  \"key\" : \"2019-02-03T00:00:00.000Z\""
                   "}")
                (->json {:key (time/date-time 2019 2 3)}))))

        (testing "converts java.time dates"
          (let [date-time (ZonedDateTime/of 2019 2 3 0 0 0 0 ZoneOffset/UTC)]
            (is (= (multiline-str
                     "{"
                     "  \"key\" : \"2019-02-03T00:00:00Z\""
                     "}")
                  (->json {:key date-time}))))))))

  (testing "uses specified encode key function when provided"
    (let [{:keys [->json]}
          (jason/new-json-coders
            {:encode-key-fn (jason/->encode-key-fn
                              {:standard-key-fn ->snake_case_string})})]
      (is (= (multiline-str
               "{"
               "  \"some_key\" : 123"
               "}")
            (->json {:some-key 123})))

      (is (= (multiline-str
               "{"
               "  \"_some_key\" : 123"
               "}")
            (->json {:_some-key 123})))))

  (testing "uses specified decode key function when provided"
    (let [{:keys [<-json]}
          (jason/new-json-coders
            {:decode-key-fn (jason/->decode-key-fn
                              {:standard-key-fn ->snake_case_keyword})})]
      (testing "converts keys to kebab case"
        (is (= {:some_key 123}
              (<-json "{\"someKey\": 123}"))))

      (testing "preserves keys prefixed with an underscore"
        (is (= {:_some_links 123}
              (<-json "{\"_someLinks\": 123}")))))))

(declare
  ->database-json
  <-database-json)

(jason/defcoders database
  :encode-key-fn (jason/->encode-key-fn ->snake_case_string)
  :decode-key-fn (jason/->decode-key-fn ->kebab-case-keyword))

(deftest defcoders
  (testing (str "defines coder functions in the current namespace of the "
             "provided type")
    (is (= {:key 123} (<-database-json "{\"key\": 123}")))
    (is (= {:some-key 123} (<-database-json "{\"some_key\": 123}")))
    (is (= {:_some-links 123} (<-database-json "{\"_some_links\": 123}")))

    (is (= (multiline-str
             "{"
             "  \"key\" : 123"
             "}")
          (->database-json {:key 123})))
    (is (= (multiline-str
             "{"
             "  \"some_key\" : 123"
             "}")
          (->database-json {:some-key 123})))
    (is (= (multiline-str
             "{"
             "  \"_some_key\" : 123"
             "}")
          (->database-json {:_some-key 123})))))
