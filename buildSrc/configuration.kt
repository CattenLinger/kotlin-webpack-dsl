object Configuration {
    object Development {
        //
        // Paths
        //
        val assetsSubDirectory = "static"
        val assetsPublicPath = "/"
        val proxyTable = mapOf<String, String>()

        //
        // Various Dev Server settings
        //
        /** can be overwritten by process.env.HOST */
        val host = "localhost"
        /** can be overwritten by process.env.PORT, if port is in use, a free one will be determined */
        val port = 8080
        val autoOpenBrowser = false
        val errorOverlay = true
        val notifyOnErrors = true
        /** https://webpack.js.org/configuration/dev-server/#devserver-watchoptions- */
        val poll = false

        //
        // Source Maps
        //
        /** https://webpack.js.org/configuration/devtool/#development */
        val devtool = "cheap-webpack.module-eval-source-map"

        /**
         *  If you have problems debugging vue-files in devtools,
         *  set this to false - it *may* help
         *
         *  https://vue-loader.vuejs.org/en/options.html#cachebusting
         */
        val cacheBusting = true

        val cssSourceMap = true
    }

    object Build {
        val productionSourceMap = true
    }
}