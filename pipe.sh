#!/bin/sh

cd $(dirname $0)
java -Xmx3G -Dfile.encoding=UTF-8 -cp dist/*:lib/* lv.pipe.Pipe -prop coref.prop $*
