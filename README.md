##Технологии

Kotlin 2.1.x
Ktor 3.2.x
Exposed ORM
PostgreSQL
kotlinx.serialization (JSON)
BCrypt для хэширования паролей
JWT для авторизации

##Функционал

Создание пользователя (POST /users)
Получение списка пользователей (GET /users)
Получение пользователя по ID (GET /users/{id})
Удаление пользователя (DELETE /users/{id})
Аутентификация (POST /auth/login) с выдачей JWT
Пароли хранятся в виде хэшей (BCrypt)
Корректные HTTP-коды и JSON-ответы для всех запросов

## Установка и запуск

1. **Склонировать репозиторий**

```bash
git clone https://github.com/Arromen/ktor-crud-auth.git
cd ktor-crud-auth
