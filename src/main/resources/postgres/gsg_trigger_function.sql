-- trigger function

-- DROP FUNCTION public.menu_items_notify();

-- N.B.: adeguare la dichiarazione delle variabili per gli id delle categorie dei panini da considerare, sulla base di quanto definito nella tabella tipologie
CREATE OR REPLACE FUNCTION public.menu_items_notify()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
declare
	panini_cat_id INTEGER := 1;
    piadine_cat_id INTEGER := 2;
    toast_cat_id INTEGER := 3;
    piatti_unici_cat_id INTEGER := 6;
    panini_spec_cat_id INTEGER := 8;
	isValidMenuItem INTEGER = 0;
	json_result json;
	v_txt text;
	id INT;
    order_number INT;
    table_number VARCHAR;
    insert_date VARCHAR;
    insert_time VARCHAR;
    client_name VARCHAR;
    take_away BOOLEAN;
    order_notes VARCHAR;
    quantity INT;
    menu_item_id INT;
    menu_item_name VARCHAR;
    menu_item_notes VARCHAR;
    category_id INT;
    category_name VARCHAR;
BEGIN
	RAISE NOTICE 'Nuova riga inserita nella tabella righe_articoli con id: %', NEW.id;

	-- verifico se articolo inserito appartiene a una delle categorie gestibili dalle piastre
	select count(ra.id) into isValidMenuItem
    from righe_articoli ra
    inner join tipologie t on ra.desc_tipologia = t.descrizione
    where ra.id = new.id and t.id in (panini_cat_id, piadine_cat_id, toast_cat_id, piatti_unici_cat_id, panini_spec_cat_id);

	if isValidMenuItem > 0 then
		select json_build_object(
        'id', ra.id,
        'orderNumber', o.progressivo, 'tableNumber', o."numeroTavolo", 'date', o."data", 'time', o.ora, 'clientName', o.cliente,
        'takeAway', o.esportazione, 'orderNotes', o.note,
        'quantity', r.quantita,
        'menuItemId', a.id, 'menuItemName', a.descrizione, 'menuItemNotes', ra.note,
        'categoryId', t.id, 'categoryName', t.descrizione
        )
        into json_result
        from righe_articoli ra
        inner join righe r on r.id = ra.id_riga
        inner join articoli a on r.descrizionebreve = a.descrizionebreve
        inner join tipologie t on a.id_tipologia = t.id
        inner join ordini o on o.id = r.id_ordine
        where ra.id = new.id;

	  	-- RAISE NOTICE 'Risultato JSON %', json_result;

	   	v_txt := format('{"operation": "%s","item": %s }', TG_OP, json_result);
	  	-- RAISE NOTICE 'Payload notifica %', v_txt;

	    -- Recupero dei singoli campi dal JSON
        id := (json_result->>'id')::INT;
        order_number := (json_result->>'orderNumber')::INT;
        table_number := json_result->>'tableNumber';
        insert_date := (json_result->>'date');
        insert_time := (json_result->>'time');
        client_name := json_result->>'clientName';
        take_away := (json_result->>'takeAway')::BOOLEAN;
        order_notes := json_result->>'orderNotes';
        quantity := (json_result->>'quantity')::INT;
        menu_item_id := (json_result->>'menuItemId')::INT;
        menu_item_name := json_result->>'menuItemName';
        menu_item_notes := json_result->>'menuItemNotes';
        category_id := (json_result->>'categoryId')::INT;
        category_name := json_result->>'categoryName';

	  	-- insert body notifica in tabella orders_ack
        INSERT INTO orders_ack (
            id, order_number, table_number, insert_date, insert_time, client_name, take_away,
            order_notes, quantity, menu_item_id, menu_item_name, menu_item_notes, category_id, category_name, ack
        ) VALUES (
            id, order_number, table_number, insert_date, insert_time, client_name, take_away,
            order_notes, quantity, menu_item_id, menu_item_name, menu_item_notes, category_id, category_name, false
        );

        -- invio notifica
	    EXECUTE FORMAT('NOTIFY plate_orders, ''%s''', v_txt);
	else
		RAISE NOTICE 'Categoria riga articolo non valida: %', NEW.pos_tipologia;
	end if;

	RETURN NEW;
END;
$function$
;

-- trigger
--DROP TRIGGER righe_articoli_trigger ON public.righe_articoli;

CREATE TRIGGER righe_articoli_trigger
after INSERT OR UPDATE ON righe_articoli
FOR EACH ROW
EXECUTE PROCEDURE menu_items_notify();

-- tabella da usare per verifica ACK notifiche
-- drop table orders_ack
CREATE TABLE orders_ack (
    id int PRIMARY KEY,
    order_number int not null,
    table_number VARCHAR(255),
    insert_date VARCHAR(255) not null,
    insert_time VARCHAR(255) not null,
    client_name varchar(255),
    take_away bool,
    order_notes varchar(255),
    quantity int not null,
    menu_item_id int not null,
    menu_item_name varchar(255),
    menu_item_notes varchar(255),
    category_id int not null,
    category_name varchar(255),
    ack bool
);