# shell 脚本

shell 脚本就是可以在 linux 系统中运行的脚本，其实就是逐行执行的 linux 命令组合。

## 批量解压文件夹中的 tar 包
> 这些 tar 包所以解压出来的文件夹名都是 report，所以直接一起解压的话会一直覆盖。
> 所以要将第二个解压的内容解压到临时文件夹中，然后再移动到最终文件夹中。

```shell
#!/usr/bin/env bash

# 定义变量和赋值
unInit="true";

# 将文件夹中的所有 tar 包，都赋值给 tars 变量
for tars in *.tar;

do
  # $unInit ：使用 unInit 变量
  # 条件语句开始
  if [ $unInit = "true" ]; then
    # 解压
    tar xf $tars;
    unInit="false";
    # $(basename ${tars}) ：获取对应 tar 包的文件名，$(basename ${tars} .tar)：获取对应 tar 包的文件名，去掉文件后缀名
    mv ./report/report.html report/$(basename ${tars} .tar).html;
    mv ./report/report.json report/$(basename ${tars} .tar).json;
  else
    mkdir temp-report;
    tar xf $tars -C temp-report;
    mv ./temp-report/report/report.html ./report/$(basename ${tars} .tar).html;
    mv ./temp-report/report/report.json ./report/$(basename ${tars} .tar).json;
    rm -rf temp-report;
  # 条件语句结束
  fi
done
```