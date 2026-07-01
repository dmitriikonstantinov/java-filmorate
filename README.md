# Filmorate

REST API для сервиса по оценке фильмов. Учебный проект.

## Стек
- Java 21
- Spring Boot 3.2.2
- Lombok
- Maven
- JUnit 5
- SLF4J + Logback

## Функциональность
- Создание, обновление, получение и удаление пользователей и фильмов
- Валидация данных с кастомными исключениями
- Глобальная обработка ошибок через @ControllerAdvice
- Unit-тесты для контроллеров

## Запуск
```bash
mvn clean package
java -jar target/filmorate-*.jar
