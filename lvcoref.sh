#!/bin/sh

cd $(dirname $0)
java -Xmx1G -Dfile.encoding=UTF-8 -cp dist/*:lib/* lv.coref.io.CorefPipe -prop coref.prop $*