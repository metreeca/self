#
# Copyright © 2013-2015 Metreeca srl. All Rights Reserved.
#

deps = node_modules
bins = node_modules/.bin

browsers = PhantomJS


########################################################################################################################

all : build

build : $(deps)

test : $(deps)

	$(bins)/karma start --single-run --browsers $(browsers) specs/karma.js

clean :

	rm -rf $(deps)


########################################################################################################################

$(deps) : package.json

	npm install
