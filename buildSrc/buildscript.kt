import webpack.*
import webpack.common.Files
import webpack.common.jsObject
import webpack.common.jsRegex

fun WebpackConfigContext.assetsPathOf(location: String): String {
    return env.path.join("assets", location)
}

val webpackBaseConfig = WebpackConfig {
    entry {
        name("app" to "./src/main.js")
    }

    output {
        path = env.project("dist")
        filename = "[name].js"
        publicPath = "/"
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
                name = assetsPathOf("img/[name].[hash:7].[ext]")
            }
        }

        rule("url-loader", Regex("\\.(mp4|webm|ogg|mp3|wav|flac|aac)(\\?.*)?\$")) {
            options {
                limit = 10000
                name = assetsPathOf("media/[name].[hash:7].[ext]")
            }
        }

        rule("url-loader", Regex("\\.(woff2?|eot|ttf|otf)(\\?.*)?\$")) {
            options {
                limit = 10000
                name = assetsPathOf("fonts/[name].[hash:7].[ext]")
            }
        }
    }
}

val WebpackConfigContext.devServerConfiguration : dynamic
    get() = jsObject {
        it.historyApiFallback = jsObject { apiFallbackConfig ->
            apiFallbackConfig.rewrites = arrayOf(jsObject { rewrite -> rewrite.from = jsRegex(".*"); rewrite.to = "/index.html" })
        }
        it.hot = true
        it.compress = true
        it.host = "localhost"
        it.port = 8080
    }

val webpackConfigurations = mapOf(
    "production" to webpackBaseConfig.merge {
        mode("production")

        module {
            useStylesheetLoaders(sourceMap = Configuration.Build.productionSourceMap)
        }

        plugins {
            WebpackDefinePlugin {
                this["process.env"] = jsObject {
                    it["mode"] = "production"
                }
            }

            MiniCssExtractPlugin()

            HtmlWebpackPlugin {
                filename = "index.html"
                template = "index.html"
                inject = true
                minifyOptions {
                    removeComments = true
                    collapseWhitespace = true
                    removeAttributeQuotes = true
                }
                chunksSortMode = "auto"
            }

            WebpackModuleConcatenationPlugin()

            CopyWebpackPlugin {
                copy(env.project("static"), Configuration.Development.assetsSubDirectory) { !it.split(Files.pathDelimiter).last().startsWith(".") }
            }
        }

        optimization {
            moduleIds = "named"
            minimize = true
        }
    },

    "development" to webpackBaseConfig.merge {
        developerTool(DeveloperTools.CheapModelSourceMap)

        mode("development")

        module {
            useStylesheetLoaders(extractCss = false, sourceMap = Configuration.Development.cssSourceMap)
        }

        optimization {
            chunkIds = "natural"
        }

        plugins {
            WebpackDefinePlugin {
                this["process.env"] = jsObject {
                    it["mode"] = "development"
                }
            }

            WebpackHotModuleReplacementPlugin()
            WebpackNoEmitOnErrorPlugin()

            HtmlWebpackPlugin {
                filename = "index.html"
                template = "index.html"
                inject = true
            }

            CopyWebpackPlugin {
                copy(env.project("static"), Configuration.Development.assetsSubDirectory) { !it.split(Files.pathDelimiter).last().startsWith(".") }
            }
        }
    }
)