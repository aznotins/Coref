@echo off

java -Xmx3G -Dfile.encoding=UTF-8 -cp dist/*;lib/* lv.pipe.Pipe -prop coref.prop %*