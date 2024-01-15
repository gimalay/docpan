(ns reader
  (:require [clojure.data.json :as json]
            [clojure.java.shell :refer [sh]]))


(defn parse-json [json-string]
  (json/read-str json-string :key-fn keyword))

(defn parse-string [markdown]
  (dissoc
    (parse-json (:out (sh "pandoc" "-f" "markdown" "-t" "json" :in markdown)))
    :pandoc-api-version))


(def md "
paragraph

- list item
- list item\n

para 2

## Header

- list item

## Links

- [test1](f5bad274)
- [test2](8a75915f) ")

(parse-string md)
