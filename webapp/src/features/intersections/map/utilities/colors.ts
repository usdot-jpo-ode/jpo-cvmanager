import { sha256 } from 'js-sha256'
import Color from 'color'

const DISALLOWED_COLORS = new Set(
  Array.from(
    new Set([
      Color('#797979').hsl().color[0],
      Color('#3a3a3a').hsl().color[0],
      Color('#c00000').hsl().color[0],
      Color('#267700').hsl().color[0],
      Color('#e6b000').hsl().color[0],
      Color('#ffffff').hsl().color[0],
      Color('#000000').hsl().color[0],
      Color('#d40000').hsl().color[0],
      Color('#eb34e8').hsl().color[0],
      Color('#0004ff').hsl().color[0],
    ]) // Extract the hue
  ).sort((a, b) => a - b)
) // ascending order

// pre-allocate color space around disallowed colors
const OUTPUT_COLOR_SPACE_LENGTH = 360
const COLOR_SPACE_AVOID_WIDTH = 7 // on each side
const COLOR_SPACE_INCREMENT = 0.1
const COLOR_SPACE_MAP = new Map<number, number>() // map from generic 0 -> 360 color space, to new shrunken color space

// Calculate the total avoidance space
let totalAvoidanceSpace = 0
for (let i = 0; i < OUTPUT_COLOR_SPACE_LENGTH; i += COLOR_SPACE_INCREMENT) {
  for (const disallowedHue of Array.from(DISALLOWED_COLORS)) {
    if (Math.abs(disallowedHue - i) < COLOR_SPACE_AVOID_WIDTH) {
      totalAvoidanceSpace += COLOR_SPACE_INCREMENT
    }
  }
}

const OUTPUT_COLOR_SPACE_LENGTH_BEFORE_SCALING = 360 - totalAvoidanceSpace

// Generate the new color space
let prevColor = 0
for (let i = 0; i < OUTPUT_COLOR_SPACE_LENGTH; i += COLOR_SPACE_INCREMENT) {
  let newColor =
    prevColor + (COLOR_SPACE_INCREMENT * OUTPUT_COLOR_SPACE_LENGTH_BEFORE_SCALING) / OUTPUT_COLOR_SPACE_LENGTH
  for (const disallowedHue of Array.from(DISALLOWED_COLORS.values())) {
    if (Math.abs(disallowedHue - newColor) < COLOR_SPACE_AVOID_WIDTH) {
      newColor = disallowedHue + COLOR_SPACE_AVOID_WIDTH
    }
  }
  COLOR_SPACE_MAP.set(Math.round(i * 10) / 10, newColor)
  prevColor = newColor
}

const start = 355
const end = 360
const step = 0.1

const numbers: number[] = []
for (let i = start; i <= end; i += step) {
  numbers.push(Math.round(i * 10) / 10)
}

export function generateColorDictionary(inputSet: Set<string>): { [key: string]: string } {
  const keysArray = Array.from(inputSet).sort() // Sort the keysArray
  const colorDictionary: { [key: string]: string } = {}

  keysArray.forEach((key) => {
    const hash = sha256(key) // Generate a hash for each key using sha256
    let hue = (parseInt(hash.slice(0, 2), 16) * 360) / 256 // Use the first 2 characters of the hash as the hue

    // Map the hue to the shrunken color space
    const keyInMap = Math.round(hue * 10) / 10
    hue = COLOR_SPACE_MAP.get(keyInMap) || hue

    const color = Color.hsl(hue, 100, 50) // Use the hue to generate a color
    colorDictionary[key] = color.hex()
  })

  return colorDictionary
}

export function generateMapboxStyleExpression(colors: { [key: string]: string }): mapboxgl.Expression {
  const layerStyle: mapboxgl.Expression = ['match', ['get', 'id'], 'temp-id', '#0004ff']
  for (const [key, value] of Object.entries(colors)) {
    layerStyle.push(key)
    layerStyle.push(value)
  }
  layerStyle.push('#000000') // other
  return layerStyle
}
