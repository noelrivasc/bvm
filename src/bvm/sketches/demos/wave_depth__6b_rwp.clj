(ns bvm.sketches.demos.wave-depth--6b-rwp
  "Recreation of wave-depth--6b using bvm.fields.rec-wave-progression
   instead of the hand-rolled make-band from wave-depth."
  (:require
   [bvm.core :as bvm]
   [bvm.utils.color :as color]
   [bvm.fields.rec-wave-progression :refer [rec-wave-progression]]))

(def palette
  {:gold [70 88.93 88.41]
   :magenta [37.55 72.08 350.19]
   :dead-purple [9.41 17.4 286.89]})

(def artwork
  (let [canvas-width-mm 900
        canvas-height-mm 600
        in-to-mm 25.4
        canvas-width-in (/ canvas-width-mm in-to-mm)
        canvas-height-in (/ canvas-height-mm in-to-mm)
        canvas-dpi 150]
    {:background-rgb (color/lch->rgb-bytes (:dead-purple palette) :srgb)
     :block-lch (:magenta palette)
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

;; -----------------------------------------------------------------------------
;; Base block, in rec-wave-progression config shape
;; -----------------------------------------------------------------------------
;; Mapping notes vs. the old make-band config:
;;   position-start/end         -> field-position-initial/final
;;   amplitude-start/end        -> amplitude-initial/final
;;   frequency, length, offset  -> *-initial (constants; final defaults to initial)
;;   variance-factor * depth    -> layout-variance-initial 0.0, -final = variance-factor
;;   vertex-var-start/end       -> vertex-variance-initial/final
;;   size-var-start/end         -> size-variance-initial/final
;;   obj-size-start/end         -> obj-width-initial/final AND obj-height-initial/final
;;   num-steps                  -> particles-per-band-initial
;;   top-*/bottom-* colors      -> fill-color 4 corners
;;                                 (top-initial, top-final, bottom-initial, bottom-final)
;;                                 = (initial-start, initial-end, final-start, final-end)
;;   stroke-color-offset        -> precomputed into stroke-color 4 corners
;;   fill-opacity-start/end     -> constant within-stroke; varies across field
;;   stroke-width-start/end     -> constant within-stroke; varies across field
;;   stroke-opacity-initial/final -> constant across field; varies within-stroke

(def base-block
  {:num-bands 57
   :particles-per-band-initial 36
   :blend-mode :blend

   :field-position-initial (:margin helper-values)
   :field-position-final (- 1 (:margin helper-values))

   :amplitude-initial 0.0
   :amplitude-final 0.0

   :frequency-initial 2.0
   :length-initial (:block-width helper-values)
   :offset-initial (+ (* -0.5 (:gap helper-values))
                      (* -0.5 (:block-width helper-values)))

   :layout-variance-initial 0.0
   :layout-variance-final 0.0

   :phase true
   :direction :horizontal

   :vertex-variance-initial 0.0
   :vertex-variance-final 0.3

   :size-variance-initial 0.0
   :size-variance-final 0.0

   :obj-width-initial 0.005
   :obj-width-final 0.01
   :obj-height-initial 0.005
   :obj-height-final 0.01

   ;; Fill colour — all four corners are the same magenta here.
   :fill-color-initial-start (:block-lch artwork)
   :fill-color-initial-end (:block-lch artwork)
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
   :fill-opacity-final-start 5
   :fill-opacity-final-end 5

   ;; Stroke width — same pattern; old sketch kept it constant at 1.
   :stroke-width-initial-start 1
   :stroke-width-initial-end 1
   :stroke-width-final-start 1
   :stroke-width-final-end 1

   ;; Stroke opacity — constant across field, varies within stroke (both 255 here).
   :stroke-opacity-initial-start 255
   :stroke-opacity-initial-end 255
   :stroke-opacity-final-start 255
   :stroke-opacity-final-end 255

   :seed-base-layout 100
   :seed-step-layout 17
   :seed-base-drawing 500
   :seed-step-drawing 23})

;; -----------------------------------------------------------------------------
;; Helpers for per-block overrides (so we can keep the original's terse style)
;; -----------------------------------------------------------------------------

(defn- with-offset
  "Shift the block's offset along the drawing axis by n block-offsets."
  [block n]
  (assoc block :offset-initial
         (+ (:offset-initial base-block) (* n (:block-offset helper-values)))))

(defn- vertex-var [block s e]
  (assoc block :vertex-variance-initial s :vertex-variance-final e))

(defn- obj-size [block s e]
  (assoc block
         :obj-width-initial s :obj-width-final e
         :obj-height-initial s :obj-height-final e))

(defn- fill-op [block s e]
  (assoc block
         :fill-opacity-initial-start s :fill-opacity-initial-end s
         :fill-opacity-final-start e :fill-opacity-final-end e))

;; -----------------------------------------------------------------------------
;; The six magenta blocks
;; -----------------------------------------------------------------------------

(def blocks
  [(-> base-block  ; 1st
       (with-offset -2)
       (vertex-var 0.1 0.6)
       (obj-size 0.005 0.05)
       (fill-op 0 20))
   (-> base-block  ; 2nd
       (with-offset -1)
       (vertex-var 0.05 0.4)
       (obj-size 0.005 0.03)
       (fill-op 0 15))

   base-block      ; 3rd

   (-> base-block  ; 4th
       (with-offset 1)
       (vertex-var 0.0 0.4)
       (obj-size 0.005 0.035)
       (fill-op 0 10))
   (-> base-block  ; 5th
       (with-offset 2)
       (vertex-var 0.1 0.6)
       (obj-size 0.005 0.047)
       (fill-op 2 12))
   (-> base-block  ; 6th
       (with-offset 3)
       (vertex-var 0.25 0.8)
       (obj-size 0.01 0.059)
       (fill-op 5 15))])

;; -----------------------------------------------------------------------------
;; Accent layer: additive blend, greyscale, outline only
;; -----------------------------------------------------------------------------
;; Old accent-block used RGB greys. In LCH a grey is [L 0 0]; L roughly
;; follows CIELAB lightness (L=0 black, L=100 white). Approximate mapping
;; from the old RGB greys:
;;   [100 100 100] -> ~[42 0 0]
;;   [0 0 0]       ->  [0  0 0]
;;   [200 200 200] -> ~[80 0 0]
;;   [50 50 50]    -> ~[20 0 0]

(defn accent-block [block]
  (assoc block
         :blend-mode :add
         :offset-initial (+ 0.0002 (:offset-initial block))

         :fill-color-initial-start [42 0 0]
         :fill-color-initial-end [0 0 0]
         :fill-color-final-start [80 0 0]
         :fill-color-final-end [20 0 0]

         :stroke-color-initial-start [42 0 0]
         :stroke-color-initial-end [0 0 0]
         :stroke-color-final-start [80 0 0]
         :stroke-color-final-end [20 0 0]

         :fill-opacity-initial-start 0
         :fill-opacity-initial-end 0
         :fill-opacity-final-start 0
         :fill-opacity-final-end 0

         :stroke-width-initial-start 1
         :stroke-width-initial-end 1
         :stroke-width-final-start 1
         :stroke-width-final-end 1

         ;; Old: stroke-opacity-initial 125, -final 0 -> within-stroke 125 -> 0.
         :stroke-opacity-initial-start 125
         :stroke-opacity-initial-end 0
         :stroke-opacity-final-start 125
         :stroke-opacity-final-end 0))

(def accents (mapv accent-block blocks))

;; -----------------------------------------------------------------------------
;; Assemble
;; -----------------------------------------------------------------------------

(def layers
  (into [] (mapcat rec-wave-progression) (concat blocks accents)))

(def config
  {:renderer :java2d
   :filename "wave-depth--6b-rwp"
   :background-color (:background-rgb artwork)
   :canvas {:canvas-width (:canvas-width artwork)
            :canvas-height (:canvas-height artwork)}
   :layers layers})

(defn -main [& _args]
  (bvm/vera-multi config))
