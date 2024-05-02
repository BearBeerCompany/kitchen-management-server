-- DROP FUNCTION public.menu_items_notify();

CREATE OR REPLACE FUNCTION public.menu_items_notify()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
declare
	panini_type_pos INTEGER := 1;
	piadine_type_pos INTEGER := 2;
	toast_type_pos INTEGER := 3;
	piatti_unici_type_pos INTEGER := 9;
	panini_spec_type_pos INTEGER := 12;
	isValidMenuItem INTEGER = 0;
	json_result json;
	v_txt text;
BEGIN
	RAISE NOTICE 'Nuova riga inserita nella tabella righe_articoli con id: %', NEW.id;

	-- verifico se articolo inserito appartiene a
	select count(ra.id) into isValidMenuItem
    from righe_articoli ra
    inner join tipologie t on ra.desc_tipologia = t.descrizione
    where ra.id = new.id and t.id in (1,2,3,6,8);

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
        where ra.id>> = new.id;

	  	RAISE NOTICE 'Risultato JSON %', json_result;

	   	v_txt := format('{"operation": "%s","item": %s }', TG_OP, json_result);
	  	RAISE NOTICE 'Payload notifica %', v_txt;

	    EXECUTE FORMAT('NOTIFY plate_orders, ''%s''', v_txt);
	else
		RAISE NOTICE 'Categoria riga articolo non valida: %', NEW.pos_tipologia;
	end if;

	RETURN NEW;
END;
$function$
;
