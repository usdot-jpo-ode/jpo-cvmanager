/* eslint-disable no-undef */
module.exports = class MockWorker {
  constructor() {
    this.postMessage = jest.fn()
    this.terminate = jest.fn()
  }
}
