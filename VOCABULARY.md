# Vocabulary

A glossary of available layouts, styles, and drawing functions.

---

## Layouts

### simple-grid

Arranges objects in a grid pattern.

| Option | Required | Description |
|--------|----------|-------------|
| `:max-cols` | yes | Maximum columns per row |
| `:gap-x` | no | Spacing between cells (fraction of canvas) |

### ring

Arranges objects along an ellipse.

| Option | Required | Description |
|--------|----------|-------------|
| `:ellipse-width` | yes | Width of ellipse (0-1) |
| `:ellipse-height` | yes | Height of ellipse (0-1) |
| `:object-width` | yes | Object width (0-1) |
| `:object-height` | yes | Object height (0-1) |
| `:start` | no | Starting position (0-1); 0=top, 0.25=right, 0.5=bottom |
| `:completion` | no | Arc span (0-1); 1=full ellipse, 0.5=half |
| `:center-x` | no | X center of ellipse (0-1) |
| `:center-y` | no | Y center of ellipse (0-1) |
| `:align-to-path` | no | Rotate objects to follow ellipse tangent |

### wavy-band

Arranges objects along a centered sine wave.

| Option | Required | Description |
|--------|----------|-------------|
| `:amplitude` | yes | Wave height (0-1) |
| `:frequency` | yes | Cycles per length (1 = full wave) |
| `:length` | yes | Span along drawing axis (0-1) |
| `:position` | yes | Cross-axis center position (0-1) |
| `:object-width` | yes | Object width (0-1) |
| `:object-height` | yes | Object height (0-1) |
| `:variance` | no | Random deviation from wave (0-1) |
| `:phase` | no | Boolean; true starts wave going down |
| `:direction` | no | `:horizontal` or `:vertical` |
| `:seed` | no | Random seed for reproducibility |

### chaotic-spiral

Places objects along a spiral path with optional randomness.

| Option | Required | Description |
|--------|----------|-------------|
| `:turns` | yes | Number of complete rotations |
| `:min-radius` | yes | Inner radius (0-1) |
| `:max-radius` | yes | Outer radius (0-1) |
| `:initial-width` | yes | First object width (0-1) |
| `:initial-height` | yes | First object height (0-1) |
| `:end-width` | yes | Last object width (0-1) |
| `:end-height` | yes | Last object height (0-1) |
| `:variance` | no | Position randomness (0-1) |
| `:dispersion` | no | Radius growth curve modifier |
| `:direction` | no | `:clockwise` or `:counter-clockwise` |
| `:align-to-path` | no | Rotate objects to follow spiral tangent |
| `:seed` | no | Random seed for reproducibility |

---

## Styles

### static-test

Returns fixed style values. Useful for testing.

No options.

### linear-fade

Interpolates all style properties from initial to final values.

| Option | Description |
|--------|-------------|
| `:initial-fill-color` | RGB tuple `[r g b]` for first object |
| `:final-fill-color` | RGB tuple `[r g b]` for last object |
| `:initial-fill-opacity` | Integer 0-255 |
| `:final-fill-opacity` | Integer 0-255 |
| `:initial-stroke-color` | RGB tuple `[r g b]` |
| `:final-stroke-color` | RGB tuple `[r g b]` |
| `:initial-stroke-width` | Integer |
| `:final-stroke-width` | Integer |
| `:initial-stroke-opacity` | Integer 0-255 |
| `:final-stroke-opacity` | Integer 0-255 |

---

## Drawing Functions

### circle

Draws an ellipse.

No options.

### rectangle

Draws a four-sided polygon with optional deformation.

| Option | Description |
|--------|-------------|
| `:vertex-variance` | Corner offset amount (0-1) |
| `:size-variance` | Size multiplier variance (0+) |
| `:seed` | Random seed for reproducibility |
