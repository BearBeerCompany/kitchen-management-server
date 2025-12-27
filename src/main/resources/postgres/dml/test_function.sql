INSERT INTO public.test_orders
(id, t, product, take_away)
VALUES(nextval('test_orders_id_seq'::regclass), now(), 'pizza', false);

INSERT INTO public.test_orders
(id, t, product, take_away)
VALUES(nextval('test_orders_id_seq'::regclass), now(), 'toast', true);


INSERT INTO public.test_orders
(id, t, product, take_away)
VALUES(nextval('test_orders_id_seq'::regclass), now(), 'sandwich', true);