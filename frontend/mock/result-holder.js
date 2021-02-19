class ResultHolder {
  constructor(success, data, message) {
    this.success = success;
    this.data = data;
    this.message = message;
  }
}

const success = data => {
  return new ResultHolder(true, data)
}

const error = message => {
  return new ResultHolder(false, undefined, message)
}

module.exports = {
  success,
  error
}
