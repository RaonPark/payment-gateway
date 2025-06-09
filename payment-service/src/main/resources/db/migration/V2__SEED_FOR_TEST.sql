insert into PAYMENT(MERCHANT_ID, ORDER_ID, AMOUNT, CURRENCY, STATUS, METHOD, USER_DATA, approved_at, created_at, ID)
values('1',
       '1',
       1203948.00,
       'KRW',
       0,
       0,
       '{"age": 29, "city": "Seoul", "username": "raonpark"}'::jsonb,
       now(),
       now(),
       1);