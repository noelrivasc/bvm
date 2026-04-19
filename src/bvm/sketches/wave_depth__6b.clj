(ns bvm.sketches.wave-depth--6b
  (:require
   [bvm.core :as bvm]
   [bvm.utils.color :as color]
   [bvm.sketches.wave-depth :refer [make-band]]
   [bvm.utils.interpolation :refer [lerp]]
   [bvm.layouts.wavy-band :refer [wavy-band]]
   [bvm.styles.linear-fade :refer [linear-fade]]
   [bvm.drawing.rectangle :refer [rectangle]]))

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
     :block-rgb (color/lch->rgb-bytes (:magenta palette) :srgb)
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

(def base-block
  {:position-start (:margin helper-values)
   :position-end (- 1 (:margin helper-values))
   :num-bands 57
   :num-steps 36
   :blend-mode :blend
   :amplitude-start 0.0
   :amplitude-end 0.0
   :frequency 2.0
   :length (:block-width helper-values)
   :offset (+ (* -0.5 (:gap helper-values)) (* -0.5 (:block-width helper-values)))
   :variance-factor 0
   :phase true
   :direction :horizontal
   :vertex-var-start 0.0
   :vertex-var-end 0.3
   :size-var-start 0.0
   :size-var-end 0.0
   :obj-size-start 0.005
   :obj-size-end 0.01
   ; NOTE that "top" means when the waves start, not
   ; necessarily the top of the drawing.
   :top-initial-color (:block-rgb artwork)
   :top-final-color (:block-rgb artwork)
   :bottom-initial-color (:block-rgb artwork)
   :bottom-final-color (:block-rgb artwork)
   :stroke-color-offset 0
   :fill-opacity-start 0
   :fill-opacity-end 5
   :stroke-width-start 1
   :stroke-width-end 1
   :stroke-opacity-initial 255
   :stroke-opacity-final 255
   :seed-base-layout 100
   :seed-step-layout 17
   :seed-base-drawing 500
   :seed-step-drawing 23})

(def blocks
  [(assoc base-block ; 1st block
          :offset (+ (:offset base-block) (* -2 (:block-offset helper-values)))
          :vertex-var-start 0.1
          :vertex-var-end 0.6
          :obj-size-end 0.05
          :fill-opacity-end 20)
   (assoc base-block ; 2nd block
          :offset (+ (:offset base-block) (* -1 (:block-offset helper-values)))
          :vertex-var-start 0.05
          :vertex-var-end 0.4
          :obj-size-end 0.03
          :fill-opacity-end 15)

   base-block; 3rd block

   (assoc base-block ; 4th block
          :offset (+ (:offset base-block) (:block-offset helper-values))
          :vertex-var-end 0.4
          :obj-size-end 0.035
          :fill-opacity-end 10)
   (assoc base-block ; 5th block
          :offset (+ (:offset base-block) (* 2 (:block-offset helper-values)))
          :vertex-var-start 0.1
          :vertex-var-end 0.6
          :obj-size-end 0.047
          :fill-opacity-start 2
          :fill-opacity-end 12)
   (assoc base-block ; 6th block
          :offset (+ (:offset base-block) (* 3 (:block-offset helper-values)))
          :vertex-var-start 0.25
          :vertex-var-end 0.8
          :obj-size-start 0.01
          :obj-size-end 0.059
          :fill-opacity-start 5
          :fill-opacity-end 15)])

(defn accent-block [block]
  (assoc block
         :blend-mode :add
         :offset (+ 0.0002 (:offset block))
         :top-initial-color [100 100 100]
         :top-final-color [0 0 0]
         :bottom-initial-color [200 200 200]
         :bottom-final-color [50 50 50]
         :fill-opacity-start 0
         :fill-opacity-end 0
         :stroke-width-start 1
         :stroke-width-end 1
         :stroke-opacity-initial 125
         :stroke-opacity-final 0))

(def accents
  (mapv
   #(accent-block %)
   blocks))

(def layers
  (into []
        (mapcat (fn [block]
                  (mapv (fn [num-band]
                          (let [depth (/ (double num-band) (dec (:num-bands block)))]
                            (make-band block depth num-band)))
                        (range (:num-bands block)))))
        (concat blocks accents)))

(def config
  {:renderer :java2d
   :filename "wave-depth--6b"
   :background-color (:background-rgb artwork)
   :canvas {:canvas-width (:canvas-width artwork)
            :canvas-height (:canvas-height artwork)}
   :layers layers})

(defn -main [& args]
  (bvm/vera-multi config))
