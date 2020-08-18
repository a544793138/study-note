# Git 命令列表

## 安装 Git

[参考链接](https://www.liaoxuefeng.com/wiki/0013739516305929606dd18361248578c67b8067c8c017b000/00137396287703354d8c6c01c904c7d9ff056ae23da865a000)

windows 系统的在 Git 官网下载安装包后安装即可。然后打开 Git -> **Git Bash**，这个就是 git 命令的输入窗口了。在打开的命令行窗口中输入以下配置：

```bash
$ git config --global user.name "Your Name"
$ git config --global user.email "email@example.com"
```

作用：因为Git是分布式版本控制系统，所以，每个机器都必须自报家门：你的名字和Email地址。

注意`git config`命令的`--global`参数，用了这个参数，表示你这台机器上所有的Git仓库都会使用这个配置，当然也可以对某个仓库指定不同的用户名和Email地址。

## Git

```bash
# 版本库又名仓库，英文名repository
$ git init
Initialized empty Git repository in /Users/michael/learngit/.git/

# 将文件加入暂存区
$ git add <file>
# 将所有改动的文件加入暂存区
$ git add -A
# 提交被修改(modified)和被删除(deleted)文件，不包括新文件(new)
$ git add -u
# 提交新文件(new)和被修改(modified)文件，不包括被删除(deleted)文件
$ git add .  

# 将暂存区的内容更新到版本库
$ git commit -m <message>

# 查看仓库当前的状态，显示修改等状态
$ git status

# 查看文件的修改状态
$ git diff <file>

# 告诉我们提交历史记录，以便确定要回退到哪个版本
$ git log
#一行输出
$ git log --pretty=oneline	
# 看到分支合并图
$ git log --graph

# 回到上一个版本
$ git reset --hard HEAD^
# 回到上上一个版本
$ git reset --hard HEAD^^
# 回到上100个版本
$ git reset --hard HEAD~100
# 回到指定的版本号，版本号不用全写，Git 会自动寻找
$ git reset --hard 1094a
# 回到指定版本号，软的，可以该版本暂存区里的情况
$ git reset --sorf XXXX

# 命令历史，以便确定要回到未来的哪个版本
$ git reflog

# 用版本库里的版本替换工作区的版本，无论工作区是修改还是删除，都可以“一键还原”
$ git checkout -- <file>
# 把暂存区的修改撤销掉，重新放回工作区（即清空暂存区）
$ git reset HEAD <file>

# 删除文件，删除后需要 commit
$ git rm <file>

# 创建并切换到dev分支
$ git checkout -b dev
# 创建dev分支
$ git branch dev
# 切换到dev分支
$ git checkout dev
# 查看当前分支情况
$ git branch
# 在当前分支上合并dev分支
$ git merge dev
# 禁用Fast forward合并模式
$ git merge --no-ff -m "merge with no-ff" dev

# 删除dev分支
$ git branch -d dev

# 可以把当前工作现场“储藏”起来，等以后恢复现场后继续工作
$ git stash
# 查看储藏的工作现场
$ git stash list
# 恢复工作现场，恢复后，stash内容并不删除，可知道恢复的编号
$ git stash apply <stash@{0}>
# 删除储藏中的工作现场
$ git stash drop
# 恢复工作现场并同时删去储藏中的
$ git stash pop

# 强行删除未合并的分支
$ git branch -D <name>

# 查看远程库的信息
$ git remote
# 更详细的信息
$ git remote -v
# 同时推送到多个git远程仓库地址，使用以下命令可以多加一个推送目的地
git remote set-url --add origin [git远程仓库地址]

# 把该分支上的所有本地提交推送到远程库
$ git push origin master

# 把当前分支的最新提交从远程仓库抓下来
$ git pull
# 如果git pull提示no tracking information，则说明本地分支和远程分支的链接关系没有创建，用命令git branch --set-upstream-to=origin/<branch-name> <branch-name>
$ git branch --set-upstream-to=origin/dev dev

# 切换到需要打标签的分支上，然后使用如下打上标签，name为标签名
$ git tag <name>
# 查看所有标签
$ git tag
# 查看历史提交的commit id
$ git log --pretty=oneline --abbrev-commit
# 对指定 commit id 的地方打上name为 v0.9 的标签
$ git tag v0.9 f52c633
# 查看标签信息
$ git show <tagname>
# 创建带有说明文字的标签，-a为标签名，-m为说明文字
$ git tag -a v0.1 -m "version 0.1 released" 1094adb
# 注意：标签总是和某个commit挂钩。如果这个commit既出现在master分支，又出现在dev分支，那么在这两个分支上都可以看到这个标签。

# 删除本地标签（创建的标签都只存储在本地，不会自动推送到远程。所以，打错的标签可以在本地安全删除）
$ git tag -d v0.1
# 推送某个标签到远程
$ git push origin v1.0
# 一次性推送全部尚未推送到远程的本地标签
$ git push origin --tags
# 删除远程标签，需要先删除本地标签，然后再使用如下
$ git push origin :refs/tags/v0.9

# 将本地的远程分支列表更新到与远程仓库拥有的分支列表一致，是用来清理本地已经被删除的远程分支的
git remote prune origin
```

## git svn

```bash
# 使用git克隆svn库的项目
$ git svn clone http://192.1.5.44:9999/svn/unionJavaCode/mastercard/caas

# 从svn上更新代码, 相当于svn的update
$ git svn rebase

# 提交你的commit到svn远程仓库
$ git svn dcommit
```

## git Github

```bash
# 生成你自己的SSH key，会在你的用户目录下生成id_rsa和id_rsa.pub两个文件，id_rsa是私钥，不能泄露出去，id_rsa.pub是公钥。
$ ssh-keygen -t rsa -C "youremail@example.com"

# 将本地仓库关联到github远程仓库，需要先有本地仓库，然后在GitHub上创建空仓库
$ git remote add origin git@github.com:<username>/<repository>.git
# 把本地仓库的内容推送到GitHub仓库，第一次推送master分支的所有内容
$ git push -u origin master
# 本地master分支的最新修改推送至GitHub
$ git push origin master

# 将远程仓库克隆到本地
$ git clone git@github.com:<username>/<repository>.git

# 创建新的本地 git 仓库，并推动到刚创建的 github 仓库
echo "# spring-security-saml-token-demo" >> README.md
git init
git add README.md
git commit -m "first commit"
git remote add origin [新仓库地址]
git push -u origin master

# 推送一个已经存在的本地 git 仓库到刚创建的 github 仓库
git remote add origin [新仓库地址]
git push -u origin master
```
