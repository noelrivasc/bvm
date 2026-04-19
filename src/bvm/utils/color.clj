(ns bvm.utils.color
  "Color conversion utilities for perceptual color manipulation in generative art.

   Pipeline: LCH <-> LAB -> XYZ -> linear RGB -> gamma-encoded RGB -> 8-bit bytes

   All colors are represented as 3-element vectors:
     LCH: [L C H] where L=[0,100], C=[0,~130], H=[0,360] degrees
     LAB: [L a b] where L=[0,100], a=[-128,127], b=[-128,127]
     Linear RGB: [r g b] floats, in-gamut when all in [0,1]
     RGB bytes: [r g b] integers [0,255]

   Supported color spaces: :srgb, :adobe-rgb")

;; ---------------------------------------------------------------------------
;; D65 whitepoint (standard illuminant for sRGB and Adobe RGB)
;; ---------------------------------------------------------------------------

(def ^:private d65-xn 0.95047)
(def ^:private d65-yn 1.00000)
(def ^:private d65-zn 1.08883)

;; ---------------------------------------------------------------------------
;; CIE constants
;; ---------------------------------------------------------------------------

(def ^:private cie-epsilon (/ 216.0 24389.0))  ; 0.008856
(def ^:private cie-kappa   (/ 24389.0 27.0))   ; 903.3

;; ---------------------------------------------------------------------------
;; XYZ -> linear RGB matrices
;; ---------------------------------------------------------------------------

(def ^:private xyz->rgb-matrices
  {:srgb     [[3.2404542 -1.5371385 -0.4985314]
              [-0.9692660  1.8760108  0.0415560]
              [0.0556434 -0.2040259  1.0572252]]
   :adobe-rgb [[2.0413690 -0.5649464 -0.3446944]
               [-0.9692660  1.8760108  0.0415560]
               [0.0134474 -0.1183897  1.0154096]]})

;; ---------------------------------------------------------------------------
;; LCH <-> LAB
;; ---------------------------------------------------------------------------

(defn lch->lab
  "Converts [L C H] to [L a b]. H is in degrees."
  [[L C H]]
  (let [H-rad (Math/toRadians H)]
    [L
     (* C (Math/cos H-rad))
     (* C (Math/sin H-rad))]))

(defn lab->lch
  "Converts [L a b] to [L C H]. H is in degrees [0, 360]."
  [[L a b]]
  (let [C (Math/sqrt (+ (* a a) (* b b)))
        H (Math/toDegrees (Math/atan2 b a))
        H (if (neg? H) (+ H 360.0) H)]
    [L C H]))

;; ---------------------------------------------------------------------------
;; Interpolation
;; ---------------------------------------------------------------------------

(defn lerp-rgb
  "Linear interpolation between two RGB colors. t in [0, 1]."
  [[r1 g1 b1] [r2 g2 b2] t]
  [(+ r1 (* t (- r2 r1)))
   (+ g1 (* t (- g2 g1)))
   (+ b1 (* t (- b2 b1)))])

(defn lerp-lab
  "Linear interpolation between two LAB colors. t in [0, 1].
   Interpolates a straight line through LAB space. Good for same-hue
   gradients; may desaturate through the midpoint for different hues."
  [[L1 a1 b1] [L2 a2 b2] t]
  [(+ L1 (* t (- L2 L1)))
   (+ a1 (* t (- a2 a1)))
   (+ b1 (* t (- b2 b1)))])

(defn- shortest-angle-delta
  "Returns the shortest signed angular distance from h1 to h2 in degrees."
  [h1 h2]
  (let [d (mod (- h2 h1) 360.0)]
    (if (> d 180.0)
      (- d 360.0)
      d)))

(defn lerp-lch
  "Interpolation between two LCH colors. t in [0, 1].
   Interpolates hue along the shortest arc. Preserves chroma through
   the midpoint, good for hue sweeps."
  [[L1 C1 H1] [L2 C2 H2] t]
  (let [L (+ L1 (* t (- L2 L1)))
        C (+ C1 (* t (- C2 C1)))
        H (+ H1 (* t (shortest-angle-delta H1 H2)))
        H (mod H 360.0)
        H (if (neg? H) (+ H 360.0) H)]
    [L C H]))

;; ---------------------------------------------------------------------------
;; LAB -> XYZ -> linear RGB
;; ---------------------------------------------------------------------------

(defn- lab->xyz
  "Converts [L a b] to [X Y Z] using D65 whitepoint."
  [[L a b]]
  (let [fy (/ (+ L 16.0) 116.0)
        fx (+ (/ a 500.0) fy)
        fz (- fy (/ b 200.0))
        fx3 (* fx fx fx)
        fz3 (* fz fz fz)
        X (* d65-xn (if (> fx3 cie-epsilon)
                      fx3
                      (/ (- (* 116.0 fx) 16.0) cie-kappa)))
        Y (* d65-yn (if (> L (* cie-kappa cie-epsilon))
                      (let [v (/ (+ L 16.0) 116.0)] (* v v v))
                      (/ L cie-kappa)))
        Z (* d65-zn (if (> fz3 cie-epsilon)
                      fz3
                      (/ (- (* 116.0 fz) 16.0) cie-kappa)))]
    [X Y Z]))

(defn- mat-mul-3
  "Multiplies a 3x3 matrix by a 3-element vector."
  [[row0 row1 row2] [x y z]]
  [(+ (* (row0 0) x) (* (row0 1) y) (* (row0 2) z))
   (+ (* (row1 0) x) (* (row1 1) y) (* (row1 2) z))
   (+ (* (row2 0) x) (* (row2 1) y) (* (row2 2) z))])

(defn lab->linear-rgb
  "Converts [L a b] to linear RGB floats via XYZ.
   color-space is :srgb or :adobe-rgb, selects the XYZ->RGB matrix.
   Result may contain values outside [0,1] for out-of-gamut colors."
  [[L a b] color-space]
  (let [xyz (lab->xyz [L a b])
        matrix (get xyz->rgb-matrices color-space)]
    (if matrix
      (mat-mul-3 matrix xyz)
      (throw (ex-info (str "Unknown color space: " color-space)
                      {:color-space color-space})))))

;; ---------------------------------------------------------------------------
;; Gamut checking
;; ---------------------------------------------------------------------------

(defn lab-in-gamut?
  "Checks whether a LAB color is within gamut for the given color space.
   Returns nil if in gamut, or a vector of three booleans [r-clipped? g-clipped? b-clipped?]
   indicating which channels fall outside [0, 1]."
  [[L a b] color-space]
  (let [[r g b] (lab->linear-rgb [L a b] color-space)
        clip? (fn [v] (or (< v 0.0) (> v 1.0)))
        clips [(clip? r) (clip? g) (clip? b)]]
    (when (some true? clips)
      clips)))

(defn lch-in-gamut?
  "Checks whether a LCH color is within gamut for the given color space.
   Returns nil if in gamut, or a vector of three booleans (order RGB) 
   indicating channels that fall outside [0, 1]"
  [[L C H] color-space]
  (let [lab (lch->lab [L C H])]
    (lab-in-gamut? lab color-space)))

;; ---------------------------------------------------------------------------
;; Gamma encoding
;; ---------------------------------------------------------------------------

(defn- srgb-gamma
  "sRGB companding: piecewise linear/power curve."
  [v]
  (if (<= v 0.0031308)
    (* 12.92 v)
    (- (* 1.055 (Math/pow v (/ 1.0 2.4))) 0.055)))

(defn- adobe-rgb-gamma
  "Adobe RGB companding: simple 2.2 power curve."
  [v]
  (Math/pow v (/ 1.0 2.19921875)))

(defn gamma-encode
  "Applies gamma encoding to a linear RGB triplet.
   Input and output are floats in [0, 1] (clamps input before encoding).
   color-space is :srgb or :adobe-rgb."
  [[r g b] color-space]
  (let [clamp  (fn [v] (max 0.0 (min 1.0 (double v))))
        encode (case color-space
                 :srgb      srgb-gamma
                 :adobe-rgb adobe-rgb-gamma
                 (throw (ex-info (str "Unknown color space: " color-space)
                                 {:color-space color-space})))]
    [(encode (clamp r))
     (encode (clamp g))
     (encode (clamp b))]))

;; ---------------------------------------------------------------------------
;; Quantization
;; ---------------------------------------------------------------------------

(defn linear-rgb->bytes
  "Converts gamma-encoded RGB floats [0, 1] to 8-bit integers [0, 255].
   Feed this the output of gamma-encode, not raw linear values."
  [[r g b]]
  [(Math/round (* 255.0 (double r)))
   (Math/round (* 255.0 (double g)))
   (Math/round (* 255.0 (double b)))])

;; ---------------------------------------------------------------------------
;; Convenience: full pipeline
;; ---------------------------------------------------------------------------

(defn lab->rgb-bytes
  "Full pipeline: LAB -> linear RGB -> gamma encode -> 8-bit bytes.
   Clamps out-of-gamut values."
  [[L a b] color-space]
  (-> (lab->linear-rgb [L a b] color-space)
      (gamma-encode color-space)
      (linear-rgb->bytes)))

(defn lch->rgb-bytes
  "Full pipeline: LCH -> LAB -> linear RGB -> gamma encode -> 8-bit bytes.
   Clamps out-of-gamut values."
  [[L C H] color-space]
  (lab->rgb-bytes (lch->lab [L C H]) color-space))
