# 数据集成

## 前端

### 打包

```
npm run build:prod
```

把 dist 目录压缩成 dist.zip

### 部署

nginx.conf

```nginx
server {
    listen       18180 default_server;
    server_name  _;

    root /usr/share/nginx/dist;
    index index.html;

    #access_log  /var/log/nginx/host.access.log  main;

    location / {
        try_files $uri /index.html;
    }



    location /api/ {
        proxy_pass http://127.0.0.1:8180;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

}
```

Dockerfile

```
FROM m.daocloud.io/docker.io/nginx:1.20.2-perl

# 删除默认配置
RUN rm /etc/nginx/conf.d/default.conf

# 拷贝自定义 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 拷贝前端 dist 文件
COPY dist /usr/share/nginx/dist


CMD ["nginx", "-g", "daemon off;"]

```



启动容器

```
#!/bin/bash

# 设置脚本出错即退出
set -e

CONTAINER_NAME="datax-front"
IMAGE_NAME="datax-front-images:1.0"
DIST_ZIP="dist.zip"
DIST_DIR="dist"

echo "===== 删除旧容器 ====="
sudo docker rm -f $CONTAINER_NAME || true

echo "===== 清理旧 dist 目录 ====="
rm -rf $DIST_DIR

echo "===== 解压 dist.zip ====="
unzip $DIST_ZIP -d $DIST_DIR

echo "===== 构建 Docker 镜像 ====="
sudo docker build -t $IMAGE_NAME .

echo "===== 启动新容器 ====="
sudo docker run -d --privileged --network=host --name $CONTAINER_NAME $IMAGE_NAME

echo "===== 部署完成 ====="
```





## 后端

### 打包

```
#全量打包
mvn clean install -DskipTests

# 单独打包
mvn -pl datax-admin -am clean install -DskipTests

mvn -pl datax-executor -am clean install -DskipTests

```

### 部署

解压，安装

```
/bin/install.sh
```



Dockerfile

```
FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/openjdk:8-jdk

WORKDIR /app

COPY ./datax-web-2.1.2 ./

ENTRYPOINT ["tail","-f","/dev/null"]

```



启动容器

```
sudo docker run --privileged --network=host -itd  --name datax-java -v /home/laihz/project/datax/datax-end/datax-web-2.1.2:/app/ swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/openjdk:8-jdk tail -f /dev/null
```



运行后端程序

```
sudo docker exec -it datax-java bash -c "cd /app/bin && ./stop.sh -m datax-admin && ./start.sh -m datax-admin"
sudo docker exec -it datax-java bash -c "cd /app/bin && ./stop.sh -m datax-executor && ./start.sh -m datax-executor"
```



