(ns bvm.fields.rec-ring-progression
  "A field that emits a vector of layers arranged as a progression of
   concentric ring strokes (hardwired to ring + lch-fade + rectangle).

   Two axes of progression:
     - initial / final : across the field axis (first ring -> last ring).
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
            [bvm.layouts.ring :refer [ring]]
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
(s/def ::num-rings pos-int?)
(s/def ::particles-per-ring-initial pos-int?)
(s/def ::particles-per-ring-final pos-int?)
(s/def ::particles-per-ring-ease ::ease-fn)
(s/def ::blend-mode ::bspec/blend-mode)

(s/def ::seed-base-layout int?)
(s/def ::seed-step-layout int?)
(s/def ::seed-base-drawing int?)
(s/def ::seed-step-drawing int?)

;; Scalar layout params (constant across field)
(s/def ::align-to-path boolean?)

;; Layout progressions: each has -initial, -final, -ease

(s/def ::ellipse-width-initial ::unit-float)
(s/def ::ellipse-width-final ::unit-float)
(s/def ::ellipse-width-ease ::ease-fn)

(s/def ::ellipse-height-initial ::unit-float)
(s/def ::ellipse-height-final ::unit-float)
(s/def ::ellipse-height-ease ::ease-fn)

(s/def ::obj-width-initial ::unit-float)
(s/def ::obj-width-final ::unit-float)
(s/def ::obj-width-ease ::ease-fn)

(s/def ::obj-height-initial ::unit-float)
(s/def ::obj-height-final ::unit-float)
(s/def ::obj-height-ease ::ease-fn)

(s/def ::start-initial number?)
(s/def ::start-final number?)
(s/def ::start-ease ::ease-fn)

(s/def ::completion-initial ::unit-float)
(s/def ::completion-final ::unit-float)
(s/def ::completion-ease ::ease-fn)

(s/def ::center-x-initial ::unit-float)
(s/def ::center-x-final ::unit-float)
(s/def ::center-x-ease ::ease-fn)

(s/def ::center-y-initial ::unit-float)
(s/def ::center-y-final ::unit-float)
(s/def ::center-y-ease ::ease-fn)

(s/def ::vertex-variance-initial ::unit-float)
(s/def ::vertex-variance-final ::unit-float)
(s/def ::vertex-variance-ease ::ease-fn)

(s/def ::layout-variance-initial ::unit-float)
(s/def ::layout-variance-final ::unit-float)
(s/def ::layout-variance-ease ::ease-fn)

(s/def ::size-variance-initial ::non-neg-number)
(s/def ::size-variance-final ::non-neg-number)
(s/def ::size-variance-ease ::ease-fn)

;; Style progressions: 4 corners + field-ease (within-stroke stays linear)

(s/def ::stroke-width-initial-start ::non-neg-number)
(s/def ::stroke-width-initial-end ::non-neg-number)
(s/def ::stroke-width-final-start ::non-neg-number)
(s/def ::stroke-width-final-end ::non-neg-number)
(s/def ::stroke-width-field-ease ::ease-fn)

(s/def ::stroke-color-initial-start ::lch-color)
(s/def ::stroke-color-initial-end ::lch-color)
(s/def ::stroke-color-final-start ::lch-color)
(s/def ::stroke-color-final-end ::lch-color)
(s/def ::stroke-color-field-ease ::ease-fn)

(s/def ::stroke-opacity-initial-start (s/int-in 0 256))
(s/def ::stroke-opacity-initial-end (s/int-in 0 256))
(s/def ::stroke-opacity-final-start (s/int-in 0 256))
(s/def ::stroke-opacity-final-end (s/int-in 0 256))
(s/def ::stroke-opacity-field-ease ::ease-fn)

(s/def ::fill-color-initial-start ::lch-color)
(s/def ::fill-color-initial-end ::lch-color)
(s/def ::fill-color-final-start ::lch-color)
(s/def ::fill-color-final-end ::lch-color)
(s/def ::fill-color-field-ease ::ease-fn)

(s/def ::fill-opacity-initial-start (s/int-in 0 256))
(s/def ::fill-opacity-initial-end (s/int-in 0 256))
(s/def ::fill-opacity-final-start (s/int-in 0 256))
(s/def ::fill-opacity-final-end (s/int-in 0 256))
(s/def ::fill-opacity-field-ease ::ease-fn)

(s/def ::config
  (s/keys :req-un [::num-rings ::particles-per-ring-initial ::blend-mode]
          :opt-un [::particles-per-ring-final ::particles-per-ring-ease
                   ::color-space
                   ::seed-base-layout ::seed-step-layout
                   ::seed-base-drawing ::seed-step-drawing
                   ::align-to-path
                   ::ellipse-width-initial ::ellipse-width-final ::ellipse-width-ease
                   ::ellipse-height-initial ::ellipse-height-final ::ellipse-height-ease
                   ::obj-width-initial ::obj-width-final ::obj-width-ease
                   ::obj-height-initial ::obj-height-final ::obj-height-ease
                   ::start-initial ::start-final ::start-ease
                   ::completion-initial ::completion-final ::completion-ease
                   ::center-x-initial ::center-x-final ::center-x-ease
                   ::center-y-initial ::center-y-final ::center-y-ease
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
   lerp-fn lets callers pass e.g. lerp-lch for colors."
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
  "For a 4-corner style param, returns [initial-for-this-ring final-for-this-ring]
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

(defn rec-ring-progression
  "Produces a vector of layer-configs (::bvm.specs/layers) arranged as a
   field of concentric ring strokes. See the ns docstring for conventions."
  [config]
  (let [num-rings (:num-rings config)
        blend-mode (:blend-mode config)
        color-space (get config :color-space :srgb)
        align-to-path (get config :align-to-path false)
        seed-base-layout (get config :seed-base-layout 0)
        seed-step-layout (get config :seed-step-layout 0)
        seed-base-drawing (get config :seed-base-drawing 0)
        seed-step-drawing (get config :seed-step-drawing 0)]
    (mapv
     (fn [i]
       (let [depth (if (<= num-rings 1) 0.0 (/ (double i) (dec num-rings)))
             ;; Layout progressions
             ew (lerp-field config :ellipse-width depth)
             eh (lerp-field config :ellipse-height depth)
             ow (lerp-field config :obj-width depth)
             oh (lerp-field config :obj-height depth)
             st (lerp-field config :start depth)
             cp (lerp-field config :completion depth)
             cx (lerp-field config :center-x depth)
             cy (lerp-field config :center-y depth)
             vertex-var (lerp-field config :vertex-variance depth)
             layout-var (lerp-field config :layout-variance depth)
             size-var (lerp-field config :size-variance depth)
             particles (int (lerp-field config :particles-per-ring depth))
             ;; Style 4-corners -> per-ring initial/final for lch-fade
             [sw-i sw-f] (resolve-style-corners config "stroke-width" depth lerp)
             [sc-i sc-f] (resolve-style-corners config "stroke-color" depth lerp-lch)
             [so-i so-f] (resolve-style-corners config "stroke-opacity" depth lerp)
             [fc-i fc-f] (resolve-style-corners config "fill-color" depth lerp-lch)
             [fo-i fo-f] (resolve-style-corners config "fill-opacity" depth lerp)]
         {:num-steps particles
          :layout-fn ring
          :blend-mode blend-mode
          :layout-options {:ellipse-width (float ew)
                           :ellipse-height (float eh)
                           :object-width (float ow)
                           :object-height (float oh)
                           :start (float st)
                           :completion (float cp)
                           :center-x (float cx)
                           :center-y (float cy)
                           :align-to-path align-to-path
                           :variance (float layout-var)
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
     (range num-rings))))
