(ns bvm.layouts.chaotic-spiral
  (:import [java.util Random]))

(defn- lerp
  "Linear interpolation between a and b by t (0-1)."
  [a b t]
  (+ a (* (- b a) t)))

(defn- radius-at-t
  "Calculate radius at normalized position t (0-1) along the spiral.
  Dispersion modifies the growth curve:
    0 = linear
    negative = roomier center (faster initial growth)
    positive = tighter center (slower initial growth)"
  [min-radius max-radius dispersion t]
  (let [curved-t (Math/pow t (+ 1.0 dispersion))]
    (lerp min-radius max-radius curved-t)))

(defn- angle-at-t
  "Calculate angle at normalized position t (0-1).
  Returns angle in radians based on number of turns and direction."
  [turns direction t]
  (let [base-angle (* t turns 2.0 Math/PI)]
    (if (= direction :clockwise)
      (- base-angle)
      base-angle)))

(defn- spiral-point
  "Get x, y coordinates for a point on the spiral at normalized position t."
  [min-radius max-radius dispersion turns direction t]
  (let [r (radius-at-t min-radius max-radius dispersion t)
        theta (angle-at-t turns direction t)
        ; Center of canvas is (0.5, 0.5)
        x (+ 0.5 (* r (Math/cos theta)))
        y (+ 0.5 (* r (Math/sin theta)))]
    {:x x :y y :r r :theta theta}))

(defn- arc-length-segment
  "Approximate arc length between two t values using distance between points."
  [min-radius max-radius dispersion turns direction t1 t2]
  (let [p1 (spiral-point min-radius max-radius dispersion turns direction t1)
        p2 (spiral-point min-radius max-radius dispersion turns direction t2)
        dx (- (:x p2) (:x p1))
        dy (- (:y p2) (:y p1))]
    (Math/sqrt (+ (* dx dx) (* dy dy)))))

(defn- compute-arc-length-table
  "Build a table mapping cumulative arc length to t values.
  Returns a vector of [t cumulative-arc-length] pairs."
  [min-radius max-radius dispersion turns direction num-samples]
  (let [dt (/ 1.0 num-samples)]
    (loop [i 0
           t 0.0
           cumulative 0.0
           table [[0.0 0.0]]]
      (if (>= i num-samples)
        table
        (let [next-t (min 1.0 (+ t dt))
              segment-length (arc-length-segment min-radius max-radius dispersion
                                                  turns direction t next-t)
              next-cumulative (+ cumulative segment-length)]
          (recur (inc i)
                 next-t
                 next-cumulative
                 (conj table [next-t next-cumulative])))))))

(defn- t-for-arc-length
  "Find t value corresponding to a target arc length using the precomputed table.
  Uses linear interpolation between table entries."
  [table target-length]
  (let [total-length (second (last table))]
    (if (<= target-length 0.0)
      0.0
      (if (>= target-length total-length)
        1.0
        (let [; Find the two entries bracketing our target
              idx (loop [i 0]
                    (if (or (>= i (dec (count table)))
                            (> (second (nth table (inc i))) target-length))
                      i
                      (recur (inc i))))
              [t1 len1] (nth table idx)
              [t2 len2] (nth table (inc idx))
              ; Linear interpolation
              frac (if (= len1 len2) 0.0 (/ (- target-length len1) (- len2 len1)))]
          (lerp t1 t2 frac))))))

(defn- tangent-angle-at-t
  "Calculate the tangent angle (in degrees) at position t on the spiral."
  [min-radius max-radius dispersion turns direction t]
  (let [epsilon 0.001
        t1 (max 0.0 (- t epsilon))
        t2 (min 1.0 (+ t epsilon))
        p1 (spiral-point min-radius max-radius dispersion turns direction t1)
        p2 (spiral-point min-radius max-radius dispersion turns direction t2)
        dx (- (:x p2) (:x p1))
        dy (- (:y p2) (:y p1))
        radians (Math/atan2 dy dx)]
    (Math/toDegrees radians)))

(defn chaotic-spiral
  "A spiral layout that places objects along a spiral path with optional chaos.

  Options:
    :turns          - Required. Number of complete rotations (int).
    :min-radius     - Required. Inner radius as fraction of canvas (0-1).
    :max-radius     - Required. Outer radius as fraction of canvas (0-1).
    :initial-width  - Required. Width of first object (fraction of canvas).
    :initial-height - Required. Height of first object (fraction of canvas).
    :end-width      - Required. Width of last object (fraction of canvas).
    :end-height     - Required. Height of last object (fraction of canvas).
    :variance       - Optional. Chaos factor 0-1 (default: 0.0).
                      At 1, offset can equal object size.
    :dispersion     - Optional. Radius growth curve modifier (default: 0.0).
                      Negative = roomier center, positive = tighter center.
    :direction      - Optional. :clockwise or :counter-clockwise (default: :counter-clockwise).
    :align-to-path  - Optional. Rotate objects to align with spiral tangent (default: false).
    :seed           - Optional. Random seed for reproducible results."
  [index num-steps options]
  (let [; Extract required options
        turns (:turns options)
        min-radius (:min-radius options)
        max-radius (:max-radius options)
        initial-width (:initial-width options)
        initial-height (:initial-height options)
        end-width (:end-width options)
        end-height (:end-height options)
        ; Extract optional options with defaults
        variance (get options :variance 0.0)
        dispersion (get options :dispersion 0.0)
        direction (get options :direction :counter-clockwise)
        align-to-path (get options :align-to-path false)
        seed (get options :seed 42)
        ; Create seeded random generator - use seed + index for per-object randomness
        rng (Random. (+ seed index))
        ; Normalized progress (0-1) for this object
        progress (if (<= num-steps 1) 0.0 (/ (double index) (dec num-steps)))
        ; Compute arc-length table for equal distribution
        arc-table (compute-arc-length-table min-radius max-radius dispersion
                                             turns direction 500)
        total-arc-length (second (last arc-table))
        ; Find t for this object's position along the arc
        target-arc-length (* progress total-arc-length)
        t (t-for-arc-length arc-table target-arc-length)
        ; Get base position on spiral
        base-point (spiral-point min-radius max-radius dispersion turns direction t)
        ; Calculate object dimensions (linear interpolation)
        obj-width (lerp initial-width end-width progress)
        obj-height (lerp initial-height end-height progress)
        ; Apply variance (random offset proportional to object size)
        x-offset (* variance obj-width (- (* 2.0 (.nextDouble rng)) 1.0))
        y-offset (* variance obj-height (- (* 2.0 (.nextDouble rng)) 1.0))
        final-x (+ (:x base-point) x-offset)
        final-y (+ (:y base-point) y-offset)
        ; Calculate rotation
        rotation (if align-to-path
                   (tangent-angle-at-t min-radius max-radius dispersion
                                        turns direction t)
                   0.0)]
    {:index index
     :x (float (max 0.0 (min 1.0 final-x)))
     :y (float (max 0.0 (min 1.0 final-y)))
     :width (float obj-width)
     :height (float obj-height)
     :rotation (float rotation)}))
