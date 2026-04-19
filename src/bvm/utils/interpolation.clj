(ns bvm.utils.interpolation)

(defn lerp
  "Linear interpolation of a to b at position t.
   a and b can be any two numbers; t must be a float
   [0, 1]"
  [a b t] (+ a (* (- b a) t)))

(defn ease-in-out
  "Smooth cubic ease-in-out of t in [0, 1].
   Slow at the edges, fast through the middle."
  [t]
  (if (< t 0.5)
    (* 4 t t t)
    (let [f (- (* 2 t) 2)]
      (+ 1 (* 0.5 f f f)))))
