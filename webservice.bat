@echo off

java -Xmx3G -Dfile.encoding=UTF-8 -cp dist/*;lib/* lv.pipe.PipeServer -port 8183 %*