(ns bvm.sketches.rec-series.studies.additive-rings
  (:require
   [bvm.core :as bvm]
   [bvm.utils.color :as color]
   [bvm.fields.rec-ring-progression :refer [rec-ring-progression]])
  (:import
   [java.util Random]))

(def palette
  {:sand [80.56 10.76 101.48]
   :acqua [80 54.13 255]
   :orange [75.59 102.47 70.94]
   :pink [59.41 99.57 343.06]})

(def artwork
  {:background-rgb (color/lch->rgb-bytes (:sand palette) :srgb)
   :canvas-width 12000
   :canvas-height 6000})

(def base-ring
  {:num-rings 17
   :particles-per-ring-initial 90
   :particles-per-ring-final 360
   :blend-mode :multiply

   :align-to-path true

   ;; Inner -> outer ring
   :ellipse-width-initial 0.08
   :ellipse-width-final 0.43
   :ellipse-height-initial 0.08
   :ellipse-height-final 0.43

   ;; Full ring, slight rotational drift across field
   :start-initial 0.0
   :start-final 0.15
   :completion-initial 1.0

   :center-x-initial 0.5
   :center-y-initial 0.5

   ;; Small tangent-aligned rects; grow slightly outward
   :obj-width-initial 0.04
   :obj-height-initial 0.02
   :obj-width-final 0.02
   :obj-height-final 0.004

   ;; Layout jitter grows outward
   :layout-variance-initial 0.0
   :layout-variance-final 0.12

   :vertex-variance-initial 0.0
   :vertex-variance-final 0.25

   :size-variance-initial 0.0
   :size-variance-final 0.2

   ;; Fill off, stroke carries the colour.
   :fill-opacity-initial-start 0
   :fill-opacity-initial-end 0
   :fill-opacity-final-start 0
   :fill-opacity-final-end 0

   :fill-color-initial-start (:acqua palette)
   :fill-color-final-start (:acqua palette)

   ;; Stroke colour: magenta at the inner ring, gold at the outer ring.
   ;; Within each ring, fade from the ring's base hue toward the other
   ;; pole so the two fields cross-hatch perceptually.
   :stroke-color-initial-start (:acqua palette)
   :stroke-color-initial-end   (:acqua palette)
   :stroke-color-final-start   (:orange palette)
   :stroke-color-final-end     (:orange palette)

   :stroke-width-initial-start 1
   :stroke-width-initial-end 1
   :stroke-width-final-start 2
   :stroke-width-final-end 2

   :stroke-opacity-initial-start 200
   :stroke-opacity-initial-end 40
   :stroke-opacity-final-start 220
   :stroke-opacity-final-end 60

   :seed-base-layout 100
   :seed-step-layout 17
   :seed-base-drawing 500
   :seed-step-drawing 23})

(def ring-2
  (assoc base-ring
         :blend-mode :add
         :center-x-initial 0.5
         :center-y-initial 0.5))

(def rings [base-ring])

(defn random-ring
  ([base seed] (random-ring base seed 0.075 0.075))
  ([base seed w-scale h-scale]
   (let [rng (Random. seed)
         wi (* w-scale (.nextDouble rng))
         hi (* h-scale (.nextDouble rng))
         wf (* w-scale (.nextDouble rng))
         hf (* h-scale (.nextDouble rng))
         rx (.nextDouble rng)
         ry (.nextDouble rng)]
     (assoc base
            :ellipse-width-initial wi
            :ellipse-height-initial hi
            :ellipse-width-final wf
            :ellipse-height-final hf
            :center-x-initial rx
            :center-y-initial ry))))

(defn random-rings [n seed base-config transform-fn]
  (let [r (Random. seed)]
    (into []
          (repeatedly n #(transform-fn base-config (.nextInt r))))))

; Claudio
(defn grid-rings [margin cols rows col-width row-height seed base-config]
  (let [r (Random. seed)]
    (into []
          (for [row (range rows)
                col (range cols)]
            (let [cx (+ margin (* col-width (+ col 0.5)))
                  cy (+ margin (* row-height (+ row 0.5)))
                  ring (random-ring base-config (.nextInt r) col-width row-height)
                  fmt (fn [x] (format "%.3f" (double x)))]
              (println (str (inc col) "," (inc row) ": "
                            "ewi " (fmt (:ellipse-width-initial ring))
                            " ehi " (fmt (:ellipse-height-initial ring))
                            " ewf " (fmt (:ellipse-width-final ring))
                            " ehf " (fmt (:ellipse-height-final ring))
                            " cx " (fmt cx) " cy " (fmt cy)))
              (assoc ring
                     :center-x-initial cx
                     :center-y-initial cy))))))

(def layers
  (into [] (mapcat rec-ring-progression) (grid-rings 0.15 12 6 0.0585 0.117 7 base-ring)))

(def config
  {:renderer :java2d
   :filename "additive-rings"
   :background-color (:background-rgb artwork)
   :canvas {:canvas-width (:canvas-width artwork)
            :canvas-height (:canvas-height artwork)}
   :layers layers})

(defn -main [& _args]
  (bvm/vera-multi config))
