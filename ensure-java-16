#!/bin/bash

JV=`java -version 2>&1 >/dev/null | head -1`
echo $JV | sed -E 's/^.*version "([^".]*)\.[^"]*".*$/\1/'

if [ "$JV" != 16 ]; then
	case "$1" in
	install)
		echo "installing sdkman..."
		curl -s "https://get.sdkman.io" | bash
		source ~/.sdkman/bin/sdkman-init.sh
		sdk install java 16.0.1-open
		;;
	use)
		echo "must source ~/.sdkman/bin/sdkman-init.sh"
		exit 1
		;;
	esac
fi
