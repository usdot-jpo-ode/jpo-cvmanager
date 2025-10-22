// Create a custom AbortController that can abort thunk promises
class ThunkAbortController extends AbortController {
  private thunkPromises: Set<{ abort: () => void }> = new Set()

  constructor(thunkPromise?: { abort: () => void }) {
    super()
    
    if (thunkPromise) {
      this.addThunkPromise(thunkPromise)
    }
  }

  addThunkPromise(thunkPromise: { abort: () => void }) {
    this.thunkPromises.add(thunkPromise)
    
    // Clean up when thunk completes
    if ('finally' in thunkPromise) {
      (thunkPromise as any).finally(() => {
        this.thunkPromises.delete(thunkPromise)
      })
    }
  }

  abort(reason?: any) {
    // Abort the fetch requests
    super.abort(reason)
    
    // Abort all registered thunk promises
    this.thunkPromises.forEach(thunkPromise => {
      try {
        thunkPromise.abort()
      } catch (error) {
        console.warn('Error aborting thunk promise:', error)
      }
    })
    
    // Clear the set
    this.thunkPromises.clear()
  }
}