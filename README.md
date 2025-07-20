# MyShop
Проект для сдачи ДЗ второго модуля 

## Описание
Данное приложение реализует базовую логику приложения магазина. Содержит следующие основные страницы:
- Страница витрины товаров
- Страница конкретного товара
- Страница корзины покупателя
- Страница всех заказов
- Страница конкретного заказа

## Требования
- Java JDK 21
- Apache Maven 3.9.9+
- Приложение поставляется в виде самодостаточного Jar архива с встроенным сервлет сервером Netty
- База данных PostgreSQL 16+

## Запуск
1. Выполнить сборку приложения: `mvn clean package`
2. Выполните запуск Docker compose манифеста: `docker compose up -d`
3. Проект доступен на порту 8080, основная страница доступна по адресу: http://localhost:8080/

## Структура проекта
### Базовые пакеты
- [/src/main/java](src/main/java) - исходный код приложения
- [/src/main/resources](src/main/resources) - конфигурационные файлы приложения
- [pom.xml](pom.xml) - Maven конфигурация
- [/src/test/java](src/test/java) - директория с тестами проекта

### Структура каталогов исходного кода
- [configuration](src/main/java/ru/girqa/myshop/configuration) - конфигурация приложения. Содержит настройки шаблонизатора Thymeleaf и маршрутизации HTTP запросов
- [controller](src/main/java/ru/girqa/myshop/controller) - содержит основные HTTP обработчики приложения (главная страница витрины товаров, конкретный товар, корзина, заказы, изображения)
- [exception](src/main/java/ru/girqa/myshop/exception) - содержит базовые исключения приложения
- [model](src/main/java/ru/girqa/myshop/model) - содержит доменные классы (для работы с БД), транспортные сущности и их мапперы
- [repository](src/main/java/ru/girqa/myshop/repository) - содержит репозитории для работы с Spring Data R2dbc. В большинстве случаев было достаточно базовых реализаций
- [service](src/main/java/ru/girqa/myshop/service) - содержит сервисный слой приложения

### Ресурсы проекта
- [application.yml](src/main/resources/application.yml) - конфигурационный файл приложения
- [templates](src/main/resources/templates) - шаблоны thymeleaf для отображения в слое представления
- [db/changelog](src/main/resources/db/changelog) - схема бд, накатываемая посредствам liquibase

## Тестирование
Тесты слоя данных реализованы на базе Testcontainers. Перед запуском тестов необходимо запустить Docker окружение.

Запуск тестов выполняется командой `mvn test`

