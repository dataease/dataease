export default {
    methods: {
        cancel(arr = []) {
            if (arr.length === 1) {
                const [dataset] = arr;
                const { id, name } = dataset;
                this.$router.push({
                    name: 'dataset',
                    params: {
                        id,
                        name
                    }
                })
            } else {
                this.$router.back()
            }
        }
    }
}