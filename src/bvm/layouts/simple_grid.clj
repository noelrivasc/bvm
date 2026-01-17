(ns bvm.layouts.simple-grid)

(defn simple-grid
  "A grid layout that arranges objects in rows and columns.

  Options:
    :max-cols  - Required. Number of columns in the grid.
    :gap-x     - Optional. Horizontal gap between items as fraction of canvas width (default: 0.0).

  Width is derived from max-cols and gap-x.
  Height and vertical spacing are derived from the number of rows
  (calculated from num-steps and max-cols), using the same gap-x value
  for consistent spacing."
  [index num-steps options]
  (let [max-cols (:max-cols options)
        gap-x (get options :gap-x 0.0)
        num-rows (int (Math/ceil (/ num-steps max-cols)))
        ; Calculate item dimensions accounting for gaps
        ; Total horizontal gap space = gap-x * (max-cols + 1) for gaps on sides and between
        total-gap-x (* gap-x (+ max-cols 1))
        item-width (/ (- 1.0 total-gap-x) max-cols)
        ; Use same gap value for vertical spacing
        total-gap-y (* gap-x (+ num-rows 1))
        item-height (/ (- 1.0 total-gap-y) num-rows)
        ; Calculate position in grid
        col (mod index max-cols)
        row (quot index max-cols)
        ; Position is: gap + col/row * (item-size + gap) + half-item-size (to center)
        x (+ gap-x (* col (+ item-width gap-x)) (/ item-width 2.0))
        y (+ gap-x (* row (+ item-height gap-x)) (/ item-height 2.0))]
    {:index index
     :x (float x)
     :y (float y)
     :width (float item-width)
     :height (float item-height)
     :rotation 0.0}))
