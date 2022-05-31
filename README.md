# beholder

Сервис собирает метрики о производительности загрузки платёжной формы из разных регионов.

## Особенности имплементации

Точка входа в приложение: ```dev.vality.beholder.service.BeholderService.behold```. Этот метод вызывается по расписанию, указанном в свойстве ```schedule.cron```.

### Подготовка данных для загрузки платёжной формы

Для загрузки платёжной формы необходимы ```InvoiceId``` и ```InvoiceAccessToken```.

Схема взаимодействия с [swag-payments](https://github.com/valitydev/swag-payments):

![PaymentsImage](img/payments.drawio.svg)

Алгоритм взаимодействия реализован здесь: ```dev.vality.beholder.service.PaymentsService.prepareFormData```

### Загрузка платёжной формы

Beholder умеет работать c простым selenium-hub и с [lambdatest](https://www.lambdatest.com/).
В реальности обе интеграции работают через selenium API и являются совместимыми.

Алгоритм загрузки и сбора метрик формы реализован здесь: ```dev.vality.beholder.service.SeleniumService.executePaymentRequest```

Его можно разбить на следующие шаги:

1. Установить подключение с selenium-hub/lambdatest
2. Отправить запрос на загрузку формы
3. Собрать метрики загрузки формы посредством javascript'а (```dev.vality.beholder.util.SeleniumUtil.PERFORMANCE_SCRIPT```)
4. Заполнить форму и отправить запрос на проведение платежа
5. Собрать логи производительности браузера

### Обновление метрик

Собранная на предыдущем шаге информация о производительности формы записывается в соответствующие метрики prometheus'а.
Этот функционал реализован в классе ```dev.vality.beholder.service.MetricsService```

#### Метрики

| Название                                         | Лейблы          | Описание                                                                     |
|--------------------------------------------------|-----------------|------------------------------------------------------------------------------|
| beholder_form_loading_requests_total             | browser, region | счетчик запросов на загрузку формы                                           |
| beholder_form_loading_failed_total               | browser, region | счетчик неудачных загрузок формы                                             |
| beholder_form_dom_complete_duration_millis       | browser, region | время от момента отправки запроса до полной загрузки формы в миллисекундах   |
| beholder_form_waiting_response_duration_millis   | browser, region | время от момента отправки запроса до начала получения ответа в миллисекундах |
| beholder_form_receiving_response_duration_millis | browser, region | время между получением первым и последним байтом информации в миллисекундах  |
| beholder_form_resource_loading_duration_millis   | browser, region | время, затраченное на загрузку ресурса (включая блокировки, ожидание и т.д)  |

## Тестирование

Поскольку загружать во время юнит-тестирования реальную платежную форму не представляется возможным,
реализован интеграционный тест, который отключен по умолчанию, однако может использоваться для локальной отладки сервиса.

Тест: ```dev.vality.beholder.IntegrationTest```
Подготовка к запуску теста:

1. Прописать валидные значения в следующих свойствах:
   1. payments.api-url - адрес для обращения к api
   2. payments.form-url - адрес для загрузки платёжной формы
   3. payments.request.shop-id - идентификатор магазина, который нужно использовать
2. Прописать в свойстве ```dev.vality.beholder.IntegrationTest.TEST_USER_TOKEN``` валидный токен
3. Готово, можно запускать тест.

## Полезные ссылки

[Описание метрик производительности](https://developer.mozilla.org/en-US/docs/Web/Performance/Navigation_and_resource_timings), которые можно получить через JS.
[Регионы](https://www.lambdatest.com/capabilities-generator/), доступные для тестирования. На их основе заполнен справочник ```regions.json```
