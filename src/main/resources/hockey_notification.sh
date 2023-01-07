# Скрипт запуска на сервере
pkill java # убиваем предыдущий запуск
java -cp '/root/hockey_calendar/build/libs/*:/root/hockey_calendar/config/' hockey_calendar # запускаем с указанием где лежать креды