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
        val devtool = "cheap-module-eval-source-map"

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

    }
}

suspend fun webpackBaseConfig() = WebpackConfig {
    entry {
        name("app" to "./src/main.js")
    }

    output {
        path = "aaaaaa"
        filename = "[name].js"
        publicPath = "ccccc"
    }

    resolve {
        extensions = listOf(".js", ".vue", ".json")
        alias = mapOf(
            "vue$" to "vue/dist/vue.esm.js",
            "@" to env.project("src")
        )
    }

    module {
        rule("vue-loader", Regex("\\.vue\$"))

        rule("babel-loader", Regex("\\.js\$")) {
            include(env.project("src"), env.project("test"), env.nodeModules("webpack-dev-server/client"))
        }

        rule("url-loader", Regex("\\.(png|jpe?g|gif|svg)(\\?.*)?\$")) {
            options {
                limit = 10000
                name = "img/[name].[hash:7].[ext]"
            }
        }

        rule("url-loader",Regex("\\.(mp4|webm|ogg|mp3|wav|flac|aac)(\\?.*)?\$")) {
            options {
                limit = 10000
                name = "media/[name].[hash:7].[ext]"
            }
        }

        rule("url-loader", Regex("\\.(woff2?|eot|ttf|otf)(\\?.*)?\$")) {
            options {
                limit = 10000
                name = "fonts/[name].[hash:7].[ext]"
            }
        }
    }

    node {
        setImmediate = false
        dgram = "empty"
        fs = "empty"
        net = "empty"
        tls = "empty"
        child_process = "empty"
    }
}