(ns spec
  (:require
    [clojure.spec.alpha :as s]))

(s/def ::format string?)

(s/def ::meta (s/map-of string? :spec/meta-value))
(s/def ::meta-value (s/or :map ::meta
                          :list (s/coll-of ::meta-value)
                          :bool boolean?
                          :string string?
                          :inlines (s/coll-of :spec/inline)
                          :blocks (s/coll-of :spec/block)))

(s/def ::alignment #{:align-left :align-right :align-center :align-default})
(s/def ::col-width (s/nilable (s/and number? pos?)))
(s/def ::col-spec (s/tuple ::alignment (s/nilable ::col-width)))
(s/def ::row (s/tuple ::attr (s/coll-of :spec/cell)))
(s/def ::row-head-columns int?)
(s/def ::table-head (s/tuple ::attr (s/coll-of :spec/row)))
(s/def ::table-body (s/tuple ::attr ::row-head-columns
                             (s/coll-of :spec/row)
                             (s/coll-of :spec/row)))
(s/def ::table-foot (s/tuple ::attr (s/coll-of :spec/row)))
(s/def ::caption (s/tuple (s/nilable (s/coll-of :spec/inline))
                          (s/coll-of :spec/block)))
(s/def ::cell (s/tuple ::attr ::alignment int? int?
                       (s/coll-of :spec/block)))

(s/def ::list-attributes (s/tuple int?
                                  :spec/list-number-style
                                  :spec/list-number-delim))
(s/def ::list-number-style #{:default-style :example :decimal
                             :lower-roman :upper-roman
                             :lower-alpha :upper-alpha})
(s/def ::list-number-delim #{:default-delim :period :one-paren :two-parens})
(s/def ::attr (s/tuple string? (s/coll-of string?) (s/map-of string? string?)))

(s/def ::quote-type #{:single-quote :double-quote})
(s/def ::target (s/tuple string? string?))
(s/def ::math-type #{:display-math :inline-math})

(s/def ::block (s/or :plain (s/coll-of :spec/inline)
                     :para (s/coll-of :spec/inline)
                     :line-block (s/coll-of (s/coll-of :spec/inline))
                     :code-block (s/tuple ::attr string?)
                     :raw-block (s/tuple ::format string?)
                     :block-quote (s/coll-of :spec/block)
                     :ordered-list ::list-attributes
                     :bullet-list (s/coll-of (s/coll-of :spec/block))
                     :definition-list (s/coll-of (s/tuple (s/coll-of :spec/inline)
                                                          (s/coll-of (s/coll-of :spec/block))))
                     :header (s/tuple int? ::attr (s/coll-of :spec/inline))
                     :horizontal-rule nil?
                     :table (s/tuple ::attr ::caption
                                     (s/coll-of ::col-spec)
                                     ::table-head
                                     (s/coll-of ::table-body)
                                     ::table-foot)
                     :figure (s/tuple ::attr ::caption
                                      (s/coll-of :spec/block))
                     :div (s/tuple ::attr (s/coll-of :spec/block))))


(s/def ::citation-id string?)
(s/def ::citation-prefix (s/coll-of :spec/inline))
(s/def ::citation-suffix (s/coll-of :spec/inline))
(s/def ::citation-note-num int?)
(s/def ::citation-hash int?)
(s/def ::citation-mode #{:author-in-text :suppress-author :normal-citation})
(s/def ::citation (s/keys :req-un [::citation-id ::citation-prefix
                                   ::citation-suffix ::citation-mode
                                   ::citation-note-num ::citation-hash]))
(s/def ::inline (s/or :str string?
                      :emph (s/coll-of :spec/inline)
                      :underline (s/coll-of :spec/inline)
                      :strong (s/coll-of :spec/inline)
                      :strikeout (s/coll-of :spec/inline)
                      :superscript (s/coll-of :spec/inline)
                      :subscript (s/coll-of :spec/inline)
                      :small-caps (s/coll-of :spec/inline)
                      :quoted (s/tuple ::quote-type (s/coll-of :spec/inline))
                      :cite (s/tuple (s/coll-of ::citation) (s/coll-of :spec/inline))
                      :code (s/tuple ::attr string?)
                      :space nil?
                      :soft-break nil?
                      :line-break nil?
                      :math (s/tuple ::math-type string?)
                      :raw-inline (s/tuple ::format string?)
                      :link (s/tuple ::attr (s/coll-of :spec/inline) ::target)
                      :image (s/tuple ::attr (s/coll-of :spec/inline) ::target)
                      :note (s/coll-of :spec/block)
                      :span (s/tuple ::attr (s/coll-of :spec/inline))))

(s/def ::pandoc (s/tuple ::meta (s/coll-of :spec/block)))


