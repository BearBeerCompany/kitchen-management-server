create table if not exists TEST_ORDERS (
  id        serial,
  t         timestamptz DEFAULT now(),
  product   text,
  take_away   boolean
);

CREATE or replace FUNCTION notify_plates_channes()
RETURNS trigger AS
$$
DECLARE
  v_txt text;
BEGIN
  v_txt := format(
 '{"operation": "%s","item": "%s", "takeAway": "%s" }',
	TG_OP,
	new.product,
	(CASE WHEN (new.take_away IS not TRUE) THEN 'false' ELSE 'true' END)
	);
  RAISE NOTICE '%', v_txt;
    EXECUTE FORMAT('NOTIFY plate_orders, ''%s''', v_txt);
  RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER plates_orders_trigger BEFORE INSERT OR UPDATE
       ON TEST_ORDERS
       FOR EACH ROW EXECUTE PROCEDURE notify_plates_channes();

