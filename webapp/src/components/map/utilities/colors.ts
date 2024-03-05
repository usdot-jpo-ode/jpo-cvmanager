export function generateColorDictionary(inputSet: Set<string>): { [key: string]: string } {
  const keysArray = Array.from(inputSet);
  const colorDictionary: { [key: string]: string } = {};

  const colorStep = 360 / keysArray.length; // Divide the color wheel into equal parts

  keysArray.forEach((key, index) => {
    const hue = index * colorStep; // Calculate hue for each key
    const color = `hsl(${hue}, 100%, 35%)`; // Create an HSL color
    colorDictionary[key] = color;
  });

  return colorDictionary;
}

export function generateMapboxStyleExpression(colors: { [key: string]: string }): mapboxgl.Expression {
  const layerStyle: mapboxgl.Expression = ["match", ["get", "id"]];
  for (const [key, value] of Object.entries(colors)) {
    layerStyle.push(key);
    layerStyle.push(value);
  }
  layerStyle.push("#000000"); // other
  return layerStyle;
}
