java_binary(
  name = 'plovr',
  deps = [':plovr-lib'],
  main_class = 'org.plovr.cli.Main')

java_library(
  name = 'plovr-lib',
  srcs = glob(['src/**/*.java']),
  resources = glob([
    'src/**/*.js',
    'src/**/*.soy',
    'src/**/*.ts',
  ]) + [
    'library_manifest.txt',
    'third_party_manifest.txt'
  ],
  source = '8',
  target = '8',
  deps = [
    '//tools/imports:revs',
    ':third-party-support-libs',
    '//third-party:COMPILE',
    '//closure/closure-library:closure-library',
    '//closure/closure-stylesheets:closure-stylesheets',
  ],
)

java_library(
  name = 'plovr-lib-src',
  resources = glob(['src/**/*.java']),
)

java_library(
  name = 'third-party-support-libs',
  srcs = [],
  resources = ['//third-party/javascript:soyutils_usegoog.js'],
  resources_root = './third-party')

java_test(
  name = 'test',
  srcs = glob(['test/**/*.java']),
  resources = glob(['test/**/*.js']),
  deps = [
    ':plovr-lib',
    '//third-party:COMPILE',
    '//third-party:TEST',
    '//closure/closure-stylesheets:closure-stylesheets',
  ],
)
