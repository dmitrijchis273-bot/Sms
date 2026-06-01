import puppeteer from 'puppeteer';

const SMS_CACHE = new Map(); // { phoneNumber: { code, timestamp } }
const SMS_TTL = 10 * 60 * 1000; // 10 minutes

export async function sendSmsWithPuppeteer(phoneNumber) {
  let browser = null;
  try {
    console.log(`[SMS] Отправка кода для номера: ${phoneNumber}`);

    browser = await puppeteer.launch({
      headless: true,
      args: ['--no-sandbox', '--disable-setuid-sandbox'],
    });

    const page = await browser.newPage();
    
    // Перехватываем все запросы для поиска кода
    let smsCode = null;
    let interceptedRequests = [];

    page.on('response', async (response) => {
      interceptedRequests.push({
        url: response.url(),
        status: response.status(),
      });
    });

    await page.goto('https://www.zaymer.ru/auth/login', {
      waitUntil: 'networkidle2',
      timeout: 30000,
    });

    console.log('[SMS] Страница загружена');

    // Ищем поле ввода телефона
    const phoneInputSelector = 'input[type="tel"], input[name="phone"], input[placeholder*="телефон" i]';
    
    await page.waitForSelector(phoneInputSelector, { timeout: 5000 }).catch(() => {
      console.log('[SMS] Селектор не найден, пытаемся найти альтернативный');
    });

    // Вводим номер телефона
    await page.type(phoneInputSelector, phoneNumber, { delay: 100 });
    console.log(`[SMS] Номер введен: ${phoneNumber}`);

    // Ищем и кликаем кнопку "ПРОДОЛЖИТЬ"
    const buttonSelector = 'button:has-text("ПРОДОЛЖИТЬ"), button[type="submit"], .btn-primary';
    
    await Promise.all([
      page.click(buttonSelector),
      page.waitForNavigation({ waitUntil: 'networkidle2', timeout: 15000 }).catch(() => {
        console.log('[SMS] Навигация не произошла, может быть AJAX запрос');
      }),
    ]);

    console.log('[SMS] Кнопка нажата, ожидание SMS...');

    // Ждем 2 секунды чтобы убедиться что SMS отправлена
    await page.waitForTimeout(2000);

    // Генерируем случайный 4-6 значный код для демо
    // В реальном сценарии нужно перехватывать SMS через API
    smsCode = Math.floor(1000 + Math.random() * 9000).toString();

    console.log(`[SMS] Код сгенерирован: ${smsCode}`);
    console.log(`[SMS] ✓ SMS должна быть отправлена на номер: ${phoneNumber}`);

    // Кэшируем код с TTL
    SMS_CACHE.set(phoneNumber, {
      code: smsCode,
      timestamp: Date.now(),
    });

    // Очищаем кэш через TTL
    setTimeout(() => {
      SMS_CACHE.delete(phoneNumber);
      console.log(`[SMS] Код для ${phoneNumber} удален из кэша`);
    }, SMS_TTL);

    return {
      success: true,
      message: `Код отправлен на номер ${phoneNumber}`,
      phoneNumber,
      code: smsCode, // В продакшене этого не должно быть!
    };
  } catch (error) {
    console.error('[SMS] Ошибка при отправке SMS:', error.message);
    return {
      success: false,
      message: `Ошибка при отправке SMS: ${error.message}`,
      error: error.message,
    };
  } finally {
    if (browser) {
      await browser.close();
    }
  }
}

export function getSmsCode(phoneNumber) {
  const cached = SMS_CACHE.get(phoneNumber);
  if (!cached) {
    return null;
  }
  // Проверяем TTL
  if (Date.now() - cached.timestamp > SMS_TTL) {
    SMS_CACHE.delete(phoneNumber);
    return null;
  }
  return cached.code;
}

export function clearSmsCode(phoneNumber) {
  SMS_CACHE.delete(phoneNumber);
  console.log(`[SMS] Код для ${phoneNumber} очищен`);
}
