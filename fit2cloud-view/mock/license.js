const {success, error} = require("./result-holder")

const licenses = {
  valid: {
    status: "valid",
    license: {
      "corporation": "xxxxxxxxxxxx",
      "expired": "2030-07-03",
      "licenseVersion": "v2",
      "product": "cmp",
      "generateTime": "1593763389356",
      "edition": "Enterprise",
      "count": 11
    },
    message: ""
  },
  invalid: {
    status: "invalid",
    license: {},
    message: "license has invalid"
  },
  expired: {
    status: "expired",
    license: {
      "corporation": "xxxxxxxxxxxx",
      "expired": "2020-07-03",
      "licenseVersion": "v2",
      "product": "cmp",
      "generateTime": "1593763389356",
      "edition": "Enterprise",
      "count": 11
    },
    message: "license has expired since 2020-07-03"
  },
}

module.exports = [
  {
    url: '/samples/license/save',
    type: 'post',
    response: config => {
      const {license} = config.body
      const data = licenses[license];

      if (!data) {
        return success(licenses.invalid)
      }
      return success(data)
    }
  },
]
