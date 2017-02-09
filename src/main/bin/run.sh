#! /bin/sh
cd `dirname $0`
PWD=`pwd`

for i in lib/*.jar;
do CLASSPATH="$CLASSPATH":$PWD/$i;
done

echo "CLASSPATH="$CLASSPATH

nohup java -server -Xmx1024m -Xms1024m -Xmn512m -classpath .:config:$CLASSPATH com.xxx.xxx.MyServer >/dev/null 2>&1 &