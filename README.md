# MyShop
Проект для сдачи ДЗ второго модуля 

## Описание
Данное приложение реализует базовую логику приложения магазина. Содержит следующие основные страницы:
- Страница витрины товаров
- Страница конкретного товара
- Страница корзины покупателя
- Страница всех заказов
- Страница конкретного заказа

Помимо основного приложения магазина, представлено простенькое приложение сервиса оплаты, обеспечивающего
следующие функции:
- Получение баланса пользователя по его userId (по умолчанию 100000 - храниться в памяти)
- Списание средств со счета

## Требования
- Java JDK 21
- Apache Maven 3.9.9+
- Приложение поставляется в виде двух самодостаточных Jar архивов с встроенными сервлет контейнерами Netty
- RDBMS PostgreSQL 16+
- Хранилище кеша Redis 7.0.11

## Запуск
1. Выполнить сборку приложения: `mvn clean package`
2. Выполните запуск Docker compose манифеста: `docker compose up -d`
3. Проект доступен на порту 8080, основная страница доступна по адресу: http://localhost:8080/

## Структура модулей
- api-spec - контракт взаимодействия МС myshop и payment
- payment-service-api - проект для генерации серверной части приложения платежей на основе контракта api-spec
- payment-service-client - проект для генерации клиентской части для сервиса платежей
- payment-service - сервис платежей
- shop-app - приложение витрины магазина

## Структура проекта shop-app
### Базовые пакеты
- [src/main/java](shop-app/src/main/java) - исходный код приложения
- [/src/main/resources](shop-app/src/main/resources) - конфигурационные файлы приложения
- [pom.xml](shop-app/pom.xml) - Maven конфигурация
- [/src/test/java](shop-app/src/test/java) - директория с тестами проекта

### Структура каталогов исходного кода
- [configuration](shop-app/src/main/java/ru/girqa/myshop/configuration) - конфигурация приложения. Содержит настройки шаблонизатора Thymeleaf и маршрутизации HTTP запросов
- [controller](shop-app/src/main/java/ru/girqa/myshop/controller) - содержит основные HTTP обработчики приложения (главная страница витрины товаров, конкретный товар, корзина, заказы, изображения)
- [exception](shop-app/src/main/java/ru/girqa/myshop/exception) - содержит базовые исключения приложения
- [model](shop-app/src/main/java/ru/girqa/myshop/model) - содержит доменные классы (для работы с БД), транспортные сущности и их мапперы
- [repository](shop-app/src/main/java/ru/girqa/myshop/repository) - содержит репозитории для работы с Spring Data R2dbc. В большинстве случаев было достаточно базовых реализаций
- [service](shop-app/src/main/java/ru/girqa/myshop/service) - содержит сервисный слой приложения

### Ресурсы проекта
- [application.yml](shop-app/src/main/resources/application.yml) - конфигурационный файл приложения
- [templates](shop-app/src/main/resources/templates) - шаблоны thymeleaf для отображения в слое представления
- [db/changelog](shop-app/src/main/resources/db/changelog) - схема бд, накатываемая посредствам liquibase

## Тестирование
Тесты слоя данных реализованы на базе Testcontainers. Перед запуском тестов необходимо запустить Docker окружение.

Запуск тестов выполняется командой `mvn test`

