# jason

JSON encoding and decoding function construction with support for configurable 
key conversion.

Supports:
- configuration of all key conversions
- preservation of metadata fields (e.g., as in HAL) during conversion
- additional Jackson modules for encoding / decoding various Java types

Includes convenience coders for wire (i.e., service to service) JSON and
database JSON:

For wire JSON:
- converts from kebab-case keywords to camelCase JSON and back again
- includes serialisation of `org.joda.time` and `java.time` types

For database JSON:
- converts from kebab-case keywords to snake_case JSON and back again
- includes serialisation of `org.joda.time` and `java.time` types

## Install

Add the following to your `project.clj` file:

```clj
[b-social/jason "0.1.7"]
```

## Documentation

* [API Docs](http://b-social.github.io/jason)

## Usage

```clojure
(require '[jason.core :refer [defcoders] :as jason])
(require '[camel-snake-kebab.core :refer [->snake_case_string
                                          ->snake_case_keyword]])

(defcoders standard)
(defcoders db
  :encode-key-fn (jason/->encode-key-fn ->snake_case_string)
  :decode-key-fn (jason/->decode-key-fn ->snake_case_keyword))

(->standard-json {:first-name "Jess"})
;; => "{\"first-name\": \"Jess\"}"

(->db-json {:first-name "Jess"})
;; => "{\"first_name\": \"Jess\"}"

(<-standard-json "{\"firstName\": \"Jess\"}")
;; => {"firstName" "Jess"}

(<-db-json "{\"first_name\": \"Jess\"}")
;; => {:first_name "Jess"}
```

See the [Getting Started](https://b-social.github.io/jason/getting-started.html) 
guide for more details.

## License

Copyright © 2018 B-Social Ltd.

Distributed under the terms of the 
[MIT License](http://opensource.org/licenses/MIT).
