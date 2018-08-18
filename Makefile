# This Makefile runs the Yext-specific packaging process for building and
# packaging plovr-lib.jar.
#
# Specifically, rather than using the 58MB plovr.jar produced by Buck, we have
# separate jars for closure tools and build a plovr-lib.jar containing just the
# plovr classes, in addition to soyutils_usegoog.js.

PLOVR_LIB_JAR=buck-out/gen/lib__plovr-lib__output/plovr-lib.jar

build:
	buck fetch ...
	buck build plovr
	jar -uf $(PLOVR_LIB_JAR) -C third-party javascript/soyutils_usegoog.js
	@echo "Built: $(PLOVR_LIB_JAR)"

.PHONY: build
