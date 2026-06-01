import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import { sendSmsWithPuppeteer, getSmsCode, clearSmsCode } from './services/smsService.js';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());

// Логирование запросов
app.use((req, res, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.path}`);
  next();
});

// Health check
app.get('/health', (req, res) => {
  res.json({ status: 'ok', message: 'SMS API Tester Backend is running' });
});

// API endpoint для отправки SMS
app.post('/api/sms/send', async (req, res) => {
  try {
    const { phone_number } = req.body;

    if (!phone_number) {
      return res.status(400).json({
        success: false,
        message: 'Поле phone_number обязательно',
      });
    }

    // Валидация номера телефона (базовая)
    const phoneRegex = /^\d{10,15}$/;
    if (!phoneRegex.test(phone_number.replace(/\D/g, ''))) {
      return res.status(400).json({
        success: false,
        message: 'Некорректный формат номера телефона',
      });
    }

    console.log(`[API] Получен запрос на отправку SMS для: ${phone_number}`);

    // Отправляем SMS через Puppeteer
    const result = await sendSmsWithPuppeteer(phone_number);

    if (result.success) {
      return res.json({
        success: true,
        message: result.message,
        phone_number,
        request_id: `req_${Date.now()}`,
      });
    } else {
      return res.status(500).json({
        success: false,
        message: result.message,
        error: result.error,
      });
    }
  } catch (error) {
    console.error('[API] Ошибка при обработке запроса:', error);
    return res.status(500).json({
      success: false,
      message: 'Внутренняя ошибка сервера',
      error: error.message,
    });
  }
});

// API endpoint для проверки кода (для тестирования)
app.post('/api/sms/verify', (req, res) => {
  try {
    const { phone_number, code } = req.body;

    if (!phone_number || !code) {
      return res.status(400).json({
        success: false,
        message: 'Поля phone_number и code обязательны',
      });
    }

    const cachedCode = getSmsCode(phone_number);

    if (!cachedCode) {
      return res.status(400).json({
        success: false,
        message: 'Код истек или не найден',
      });
    }

    if (cachedCode === code) {
      clearSmsCode(phone_number);
      return res.json({
        success: true,
        message: 'Код верифицирован успешно',
        phone_number,
      });
    } else {
      return res.status(400).json({
        success: false,
        message: 'Неверный код',
      });
    }
  } catch (error) {
    console.error('[API] Ошибка при проверке кода:', error);
    return res.status(500).json({
      success: false,
      message: 'Внутренняя ошибка сервера',
      error: error.message,
    });
  }
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({
    success: false,
    message: 'Endpoint не найден',
    path: req.path,
  });
});

// Error handler
app.use((err, req, res, next) => {
  console.error('[ERROR]', err);
  res.status(500).json({
    success: false,
    message: 'Внутренняя ошибка сервера',
    error: err.message,
  });
});

// Запуск сервера
app.listen(PORT, () => {
  console.log(`
╔════════════════════════════════════════╗
║   SMS API Tester Backend               ║
║   Сервер запущен на порту ${PORT}        ║
╚════════════════════════════════════════╝

Endpoints:
  GET  /health                    - Проверка статуса
  POST /api/sms/send              - Отправка SMS кода
  POST /api/sms/verify            - Проверка SMS кода

Документация:
  POST /api/sms/send
  Body: { "phone_number": "+79991234567" }

  POST /api/sms/verify
  Body: { "phone_number": "+79991234567", "code": "1234" }
  `);
});

export default app;
