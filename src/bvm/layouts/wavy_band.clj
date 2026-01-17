(ns bvm.layouts.wavy-band
  (:import [java.util Random]))

(defn wavy-band
  "Arranges objects along a centered sine wave.

  Options:
    :amplitude     - Required. Wave height as fraction of canvas (0-1).
    :frequency     - Required. Wave cycles per length. 1 = full wave, 0.5 = half wave.
    :length        - Required. Fraction of canvas the wave spans (0-1).
    :position      - Required. Cross-axis position (0-1). For horizontal waves,
                     this is the y-axis center. For vertical, the x-axis center.
    :object-width  - Required. Width of objects (fraction of canvas).
    :object-height - Required. Height of objects (fraction of canvas).
    :variance      - Optional. Random deviation from wave (0-1). Default: 0.0.
    :phase         - Optional. Boolean. True starts wave going down. Default: false.
    :direction     - Optional. :horizontal or :vertical. Default: :horizontal.
    :seed          - Optional. Random seed for reproducibility. Default: 42."
  [index num-steps options]
  (let [;; Required options
        amplitude (:amplitude options)
        frequency (:frequency options)
        length (:length options)
        position (:position options)
        object-width (:object-width options)
        object-height (:object-height options)
        ;; Optional with defaults
        variance (get options :variance 0.0)
        phase (get options :phase false)
        direction (get options :direction :horizontal)
        seed (get options :seed 42)
        ;; Create seeded random for this object
        rng (Random. (+ seed index))
        ;; Progress along the wave (0 to 1)
        progress (if (<= num-steps 1) 0.5 (/ (double index) (dec num-steps)))
        ;; Phase offset: add π to start going down
        phase-offset (if phase Math/PI 0.0)
        ;; Calculate sine wave value (-1 to 1)
        sine-value (Math/sin (+ (* 2.0 Math/PI frequency progress) phase-offset))
        ;; Apply variance as random offset scaled by amplitude
        variance-offset (* variance amplitude (- (* 2.0 (.nextDouble rng)) 1.0))
        wave-offset (+ (* amplitude sine-value) variance-offset)
        ;; Wave is centered: spans from (0.5 - length/2) to (0.5 + length/2)
        start-pos (- 0.5 (/ length 2.0))
        draw-axis-pos (+ start-pos (* progress length))
        cross-axis-pos (+ position wave-offset)
        ;; Assign x, y based on direction
        [x y] (if (= direction :horizontal)
                [draw-axis-pos cross-axis-pos]
                [cross-axis-pos draw-axis-pos])]
    {:index index
     :x (float (max 0.0 (min 1.0 x)))
     :y (float (max 0.0 (min 1.0 y)))
     :width (float object-width)
     :height (float object-height)
     :rotation 0.0}))
