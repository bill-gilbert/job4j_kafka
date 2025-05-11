rem на подумать.
rem возможно надо было добавить полный путь к kafka и использовать его
rem т.к. я могу использовать другую версию кафки и что-бы было проще между ними переключаться 
rem пример : добавил переменную окружения Kafka со значение c:\kafka\kafka_2.12-3.4.0\bin\windows\
rem а в пути PATH оставить %KAFKA%. 

kafka-topics --create ^
  --bootstrap-server localhost:9092,localhost:9192,localhost:9292 ^
  --topic message-events ^
  --partitions 3 ^
  --replication-factor 3