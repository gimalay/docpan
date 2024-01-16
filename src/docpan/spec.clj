(ns docpan.spec
  (:require
    [clojure.spec.alpha :as s]
    [clojure.test :refer :all]))

(s/def ::attr (s/or
                :id (s/tuple string?)
                :id-class (s/tuple string? (s/coll-of string?))
                :id-kv (s/tuple string? (s/map-of string? string?))
                :id-class-kv (s/tuple string? (s/coll-of string?) (s/map-of string? string?))
                :class (s/tuple (s/coll-of string?))
                :class-kv (s/tuple (s/coll-of string?) (s/map-of string? string?))))

(deftest test-attr
  (is (s/valid? ::attr ["id" ["class"] {"key" "value"}]))
  (is (s/valid? ::attr ["id" ["class"] {}]))
  (is (s/valid? ::attr ["id" ["class"]]))
  (is (s/valid? ::attr [["class"]]))
  (is (s/valid? ::attr ["id" {}]))
  (is (s/valid? ::attr ["id" [] {}]))
  (is (s/valid? ::attr ["id" [] {"key" "value"}]))
  (is (s/valid? ::attr ["id" [] {}])))

(s/def ::emph (s/tuple
                (s/and keyword? #(= % :emph))
                (s/coll-of ::inline)))

(deftest test-emph
  (is (s/valid? ::emph [:emph ["test"]]))
  (is (s/valid? ::emph [:emph ["string" [:emph ["another string"]]]]))
  (is (not (s/valid? ::emph [:other ["test"]]))))

(s/def ::underline (s/tuple
                     (s/and keyword? #(= % :underline))
                     (s/coll-of ::inline)))

(deftest test-underline
  (is (s/valid? ::underline [:underline ["test"]]))
  (is (s/valid? ::underline [:underline ["string" [:underline ["another string"]]]]))
  (is (not (s/valid? ::underline [:other ["test"]]))))

(s/def ::strong (s/tuple
                  (s/and keyword? #(= % :strong))
                  (s/coll-of ::inline)))

(deftest test-strong
  (is (s/valid? ::strong [:strong ["test"]]))
  (is (s/valid? ::strong [:strong ["string" [:strong ["another string"]]]]))
  (is (not (s/valid? ::strong [:other ["test"]]))))


(s/def ::link-no-attrs (s/tuple
                         (s/and keyword? #(= % :link))
                         (s/coll-of ::inline)
                         string?))

(deftest test-link-no-attrs
  (is (s/valid? ::link-no-attrs [:link ["test"] "target"])))

(s/def ::link-with-attrs (s/tuple
                           (s/and keyword? #(= % :link))
                           (s/coll-of ::inline)
                           string?
                           ::attr))

(deftest test-link-with-attrs
  (is (s/valid? ::link-with-attrs [:link ["test"] "target" ["id" ["class"] {"key" "value"}]]))
  (is (s/valid? ::link-with-attrs [:link ["test"] "target" ["id" ["class"] {}]]))
  (is (s/valid? ::link-with-attrs [:link ["test"] "target" ["id"]])))

(s/def ::link (s/or
                :no-attr (s/tuple
                           (s/and keyword? #(= % :link))
                           (s/coll-of ::inline)
                           string?)
                :attrs (s/tuple
                         (s/and keyword? #(= % :link))
                         (s/coll-of ::inline)
                         string?
                         ::attr)))

(deftest test-link
  (is (s/valid? ::link [:link ["test"] "target"]))
  (is (s/valid? ::link [:link ["test"] "target" ["id" ["class"] {"key" "value"}]]))
  (is (s/valid? ::link [:link ["test"] "target" [["class"] {"key" "value"}]])))

(s/def ::inline (s/or :str string?
                      :emph ::emph
                      :strong ::strong
                      :underline ::underline))

(deftest test-inline
  (is (s/valid? ::inline "test"))
  (is (s/valid? ::inline [:emph ["test"]]))
  (is (s/valid? ::inline [:underline ["test"]])))



(s/def ::plain (s/tuple (s/and keyword? #(= % :plain))
                        (s/coll-of ::inline)))

(deftest test-plain
  (is (s/valid? ::plain [:plain ["test"]]))
  (is (s/valid? ::plain [:plain ["test" [:emph ["test"]]]])))

(s/def ::para (s/tuple
                (s/and keyword? #(= % :para))
                (s/coll-of ::inline)))

(deftest test-para
  (is (s/valid? ::para [:para ["test"]]))
  (is (s/valid? ::para [:para ["test" "test2"]]))
  (is (s/valid? ::para [:para ["test" [:emph ["test"]]]])))



(s/def ::code-block (s/or
                      :no-attr (s/tuple
                                 (s/and keyword? #(= % :code-block))
                                 string?)
                      :attr (s/tuple
                              (s/and keyword? #(= % :code-block))
                              string?
                              ::attr)))

(deftest test-code-block
  (is (s/valid? ::code-block [:code-block "test"]))
  (is (s/valid? ::code-block [:code-block "test" ["id"]])))


(s/def ::format string?)

(s/def ::raw-block (s/or
                     :no-format (s/tuple string?)
                     :with-format (s/tuple string? ::format)))

(deftest test-raw-block
  (is (s/valid? ::raw-block ["text"]))
  (is (s/valid? ::raw-block ["text" "format"])))

(s/def ::bullet-list (s/tuple
                       (s/and keyword? #(= % :bullet-list))
                       (s/coll-of (s/coll-of ::block))))

(deftest test-bullet-list
  (is (s/valid? ::bullet-list [:bullet-list [[:para "test"]
                                             [:para "test"]]])))


(s/def ::horizontal-rule (s/tuple
                           (s/and keyword? #(= % :horizontal-rule))))

(s/def ::header (s/or
                  :no-attr (s/tuple
                             (s/and keyword? #(= % :header))
                             int?
                             (s/coll-of ::inline))
                  :with-attr (s/tuple
                               (s/and keyword? #(= % :header))
                               int?
                               (s/coll-of ::inline)
                               ::attr)))

(s/def ::block (s/or
                 :plain ::plain
                 :para ::para
                 :code-block ::code-block
                 :raw-block ::raw-block
                 :ordered-list ::bullet-list
                 :bullet-list ::bullet-list
                 :header ::header
                 :horizontal-rule ::horizontal-rule))