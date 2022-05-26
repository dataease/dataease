var profile = {
  resourceTags: {
    copyOnly: function (filename, mid) {
      return mid === "moment/package";
    },

    amd: function (filename, mid) {
      return /\.js$/.test(filename);
    }
  }
};
