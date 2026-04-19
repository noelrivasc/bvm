(ns bvm.fields.rec-wave-progression
  "A field that emits a vector of layers arranged as a progression of
   wavy bands (hardwired to wavy-band + linear-fade + rectangle).

   Two axes of progression:
     - initial / final : across the field axis (first band -> last band).
     - start / end     : within a stroke (consumed by lch-fade).

   Colour params are LCH tuples [L C H] (L in [0,100], C in [0,~130],
   H in degrees). Interpolation happens in LCH on both axes; conversion
   to RGB bytes happens inside the style fn at the very last step. The
   target gamut for that conversion is controlled by :color-space
   (:srgb, default; or :adobe-rgb).

   Every progression accepts an `-ease` fn with signature (t -> t), both in
   [0, 1]. Easing is applied on the field axis only. Within-stroke
   interpolation stays linear via lch-fade.

   Defaults:
     - Missing `-final` defaults to its `-initial` counterpart.
     - Missing `-*-end` style value defaults to the matching `-*-start`.
     - Missing `-final-*` style pair defaults to the matching `-initial-*`.
     - Missing `-ease` defaults to identity (linear)."
  (:require [clojure.spec.alpha :as s]
            [bvm.specs :as bspec]
            [bvm.utils.interpolation :refer [lerp]]
            [bvm.utils.color :refer [lerp-lch]]
            [bvm.layouts.wavy-band :refer [wavy-band]]
            [bvm.styles.lch-fade :refer [lch-fade]]
            [bvm.drawing.rectangle :refer [rectangle]]))

;; -----------------------------------------------------------------------------
;; Spec
;; -----------------------------------------------------------------------------

(s/def ::ease-fn ifn?)

(s/def ::unit-float (s/and number? #(<= 0 % 1)))
(s/def ::non-neg-number (s/and number? #(<= 0 %)))

;; LCH colour: [L C H]. Kept loose — out-of-gamut values get silent-clamped
;; downstream by `lch->rgb-bytes`.
(s/def ::lch-color
  (s/tuple number? number? number?))

(s/def ::color-space #{:srgb :adobe-rgb})

;; Top-level / shape of the field
(s/def ::num-bands pos-int?)
(s/def ::particles-per-band-initial pos-int?)
(s/def ::particles-per-band-final pos-int?)
(s/def ::particles-per-band-ease ::ease-fn)
(s/def ::blend-mode ::bspec/blend-mode)

(s/def ::seed-base-layout int?)
(s/def ::seed-step-layout int?)
(s/def ::seed-base-drawing int?)
(s/def ::seed-step-drawing int?)

;; Scalar layout params (constant across field)
(s/def ::phase boolean?)
(s/def ::direction #{:horizontal :vertical})

;; Layout progressions: each has -initial, -final, -ease
(defmacro ^:private def-progression [name-sym pred]
  (let [base (name name-sym)
        ini (keyword (str *ns*) (str base "-initial"))
        fin (keyword (str *ns*) (str base "-final"))
        ease (keyword (str *ns*) (str base "-ease"))]
    `(do
       (s/def ~ini ~pred)
       (s/def ~fin ~pred)
       (s/def ~ease ::ease-fn))))

(def-progression amplitude ::unit-float)
(def-progression frequency ::non-neg-number)
(def-progression length ::unit-float)
(def-progression offset number?)
(def-progression field-position ::unit-float)
(def-progression obj-width ::unit-float)
(def-progression obj-height ::unit-float)
(def-progression vertex-variance ::unit-float)
(def-progression layout-variance ::unit-float)
(def-progression size-variance ::non-neg-number)

;; Style progressions: 4 corners + field-ease (within-stroke stays linear)
(defmacro ^:private def-style-progression [name-sym pred]
  (let [base (name name-sym)
        is (keyword (str *ns*) (str base "-initial-start"))
        ie (keyword (str *ns*) (str base "-initial-end"))
        fs (keyword (str *ns*) (str base "-final-start"))
        fe (keyword (str *ns*) (str base "-final-end"))
        ease (keyword (str *ns*) (str base "-field-ease"))]
    `(do
       (s/def ~is ~pred)
       (s/def ~ie ~pred)
       (s/def ~fs ~pred)
       (s/def ~fe ~pred)
       (s/def ~ease ::ease-fn))))

(def-style-progression stroke-width ::non-neg-number)
(def-style-progression stroke-color ::lch-color)
(def-style-progression stroke-opacity (s/int-in 0 256))
(def-style-progression fill-color ::lch-color)
(def-style-progression fill-opacity (s/int-in 0 256))

(s/def ::config
  (s/keys :req-un [::num-bands ::particles-per-band-initial ::blend-mode]
          :opt-un [::particles-per-band-final ::particles-per-band-ease
                   ::color-space
                   ::seed-base-layout ::seed-step-layout
                   ::seed-base-drawing ::seed-step-drawing
                   ::phase ::direction
                   ::amplitude-initial ::amplitude-final ::amplitude-ease
                   ::frequency-initial ::frequency-final ::frequency-ease
                   ::length-initial ::length-final ::length-ease
                   ::offset-initial ::offset-final ::offset-ease
                   ::field-position-initial ::field-position-final ::field-position-ease
                   ::obj-width-initial ::obj-width-final ::obj-width-ease
                   ::obj-height-initial ::obj-height-final ::obj-height-ease
                   ::vertex-variance-initial ::vertex-variance-final ::vertex-variance-ease
                   ::layout-variance-initial ::layout-variance-final ::layout-variance-ease
                   ::size-variance-initial ::size-variance-final ::size-variance-ease
                   ::stroke-width-initial-start ::stroke-width-initial-end
                   ::stroke-width-final-start ::stroke-width-final-end
                   ::stroke-width-field-ease
                   ::stroke-color-initial-start ::stroke-color-initial-end
                   ::stroke-color-final-start ::stroke-color-final-end
                   ::stroke-color-field-ease
                   ::stroke-opacity-initial-start ::stroke-opacity-initial-end
                   ::stroke-opacity-final-start ::stroke-opacity-final-end
                   ::stroke-opacity-field-ease
                   ::fill-color-initial-start ::fill-color-initial-end
                   ::fill-color-final-start ::fill-color-final-end
                   ::fill-color-field-ease
                   ::fill-opacity-initial-start ::fill-opacity-initial-end
                   ::fill-opacity-final-start ::fill-opacity-final-end
                   ::fill-opacity-field-ease]))

;; -----------------------------------------------------------------------------
;; Helpers
;; -----------------------------------------------------------------------------

(defn- lerp-field
  "Interpolate from initial -> final at field-depth t, applying ease to t.
   final defaults to initial; ease defaults to identity.
   lerp-fn lets callers pass e.g. lerp-rgb for colors."
  ([config base-key t] (lerp-field config base-key t lerp))
  ([config base-key t lerp-fn]
   (let [ini-k (keyword (str (name base-key) "-initial"))
         fin-k (keyword (str (name base-key) "-final"))
         ease-k (keyword (str (name base-key) "-ease"))
         initial (get config ini-k)
         final (get config fin-k initial)
         ease (get config ease-k identity)]
     (lerp-fn initial final (ease t)))))

(defn- resolve-style-corners
  "For a 4-corner style param, returns [initial-for-this-band final-for-this-band]
   after collapsing the field axis at depth t. Within-stroke (start->end) is
   preserved for the style fn to handle."
  [config base t lerp-fn]
  (let [is (get config (keyword (str base "-initial-start")))
        ie (get config (keyword (str base "-initial-end")) is)
        fs (get config (keyword (str base "-final-start")) is)
        fe (get config (keyword (str base "-final-end")) ie)
        ease (get config (keyword (str base "-field-ease")) identity)
        et (ease t)]
    [(lerp-fn is fs et)
     (lerp-fn ie fe et)]))

;; -----------------------------------------------------------------------------
;; The field
;; -----------------------------------------------------------------------------

(defn rec-wave-progression
  "Produces a vector of layer-configs (::bvm.specs/layers) arranged as a
   field of wavy bands. See the ns docstring for conventions."
  [config]
  (let [num-bands (:num-bands config)
        blend-mode (:blend-mode config)
        color-space (get config :color-space :srgb)
        phase (get config :phase false)
        direction (get config :direction :horizontal)
        seed-base-layout (get config :seed-base-layout 0)
        seed-step-layout (get config :seed-step-layout 0)
        seed-base-drawing (get config :seed-base-drawing 0)
        seed-step-drawing (get config :seed-step-drawing 0)]
    (mapv
     (fn [i]
       (let [depth (if (<= num-bands 1) 0.0 (/ (double i) (dec num-bands)))
             ;; Layout progressions
             amplitude (lerp-field config :amplitude depth)
             frequency (lerp-field config :frequency depth)
             length (lerp-field config :length depth)
             offset (lerp-field config :offset depth)
             position (lerp-field config :field-position depth)
             obj-width (lerp-field config :obj-width depth)
             obj-height (lerp-field config :obj-height depth)
             vertex-var (lerp-field config :vertex-variance depth)
             layout-var (lerp-field config :layout-variance depth)
             size-var (lerp-field config :size-variance depth)
             particles (int (lerp-field config :particles-per-band depth))
             ;; Style 4-corners -> per-band initial/final for lch-fade
             [sw-i sw-f] (resolve-style-corners config "stroke-width" depth lerp)
             [sc-i sc-f] (resolve-style-corners config "stroke-color" depth lerp-lch)
             [so-i so-f] (resolve-style-corners config "stroke-opacity" depth lerp)
             [fc-i fc-f] (resolve-style-corners config "fill-color" depth lerp-lch)
             [fo-i fo-f] (resolve-style-corners config "fill-opacity" depth lerp)]
         {:num-steps particles
          :layout-fn wavy-band
          :blend-mode blend-mode
          :layout-options {:amplitude (float amplitude)
                           :frequency (float frequency)
                           :length (float length)
                           :offset (float offset)
                           :position (float position)
                           :object-width (float obj-width)
                           :object-height (float obj-height)
                           :variance (float layout-var)
                           :phase phase
                           :direction direction
                           :seed (+ seed-base-layout (* i seed-step-layout))}
          :style-fn lch-fade
          :style-options {:color-space color-space
                          :initial-fill-color fc-i
                          :final-fill-color fc-f
                          :initial-fill-opacity (int fo-i)
                          :final-fill-opacity (int fo-f)
                          :initial-stroke-color sc-i
                          :final-stroke-color sc-f
                          :initial-stroke-width (int sw-i)
                          :final-stroke-width (int sw-f)
                          :initial-stroke-opacity (int so-i)
                          :final-stroke-opacity (int so-f)}
          :draw-fn rectangle
          :drawing-options {:vertex-variance (float vertex-var)
                            :size-variance (float size-var)
                            :seed (+ seed-base-drawing (* i seed-step-drawing))}}))
     (range num-bands))))
