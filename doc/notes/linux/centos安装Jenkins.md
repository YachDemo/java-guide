# centOS7安装Jenkins

## 安装Jenkins

rpm包下载地址https://mirrors.tuna.tsinghua.edu.cn/jenkins/redhat-stable/

安装

```shell
rpm -ivh jenkins-2.263.3-1.1.noarch.rpm
# jenkins配置文件位置
/etc/sysconfig/jenkins
```

赋权
```shell
chown -R jenkins:jenkins /var/lib/jenkins
chown -R jenkins:jenkins /var/cache/jenkins
chown -R jenkins:jenkins /var/log/jenkins
```


## 一个通用的Jenkinsfile脚本

```groovy
pipeline {
    agent any
    triggers{
        GenericTrigger(
            genericVariables: [
              [key: 'ref', value: '$.ref']
             ],
            causeString: 'Triggered on $ref',
            token: 'tikbee-branch',
            printContributedVariables: true,
            printPostContent: true,
            silentResponse: false,
            regexpFilterText: '$ref',
            // 指定要监听的分支
            regexpFilterExpression: '^refs/heads/(\\d*|develop)$'
        )
    }
    stages {
        stage('拉取代码') {
            steps {
                // 开始构建
                git branch: 'develop', credentialsId: '637ce472-8658-4450-9295-b77dfe048bad', url: 'http://125.88.36.99:8099/nono/tikbee.git'
                echo "代码同步完成"
            }
        }
        stage('构建jar') {
            steps {
                sh 'mvn clean package -Dmaven.test.skip=true -Dmaven.compile.fork=true -T 4C -Pdev'
                // 复制项目
                echo "=================开始复制项目================="
                sh 'cp bussiness-app/target/tikbeeApp.jar /app/tikbee/'
                sh 'cp bussiness-mch/target/mchApp.jar /app/mch/'
                sh 'cp bussiness-rider/target/tikbeeRider.jar /app/rider/'
                echo "=================复制项目结束================="
            }
        }
        stage('启动') {
            steps {
                // 启动服务
                echo "开始启动服务"
                // 启动app
                echo "启动app"
                sh 'JENKINS_NODE_COOKIE=dontKillMe sh /app/tikbee/run-app.sh -start'
                echo "启动mch"
                sh 'JENKINS_NODE_COOKIE=dontKillMe sh /app/mch/run-mch.sh -start'
                echo "启动rider"
                sh 'JENKINS_NODE_COOKIE=dontKillMe sh /app/rider/run-rider.sh -start'
            }
        }
    }
}
```