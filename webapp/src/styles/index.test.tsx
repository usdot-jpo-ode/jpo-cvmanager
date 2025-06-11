import { THEMES, getCurrentTheme } from './index'

describe('getCurrentTheme', () => {
  const consoleWarnSpy = jest.spyOn(console, 'warn').mockImplementation(() => {})

  afterEach(() => {
    jest.clearAllMocks()
  })

  it('should return the dark theme when isDarkTheme is true and darkThemeName is valid', () => {
    const theme = getCurrentTheme(true, 'light', 'dark')
    expect(theme).toBe(THEMES.dark)
    expect(consoleWarnSpy).not.toHaveBeenCalled()
  })

  it('should return the light theme when isDarkTheme is false and lightThemeName is valid', () => {
    const theme = getCurrentTheme(false, 'light', 'dark')
    expect(theme).toBe(THEMES.light)
    expect(consoleWarnSpy).not.toHaveBeenCalled()
  })

  it('should return the default dark theme when isDarkTheme is true and darkThemeName is invalid', () => {
    const theme = getCurrentTheme(true, 'light', 'invalidDarkTheme')
    expect(theme).toBe(THEMES.dark)
    expect(consoleWarnSpy).toHaveBeenCalledWith('Unknown dark theme name: invalidDarkTheme')
  })

  it('should return the default light theme when isDarkTheme is false and lightThemeName is invalid', () => {
    const theme = getCurrentTheme(false, 'invalidLightTheme', 'dark')
    expect(theme).toBe(THEMES.light)
    expect(consoleWarnSpy).toHaveBeenCalledWith('Unknown light theme name: invalidLightTheme')
  })

  it('should return the default dark theme when isDarkTheme is true and darkThemeName is undefined', () => {
    const theme = getCurrentTheme(true, 'light', undefined as unknown as string)
    expect(theme).toBe(THEMES.dark)
    expect(consoleWarnSpy).not.toHaveBeenCalled()
  })

  it('should return the default light theme when isDarkTheme is false and lightThemeName is undefined', () => {
    const theme = getCurrentTheme(false, undefined as unknown as string, 'dark')
    expect(theme).toBe(THEMES.light)
    expect(consoleWarnSpy).not.toHaveBeenCalled()
  })

  it('should return the default dark theme when isDarkTheme is true and darkThemeName is null', () => {
    const theme = getCurrentTheme(true, 'light', null as unknown as string)
    expect(theme).toBe(THEMES.dark)
    expect(consoleWarnSpy).not.toHaveBeenCalled()
  })

  it('should return the default light theme when isDarkTheme is false and lightThemeName is null', () => {
    const theme = getCurrentTheme(false, null as unknown as string, 'dark')
    expect(theme).toBe(THEMES.light)
    expect(consoleWarnSpy).not.toHaveBeenCalled()
  })
})
