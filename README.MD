# Kotlin Webpack DSL

This is a demo for showing how to generate webpack config using kotlin/js.

## Usage

Clone and run:
```bash
./buildSrc/script init
```

It will download all dependencies and prepare directories for use.

After init, use:

- `npm run build` for webpack build
- `npm run serve` for dev server
- `npm run build_script` for generating build script
- `npm run clean` for cleaning

## Files and Directories

- `buildSrc` : Build script root
- `buildSrc/script` : Convenient script for build script compiling
- `buildSrc/webpack` : The kotlin webpack dsl src
- `buildSrc/buildscript.kt`: build script config, where the dsl usage stay
- `buildSrc/dependencies.rc` : build script dependencies, described by maven coordinates
- `kotlin_build`: output of kotlinc-js
- `lib`: Jars for the build script