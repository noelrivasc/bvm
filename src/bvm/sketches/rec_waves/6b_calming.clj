(ns bvm.sketches.rec-waves.6b-calming
  (:require
   [bvm.core :as bvm]
   [bvm.utils.interpolation :as interpol]
   [bvm.fields.rec-wave-progression :refer [rec-wave-progression]]
   [bvm.utils.color :as color]))

(def palette
  {:gold [60 88.93 88.41]
   :magenta [37.55 72.08 350.19]
   :pink [43 72.08 350.19]
   :dead-purple [9.41 17.4 286.89]})

(def artwork
  (let [canvas-width-mm 900
        canvas-height-mm 600
        in-to-mm 25.4
        canvas-width-in (/ canvas-width-mm in-to-mm)
        canvas-height-in (/ canvas-height-mm in-to-mm)
        canvas-dpi 300]
    {:background-rgb (color/lch->rgb-bytes (:dead-purple palette) :srgb)
     :block-lch (:magenta palette)
     :block-light-lch (:pink palette)
     :canvas-width-mm canvas-width-mm
     :canvas-height-mm canvas-height-mm
     :canvas-width (Math/round (* canvas-width-in canvas-dpi))
     :canvas-height (Math/round (* canvas-height-in canvas-dpi))}))

(def helper-values
  (as->
   {:gap (/ 50 (:canvas-width-mm artwork))
    :block-width (/ 80 (:canvas-width-mm artwork))
    :margin (/ 100 (:canvas-width-mm artwork))} v
    (assoc v
           :block-offset (+ (:gap v) (:block-width v))
           :block-height (- 1 (* 2 (:margin v))))))

(def wh-ratio (/ (:canvas-width artwork) (:canvas-height artwork)))

(def base-block
  {:num-bands 57
   :particles-per-band-initial 17
   :particles-per-band-final 17
   :blend-mode :blend

   :field-position-initial (:margin helper-values)
   :field-position-final (- 1 (:margin helper-values))

   :amplitude-initial 0.0
   :amplitude-final 0.0

   :frequency-initial 2.0
   :length-initial (:block-width helper-values)
   :offset-initial (+ (* 2.5 (:gap helper-values))
                      (* 2.5 (:block-width helper-values)))

   :layout-variance-initial 0.0
   :layout-variance-final 0.0

   :phase true
   :direction :horizontal

   :vertex-variance-initial 0.0
   :vertex-variance-final 0.0

   :size-variance-initial 0.0
   :size-variance-final 0.0

   :obj-width-initial 0.005
   :obj-width-final 0.005
   :obj-height-initial (* 0.005 wh-ratio)
   :obj-height-final (* 0.005 wh-ratio)

   ;; Fill colour — all four corners are the same magenta here.
   :fill-color-initial-start (:block-light-lch artwork)
   :fill-color-initial-end (:block-light-lch artwork)
   :fill-color-final-start (:block-lch artwork)
   :fill-color-final-end (:block-lch artwork)

   ;; Stroke colour — offset was 0 in the old base-block, so same corners.
   :stroke-color-initial-start (:block-lch artwork)
   :stroke-color-initial-end (:block-lch artwork)
   :stroke-color-final-start (:block-lch artwork)
   :stroke-color-final-end (:block-lch artwork)

   ;; Fill opacity — constant within stroke, varies across the field
   ;; (start = 0 at band 0, end = 5 at band N-1).
   :fill-opacity-initial-start 0
   :fill-opacity-initial-end 0
   :fill-opacity-final-start 25
   :fill-opacity-final-end 25

   ;; Stroke width — same pattern; old sketch kept it constant at 1.
   :stroke-width-initial-start 2
   :stroke-width-initial-end 2
   :stroke-width-final-start 2
   :stroke-width-final-end 2

   ;; Stroke opacity — constant across field, varies within stroke (both 255 here).
   :stroke-opacity-initial-start 255
   :stroke-opacity-initial-end 255
   :stroke-opacity-final-start 255
   :stroke-opacity-final-end 255

   :seed-base-layout 100
   :seed-step-layout 17
   :seed-base-drawing 500
   :seed-step-drawing 23})

#_(def blocks
    [(-> base-block
         (assoc :offset-initial (- (:offset-initial base-block) (:block-offset helper-values))))
     base-block])

(def blocks
  (vec
   (take 6
         (iterate
          #(as-> % b
             (assoc b :offset-initial (- (:offset-initial b) (+ (:gap helper-values) (:block-width helper-values)))

                    :vertex-variance-initial (+ 0.13 (:vertex-variance-initial b))
                    :vertex-variance-final (+ 0.17 (:vertex-variance-final b))
                    :obj-width-initial (* 1.5 (:obj-width-initial b))
                    :obj-width-final (* 1.5 (:obj-width-final b))
                    :obj-height-initial (* 1.5 (:obj-height-initial b))
                    :obj-height-final (* 1.5 (:obj-height-final b))))

          base-block))))

;; on each block to the left
;; shift to the left
;; increase vertex variance
;; increase base size
;; incrase fill opacity

(defn accent-block [block]
  (assoc block
         :blend-mode :add
         :offset-initial (- (:offset-initial block) 0.0001)

         :vertex-variance-initial (* 1.1 (:vertex-variance-initial block))
         :vertex-variance-final (* 1.1 (:vertex-variance-final block))

         :fill-color-initial-start [42 0 0]
         :fill-color-initial-end [0 0 0]
         :fill-color-final-start [80 0 0]
         :fill-color-final-end [20 0 0]

         :fill-opacity-ease interpol/ease-in-out
         :fill-opacity-initial-start 2
         :fill-opacity-initial-end 0
         :fill-opacity-final-start 8
         :fill-opacity-final-end 0

         :stroke-color-initial-start [42 0 0]
         :stroke-color-initial-end [0 0 0]
         :stroke-color-final-start [100 0 0]
         :stroke-color-final-end [20 0 0]

         :stroke-width-initial-start 1
         :stroke-width-initial-end 1
         :stroke-width-final-start 1
         :stroke-width-final-end 1

         ;; Old: stroke-opacity-initial 125, -final 0 -> within-stroke 125 -> 0.
         :stroke-opacity-ease interpol/ease-in-out
         :stroke-opacity-initial-start 80
         :stroke-opacity-initial-end 0
         :stroke-opacity-final-start 100
         :stroke-opacity-final-end 0))

(def accents (mapv accent-block blocks))

(def gold-line
  [{:num-bands 17
    :particles-per-band-initial 217
    :particles-per-band-final 217
    :blend-mode :add

    :field-position-initial 0.675
    :field-position-final 1

    :offset-initial 0

    :amplitude-initial 0.0
    :amplitude-final 0.0

    :frequency-initial 2.0
    ;:length-initial (- 1 (* 2 (:margin helper-values)))
    :length-initial 1

    :layout-variance-initial 0.0
    :layout-variance-final 0.0

    :phase true
    :direction :horizontal

    :vertex-variance-initial 0.7
    :vertex-variance-final 0.7

    :size-variance-initial 0.0
    :size-variance-final 0.0

    :obj-width-initial 0.03
    :obj-width-final 0.03
    :obj-height-initial (* 0.03 wh-ratio)
    :obj-height-final (* 0.03 wh-ratio)

      ;; Fill colour — all four corners are the same magenta here.
    :fill-color-initial-start (:gold palette)
    :fill-color-initial-end (:gold palette)
    :fill-color-final-start (:gold palette)
    :fill-color-final-end (:gold palette)

      ;; Stroke colour — offset was 0 in the old base-block, so same corners.
    :stroke-color-initial-start (:gold palette)
    :stroke-color-initial-end (:gold palette)
    :stroke-color-final-start (:gold palette)
    :stroke-color-final-end (:gold palette)

      ;; Fill opacity — constant within stroke, varies across the field
      ;; (start = 0 at band 0, end = 5 at band N-1).
    :fill-opacity-initial-start 2
    :fill-opacity-initial-end 0
    :fill-opacity-final-start 7
    :fill-opacity-final-end 2

      ;; Stroke width — same pattern; old sketch kept it constant at 1.
    :stroke-width-initial-start 1
    :stroke-width-initial-end 1
    :stroke-width-final-start 1
    :stroke-width-final-end 1

      ;; Stroke opacity — constant across field, varies within stroke (both 255 here).
    :stroke-opacity-initial-start 15
    :stroke-opacity-initial-end 0
    :stroke-opacity-final-start 77
    :stroke-opacity-final-end 0

    :seed-base-layout 100
    :seed-step-layout 17
    :seed-base-drawing 500
    :seed-step-drawing 23}])

;; -----------------------------------------------------------------------------
;; Assemble
;; -----------------------------------------------------------------------------

(def strokes ; layers
  (into [] (mapcat rec-wave-progression) (concat gold-line blocks accents)))

(def config
  {:renderer :java2d
   :filename "6b-calming"
   :background-color (:background-rgb artwork)
   :canvas {:canvas-width (:canvas-width artwork)
            :canvas-height (:canvas-height artwork)}
   :layers strokes})

(defn -main [& _args]
  (bvm/vera-multi config))

