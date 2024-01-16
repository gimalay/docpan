(ns docpan.reader
  (:require [clojure.data.json :as json]
            [clojure.java.shell :refer [sh]]))




(defn parse-json [json-string]
  (json/read-str json-string :key-fn keyword))

(defn parse-string [markdown]
  (dissoc
    (parse-json (:out (sh "pandoc" "-f" "markdown" "-t" "json" :in markdown)))
    :pandoc-api-version))


(def md "
---
test: val
---

paragraph

- list item
- list item\n

para 2

## Header

- list item

## Links

---

- [test1](f5bad274)
- [test2](8a75915f) ")

(defn parse-attr [attr]
  (let [[identifier classes keyvals] attr]
    [(if (empty? identifier) nil identifier)
     (seq classes)
     (into {} keyvals)]))

(defn parse-inline [inline]
  (let [inline-type (keyword (:t inline))
        parse-fn (case inline-type
                   :Str (fn [i] (:c i))
                   :Space (fn [_] nil)
                   :SoftBreak (fn [_] nil)
                   :LineBreak (fn [_] nil)
                   :Emph (fn [i] (map parse-inline (:c i)))
                   :Strong (fn [i] (map parse-inline (:c i)))
                   :Link (fn [i] (let [[attr inlines target] (:c i)]
                                   [(parse-attr attr)
                                    (map parse-inline inlines)
                                    target]))
                   (fn [i] i))]
    [inline-type (parse-fn inline)]))

(defn parse-block [block]
  (let [block-type (keyword (:t block))
        parse-fn (case block-type
                   :Para (fn [b] (map parse-inline (:c b)))
                   :Plain (fn [b] (map parse-inline (:c b)))
                   :BulletList (fn [b] (map (comp (partial map parse-block) :c) (:c b)))
                   :OrderedList (fn [b] (map (comp (partial map parse-block) :c) (:c b)))
                   :Header (fn [b] (let [[level attr inlines] (:c b)]
                                     [level (parse-attr attr) (map parse-inline inlines)]))
                   :HorizontalRule (constantly nil)
                   (fn [b] b))]
    [block-type (parse-fn block)]))

(defn parse-meta-value [mv]
  (case (keyword (:t mv))
    :MetaMap (into {} (map (fn [[k v]] [k (parse-meta-value v)]) (:c mv)))
    :MetaList (map parse-meta-value (:c mv))
    :MetaBool (:c mv)
    :MetaInlines (map parse-inline (:c mv))
    :MetaString (:c mv)
    mv))

(defn parse-meta [meta]
  (into {} (map (fn [[k v]] [k (parse-meta-value v)]) meta)))

(defn parse-pandoc [json]
  {:meta   (parse-meta (:meta json)),
   :blocks (map parse-block (:blocks json))})

(comment
  (parse-pandoc
    {"pandoc-api-version" [1, 23, 1],
     "meta"               {"test" {"t" "MetaInlines", "c" [{"t" "Str", "c" "val"}]}},
     "blocks"             [{"t" "Para", "c" [{"t" "Str", "c" "paragraph"}]},
                           {"t" "BulletList", "c" [[{"t" "Plain", "c" [{"t" "Str", "c" "list"}, {"t" "Space"}, {"t" "Str", "c" "item"}]}],
                                                   [{"t" "Plain", "c" [{"t" "Str", "c" "list"}, {"t" "Space"}, {"t" "Str", "c" "item"}]}]]},
                           {"t" "Para", "c" [{"t" "Str", "c" "para"}, {"t" "Space"}, {"t" "Str", "c" "2"}]},
                           {"t" "Header", "c" [2, ["header", [], []], [{"t" "Str", "c" "Header"}]]},
                           {"t" "BulletList", "c" [[{"t" "Plain", "c" [{"t" "Str", "c" "list"}, {"t" "Space"}, {"t" "Str", "c" "item"}]}]]},
                           {"t" "Header", "c" [2, ["links", [], []], [{"t" "Str", "c" "Links"}]]},
                           {"t" "HorizontalRule"},
                           {"t" "BulletList", "c" [[{"t" "Plain", "c" [{"t" "Link", "c" [["", [], []], [{"t" "Str", "c" "test1"}], ["f5bad274", ""]]}]}],
                                                   [{"t" "Plain", "c" [{"t" "Link", "c" [["", [], []], [{"t" "Str", "c" "test2"}], ["8a75915f", ""]]}]}]]}]}))
